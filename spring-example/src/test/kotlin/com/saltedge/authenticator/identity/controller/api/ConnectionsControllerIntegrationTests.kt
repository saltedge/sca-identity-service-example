/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2019 Salt Edge Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 or later.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * For the additional permissions granted for Salt Edge Authenticator
 * under Section 7 of the GNU General Public License see THIRD_PARTY_NOTICES.md
 */
package com.saltedge.authenticator.identity.controller.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.saltedge.authenticator.identity.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.identity.HEADER_KEY_EXPIRES_AT
import com.saltedge.authenticator.identity.HEADER_KEY_SIGNATURE
import com.saltedge.authenticator.identity.TestTools
import com.saltedge.authenticator.identity.model.Connection
import com.saltedge.authenticator.identity.model.ConnectionsRepository
import com.saltedge.authenticator.identity.model.User
import com.saltedge.authenticator.identity.model.mapping.CreateConnectionRequest
import com.saltedge.authenticator.identity.model.mapping.CreateConnectionRequestData
import com.saltedge.authenticator.identity.tools.nowUtcSeconds
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest(ConnectionsController::class)
class ConnectionsControllerIntegrationTests {
	@Autowired
	private val mvc: MockMvc? = null
	@MockBean
	private val repository: ConnectionsRepository? = null
	private val testConnection = Connection().apply {
		publicKey = TestTools.rsaPublicKeyString
		accessToken = "accessToken"
		user = User()
	}

	@Test
	fun createConnectionTest_returnSuccess() {
		val requestData = CreateConnectionRequest(CreateConnectionRequestData(
				publicKey = "key",
				returnUrl = "authenticator//connect",
				platform = "android",
				pushToken = "token"
		))
		val json: String = ObjectMapper().writeValueAsString(requestData)
		val connectionCaptor = ArgumentCaptor.forClass(Connection::class.java)
		mvc!!.perform(post(CONNECTIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(json))

				.andExpect(status().isOk)
				.andExpect(jsonPath("$.data.connect_url", Matchers.startsWith("https://localhost/admin/enroll?token=")))
				.andExpect(jsonPath("$.data.id", `is`("0")))

		Mockito.verify(repository!!).save(connectionCaptor.capture())
		val connection = connectionCaptor.value
		assertThat(connection.publicKey).isEqualTo(requestData.data!!.publicKey)
		assertThat(connection.pushToken).isEqualTo(requestData.data!!.pushToken)
		assertThat(connection.platform).isEqualTo(requestData.data!!.platform)
		assertThat(connection.returnUrl).isEqualTo(requestData.data!!.returnUrl)
		assertThat(connection.connectToken).isNotEmpty()
	}

	@Test
	fun createConnectionTest_returnError_whenInvalidRequestContent() {
		val requestData = CreateConnectionRequestData(
				publicKey = "key",
				returnUrl = "authenticator//connect",
				platform = "android",
				pushToken = "token"
		)

		mvc!!.perform(post(CONNECTIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(ObjectMapper().writeValueAsString(requestData)))

				.andExpect(status().isBadRequest)
	}

	@Test
	fun revokeConnectionTest_returnSuccess() {
		val requestUrl = "http://localhost$CONNECTIONS_REQUEST_PATH"
		val expiresAt = (nowUtcSeconds + 60).toString()
		val signature = TestTools.createSignature(
				requestMethod = "${HttpMethod.DELETE}",
				requestUrl = requestUrl,
				expiresAt = expiresAt,
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)

		given(repository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection)
		val connectionCaptor = ArgumentCaptor.forClass(Connection::class.java)

		mvc!!.perform(delete(CONNECTIONS_REQUEST_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature)
		)

				.andExpect(status().isOk)
				.andExpect(jsonPath("$.data.access_token", `is`("accessToken")))
				.andExpect(jsonPath("$.data.success", `is`(true)))

		Mockito.verify(repository).save(connectionCaptor.capture())
		assertThat(connectionCaptor.value.revoked).isTrue()
	}
}
