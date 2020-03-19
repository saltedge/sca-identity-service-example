/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2020 Salt Edge Inc.
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
package com.saltedge.sca.sdk.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltedge.sca.sdk.TestTools;
import com.saltedge.sca.sdk.models.api.ErrorResponse;
import com.saltedge.sca.sdk.models.api.requests.UpdateAuthorizationRequest;
import com.saltedge.sca.sdk.models.api.responces.AuthorizationsResponse;
import com.saltedge.sca.sdk.models.api.responces.UpdateAuthorizationResponse;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionsRepository;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.tools.DateTools;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;

import javax.naming.NamingException;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;
import static com.saltedge.sca.sdk.controllers.AuthorizationsController.AUTHORIZATIONS_REQUEST_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthorizationErrorsIntegrationTests {
	@LocalServerPort
	Integer randomServerPort = 0;
	@MockBean
	private ServiceProvider serviceProvider;
	@MockBean
	private ClientConnectionsRepository connectionsRepository;
	@MockBean
	private AuthorizationsRepository authorizationsRepository;

	private TestRestTemplate testRestTemplate = new TestRestTemplate();
	private ClientConnectionEntity testConnection;
	private AuthorizationEntity testAuthorization;
	
	public AuthorizationErrorsIntegrationTests() throws Exception {
		testConnection = new ClientConnectionEntity();
		try {
			testConnection.setPublicKey(TestTools.getRsaPublicKeyPemString());
		} catch (FileNotFoundException | NamingException e) {
			e.printStackTrace();
		}
		testConnection.setUserId(String.valueOf(1));
		
		testAuthorization = new AuthorizationEntity();
		testAuthorization.setId(1L);
		testAuthorization.setUserId(String.valueOf(1));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenNoAccessToken() throws Exception {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		String expiresAt = String.valueOf(DateTools.nowUtcSeconds() + 60);
		String signature = TestTools.createSignature(
				HttpMethod.POST.toString(),
				requestUrl,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt);
		headers.add(HEADER_KEY_SIGNATURE, signature);
		ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(new ErrorResponse("AccessTokenMissing", "Access Token is missing."));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenNoExpiresAt() {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");

		ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(new ErrorResponse("SignatureExpired", "Expired Signature."));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenExpiresAtIsBeforeNow() {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");
		headers.add(HEADER_KEY_EXPIRES_AT, "0");

		ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<String>(headers), ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(new ErrorResponse("SignatureExpired", "Expired Signature."));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenNoSignature() {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");
		headers.add(HEADER_KEY_EXPIRES_AT, String.valueOf(DateTools.nowUtcSeconds() + 60));

		ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(new ErrorResponse("SignatureMissing", "Signature is missing."));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenNoConnectionByAccessToken() {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(null);
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");
		headers.add(HEADER_KEY_EXPIRES_AT, String.valueOf(DateTools.nowUtcSeconds() + 60));
		headers.add(HEADER_KEY_SIGNATURE, "signature");

		ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		assertThat(response.getBody()).isEqualTo(new ErrorResponse("ConnectionNotFound", "Authenticator Connection Not Found."));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenSignatureIsInvalid() throws Exception {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		String expiresAt = String.valueOf(DateTools.nowUtcSeconds() + 60);
		String signature = TestTools.createSignature(
				HttpMethod.POST.toString(),
				requestUrl,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection);
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt);
		headers.add(HEADER_KEY_SIGNATURE, signature);

		ResponseEntity<ErrorResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), ErrorResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(response.getBody()).isEqualTo(new ErrorResponse("InvalidSignature", "Invalid Signature."));
	}

	@Test
	public void getAuthorizationsTest_returnSuccess_onGetAuthorizations() throws Exception {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH;
		String expiresAt = String.valueOf(DateTools.nowUtcSeconds() + 60);
		String signature = TestTools.createSignature(
				HttpMethod.GET.toString(),
				requestUrl,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection);
		given(authorizationsRepository.findByUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
				userCaptor.capture(),
				any(LocalDateTime.class))
		).willReturn(new ArrayList<>());

		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt);
		headers.add(HEADER_KEY_SIGNATURE, signature);

		ResponseEntity<AuthorizationsResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.GET, new HttpEntity<>(headers), AuthorizationsResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().data).isEmpty();
	}

	@Test
	public void updateAuthorizationTest_returnSuccess_onPutAuthorization() throws Exception {
		String requestUrl = getBaseUrl() + AUTHORIZATIONS_REQUEST_PATH + "/1";
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));

		UpdateAuthorizationRequest body = new UpdateAuthorizationRequest(true, "1234567890");

		String rawBody = new ObjectMapper().writeValueAsString(body);
		String signature = TestTools.createSignature(
				HttpMethod.PUT.toString(),
				requestUrl,
				expiresAt,
				rawBody,
				TestTools.getRsaPrivateKey()
		);
		LinkedMultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
		headers.add(HEADER_KEY_ACCESS_TOKEN, "accessToken");
		headers.add(HEADER_KEY_EXPIRES_AT, expiresAt);
		headers.add(HEADER_KEY_SIGNATURE, signature);

		ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection);
		given(authorizationsRepository.findFirstByIdAndUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
				eq(1L),
				userCaptor.capture(),
				any(LocalDateTime.class))
		).willReturn(testAuthorization);

		ResponseEntity<UpdateAuthorizationResponse> response = testRestTemplate.exchange(requestUrl, HttpMethod.PUT, new HttpEntity<>(body, headers), UpdateAuthorizationResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().data.success).isFalse();
	}

	private String getBaseUrl() {
		return "http://localhost:" + randomServerPort;
	}
}
