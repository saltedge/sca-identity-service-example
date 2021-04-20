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
package com.saltedge.sca.sdk.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltedge.sca.sdk.MockMvcTestAbs;
import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.TestTools;
import com.saltedge.sca.sdk.models.api.requests.ScaUpdateAuthorizationRequest;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.tools.DateTools;
import com.saltedge.sca.sdk.tools.JsonTools;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;
import static com.saltedge.sca.sdk.controllers.AuthorizationsController.AUTHORIZATIONS_REQUEST_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AuthorizationsController.class)
public class AuthorizationsControllerIntegrationTests extends MockMvcTestAbs {
	@Test
	public void whenGetActiveAuthorizations_returnSuccess() throws Exception {
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testAuthorizedConnection);
		given(authorizationsService.getActiveAuthorizations(any(ClientConnectionEntity.class))).willReturn(new ArrayList<>());
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));
		String signature = TestTools.createSignature(
				"get",
				"http://localhost" + AUTHORIZATIONS_REQUEST_PATH,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		mvc.perform(MockMvcRequestBuilders.get(AUTHORIZATIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data", Matchers.hasSize(0)));
	}

	@Test
	public void whenGetAuthorizationsTest_returnError_whenNoConnection() throws Exception {
		ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(null);
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));
		String signature = TestTools.createSignature(
				"get",
				"http://localhost" + AUTHORIZATIONS_REQUEST_PATH,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		mvc.perform(MockMvcRequestBuilders.get(AUTHORIZATIONS_REQUEST_PATH).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error_class", Matchers.is("ConnectionNotFound")))
				.andExpect(jsonPath("$.error_message", Matchers.is("Authenticator Connection Not Found.")));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenNoPublicKeyInConnection() throws Exception {
		ClientConnectionEntity connection = new ClientConnectionEntity();
		connection.setUserId("5");
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(connection);
		given(authorizationsService.getActiveAuthorizations(any(ClientConnectionEntity.class))).willReturn(new ArrayList<>());
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));
		String signature = TestTools.createSignature(
				"get",
				"http://localhost" + AUTHORIZATIONS_REQUEST_PATH,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		mvc.perform(MockMvcRequestBuilders.get(AUTHORIZATIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error_class", Matchers.is("ConnectionNotFound")))
				.andExpect(jsonPath("$.error_message", Matchers.is("Authenticator Connection Not Found.")));
	}

	@Test
	public void getAuthorizationsTest_returnError_whenNoUserInConnection() throws Exception {
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testConnection);
		given(authorizationsService.getActiveAuthorizations(any(ClientConnectionEntity.class))).willReturn(new ArrayList<>());
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));
		String signature = TestTools.createSignature(
				"get",
				"http://localhost" + AUTHORIZATIONS_REQUEST_PATH,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		mvc.perform(MockMvcRequestBuilders.get(AUTHORIZATIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error_class", Matchers.is("UserNotFound")))
				.andExpect(jsonPath("$.error_message", Matchers.is("User Not Found.")));
	}

	@Test
	public void whenConfirmAuthorization_returnSuccess() throws Exception {
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testAuthorizedConnection);
		given(authorizationsConfirmService.confirmAuthorization(
			testAuthorizedConnection, 1L, "1234567890", true, "GEO:1.2;3.4", "passcode")
		).willReturn(true);

		String requestUrl = "http://localhost" + AUTHORIZATIONS_REQUEST_PATH + "/1";
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));

		ScaUpdateAuthorizationRequest body = new ScaUpdateAuthorizationRequest(true, "1234567890");
		String rawBody = new ObjectMapper().writeValueAsString(body);
		String signature = TestTools.createSignature(
			HttpMethod.PUT.toString(),
			requestUrl,
			expiresAt,
			rawBody,
			TestTools.getRsaPrivateKey()
		);

		mvc.perform(MockMvcRequestBuilders.put(AUTHORIZATIONS_REQUEST_PATH + "/1")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
			.header(HEADER_KEY_EXPIRES_AT, expiresAt)
			.header(HEADER_KEY_SIGNATURE, signature)
			.header("GEO-Location", "GEO:1.2;3.4")
			.header("Authorization-Type", "passcode")
			.content(rawBody)
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.success", Matchers.is(true)))
			.andExpect(jsonPath("$.data.id", Matchers.is("1")));
	}
}
