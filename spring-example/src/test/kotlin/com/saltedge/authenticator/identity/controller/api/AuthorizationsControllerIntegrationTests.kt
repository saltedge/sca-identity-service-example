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

import com.saltedge.authenticator.identity.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.identity.HEADER_KEY_EXPIRES_AT
import com.saltedge.authenticator.identity.HEADER_KEY_SIGNATURE
import com.saltedge.authenticator.identity.TestTools
import com.saltedge.authenticator.identity.model.AuthorizationsRepository
import com.saltedge.authenticator.identity.model.Connection
import com.saltedge.authenticator.identity.model.ConnectionsRepository
import com.saltedge.authenticator.identity.model.User
import com.saltedge.authenticator.identity.model.mapping.EncryptedAuthorization
import com.saltedge.authenticator.identity.tools.nowUtcSeconds
import org.hamcrest.Matchers
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.anyLong
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest(AuthorizationsController::class)
class AuthorizationsControllerIntegrationTests {
	@Autowired
	private val mvc: MockMvc? = null
	@MockBean
	private val connectionsRepository: ConnectionsRepository? = null
	@MockBean
	private val authorizationsRepository: AuthorizationsRepository? = null
	private val testConnection = Connection().apply {
		publicKey = TestTools.rsaPublicKeyString
		user = User()
	}

	@Test
	fun getAuthorizationsTest_returnSuccess() {
		val userCaptor = ArgumentCaptor.forClass(User::class.java)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection)
		given(authorizationsRepository!!.findByUserAndExpiresAtGreaterThanAndConfirmedIsNull(user = userCaptor.capture(), currentDate = anyLong())).willReturn(emptyList());
		val expiresAt = nowUtcSeconds + 60
		val signature = TestTools.createSignature(
				requestMethod = "get",
				requestUrl = "http://localhost$AUTHORIZATIONS_REQUEST_PATH",
				expiresAt = expiresAt.toString(),
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)

		mvc!!.perform(get(AUTHORIZATIONS_REQUEST_PATH).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isOk)
				.andExpect(jsonPath("$.data", hasSize<EncryptedAuthorization>(0)))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoConnection() {
		val userCaptor = ArgumentCaptor.forClass(User::class.java)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(null)
		val expiresAt = nowUtcSeconds + 60
		val signature = TestTools.createSignature(
				requestMethod = "get",
				requestUrl = "http://localhost$AUTHORIZATIONS_REQUEST_PATH",
				expiresAt = expiresAt.toString(),
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)

		mvc!!.perform(get(AUTHORIZATIONS_REQUEST_PATH).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isUnauthorized)
				.andExpect(jsonPath("$.error_class", Matchers.`is`("ConnectionNotFound")))
				.andExpect(jsonPath("$.error_message", Matchers.`is`("ConnectionNotFound")))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoPublicKeyInConnection() {
		val userCaptor = ArgumentCaptor.forClass(User::class.java)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(Connection())
		given(authorizationsRepository!!.findByUserAndExpiresAtGreaterThanAndConfirmedIsNull(user = userCaptor.capture(), currentDate = anyLong())).willReturn(emptyList());
		val expiresAt = nowUtcSeconds + 60
		val signature = TestTools.createSignature(
				requestMethod = "get",
				requestUrl = "http://localhost$AUTHORIZATIONS_REQUEST_PATH",
				expiresAt = expiresAt.toString(),
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)

		mvc!!.perform(get(AUTHORIZATIONS_REQUEST_PATH).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isUnauthorized)
				.andExpect(jsonPath("$.error_class", Matchers.`is`("ConnectionNotFound")))
				.andExpect(jsonPath("$.error_message", Matchers.`is`("ConnectionNotFound")))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoUserInConnection() {
		val userCaptor = ArgumentCaptor.forClass(User::class.java)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken"))
				.willReturn(testConnection.apply { user = null })
		given(authorizationsRepository!!.findByUserAndExpiresAtGreaterThanAndConfirmedIsNull(user = userCaptor.capture(), currentDate = anyLong())).willReturn(emptyList());
		val expiresAt = nowUtcSeconds + 60
		val signature = TestTools.createSignature(
				requestMethod = "get",
				requestUrl = "http://localhost$AUTHORIZATIONS_REQUEST_PATH",
				expiresAt = expiresAt.toString(),
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)

		mvc!!.perform(get(AUTHORIZATIONS_REQUEST_PATH).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isNotFound)
				.andExpect(jsonPath("$.error_class", Matchers.`is`("UserNotFound")))
				.andExpect(jsonPath("$.error_message", Matchers.`is`("UserNotFound")))
	}
}
