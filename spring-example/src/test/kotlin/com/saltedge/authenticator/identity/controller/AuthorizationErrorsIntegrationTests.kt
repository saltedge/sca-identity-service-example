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
package com.saltedge.authenticator.identity.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.saltedge.authenticator.identity.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.identity.HEADER_KEY_EXPIRES_AT
import com.saltedge.authenticator.identity.HEADER_KEY_SIGNATURE
import com.saltedge.authenticator.identity.TestTools
import com.saltedge.authenticator.identity.controller.api.AUTHORIZATIONS_REQUEST_PATH
import com.saltedge.authenticator.identity.model.*
import com.saltedge.authenticator.identity.model.mapping.*
import com.saltedge.authenticator.identity.tools.nowUtcSeconds
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthorizationErrorsIntegrationTests {
	@LocalServerPort
	var randomServerPort: Int = 0
	val baseUrl: String
		get() = "http://localhost:$randomServerPort"
	@MockBean
	private val connectionsRepository: ConnectionsRepository? = null
	@MockBean
	private val authorizationsRepository: AuthorizationsRepository? = null
	private val testRestTemplate: TestRestTemplate
		get() = TestRestTemplate()
	private val testConnection = Connection().apply {
		publicKey = TestTools.rsaPublicKeyString
		user = User()
	}
	private val testAuthorization = Authorization().apply {
		id = 1
		user = User()
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoAccessToken() {
		val headers = LinkedMultiValueMap<String, String>()
		val response = testRestTemplate.exchange("$baseUrl$AUTHORIZATIONS_REQUEST_PATH", HttpMethod.GET, HttpEntity<Any>(headers), ErrorResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
		assertThat(response.body).isEqualTo(ErrorResponse("AccessTokenNotFound", "AccessTokenNotFound"))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoExpiresAt() {
		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")

		val response = testRestTemplate.exchange("$baseUrl$AUTHORIZATIONS_REQUEST_PATH", HttpMethod.GET, HttpEntity<Any>(headers), ErrorResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
		assertThat(response.body).isEqualTo(ErrorResponse("SignatureExpired", "SignatureExpired"))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenExpiresAtIsBeforeNow() {
		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")
		headers.add(HEADER_KEY_EXPIRES_AT, "0")

		val response = testRestTemplate.exchange("$baseUrl$AUTHORIZATIONS_REQUEST_PATH", HttpMethod.GET, HttpEntity<Any>(headers), ErrorResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
		assertThat(response.body).isEqualTo(ErrorResponse("SignatureExpired", "SignatureExpired"))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoSignature() {
		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")
		headers.add(HEADER_KEY_EXPIRES_AT, (nowUtcSeconds + 60).toString())

		val response = testRestTemplate.exchange("$baseUrl$AUTHORIZATIONS_REQUEST_PATH", HttpMethod.GET, HttpEntity<Any>(headers), ErrorResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
		assertThat(response.body).isEqualTo(ErrorResponse("SignatureNotFound", "SignatureNotFound"))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenNoConnectionByAccessToken() {
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(null)
		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")
		headers.add(HEADER_KEY_EXPIRES_AT, (nowUtcSeconds + 60).toString())
		headers.add(HEADER_KEY_SIGNATURE, "signature")

		val response = testRestTemplate.exchange("$baseUrl$AUTHORIZATIONS_REQUEST_PATH", HttpMethod.GET, HttpEntity<Any>(headers), ErrorResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
		assertThat(response.body).isEqualTo(ErrorResponse("ConnectionNotFound", "ConnectionNotFound"))
	}

	@Test
	fun getAuthorizationsTest_returnError_whenSignatureIsInvalid() {
		val requestUrl = "$baseUrl$AUTHORIZATIONS_REQUEST_PATH"
		val expiresAt = (nowUtcSeconds + 60).toString()
		val signature = TestTools.createSignature(
				requestMethod = "${HttpMethod.POST}",
				requestUrl = requestUrl,
				expiresAt = expiresAt,
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection)
		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt)
		headers.add(HEADER_KEY_SIGNATURE, signature)

		val response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, HttpEntity<Any>(headers), ErrorResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
		assertThat(response.body).isEqualTo(ErrorResponse("InvalidSignature", "InvalidSignature"))
	}

	@Test
	fun getAuthorizationsTest_returnSuccess_onGetAuthorizations() {
		val requestUrl = "$baseUrl$AUTHORIZATIONS_REQUEST_PATH"
		val expiresAt = (nowUtcSeconds + 60).toString()
		val signature = TestTools.createSignature(
				requestMethod = "${HttpMethod.GET}",
				requestUrl = requestUrl,
				expiresAt = expiresAt,
				requestBody = "",
				privateKey = TestTools.rsaPrivateKey
		)

		val userCaptor = ArgumentCaptor.forClass(User::class.java)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection)
		given(authorizationsRepository!!.findByUserAndExpiresAtGreaterThanAndConfirmedIsNull(
				user = userCaptor.capture(),
				currentDate = anyLong())
		).willReturn(emptyList());

		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt)
		headers.add(HEADER_KEY_SIGNATURE, signature)

		val response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, HttpEntity<Any>(headers), AuthorizationsResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(response.body!!.data).isEqualTo(emptyList<EncryptedAuthorization>())
	}

	@Test
	fun updateAuthorizationTest_returnSuccess_onPutAuthorization() {
		val requestUrl = "$baseUrl$AUTHORIZATIONS_REQUEST_PATH/1"
		val expiresAt = (nowUtcSeconds + 60).toString()

		val body = UpdateAuthorizationRequest(UpdateAuthorizationRequestData(confirm = true, authorizationCode = "1234567890"))
		val rawBody = ObjectMapper().writeValueAsString(body)
		val signature = TestTools.createSignature(
				requestMethod = "${HttpMethod.PUT}",
				requestUrl = requestUrl,
				expiresAt = expiresAt,
				requestBody = rawBody,
				privateKey = TestTools.rsaPrivateKey
		)
		val headers = LinkedMultiValueMap<String, String>()
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken")
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt)
		headers.add(HEADER_KEY_SIGNATURE, signature)

		val userCaptor = ArgumentCaptor.forClass(User::class.java)
		given(connectionsRepository!!.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection)
		given(authorizationsRepository!!.findByIdAndUserAndExpiresAtGreaterThanAndConfirmedIsNull(
				id = eq(1L),
				user = userCaptor.capture(),
				currentDate = anyLong())
		).willReturn(Optional.of(testAuthorization));

		val response = testRestTemplate.exchange(requestUrl, HttpMethod.PUT, HttpEntity(body, headers), UpdateAuthorizationResponse::class.java)

		assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
		assertThat(response.body!!.data.success).isFalse()
	}
}
