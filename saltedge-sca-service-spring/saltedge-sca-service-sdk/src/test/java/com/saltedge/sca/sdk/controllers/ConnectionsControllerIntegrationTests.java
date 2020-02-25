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
import com.saltedge.sca.sdk.TestTools;
import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.models.api.requests.CreateConnectionRequest;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.tools.DateTools;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;
import static com.saltedge.sca.sdk.controllers.ConnectionsController.CONNECTIONS_REQUEST_PATH;
import static com.saltedge.sca.sdk.tools.UrlTools.DEFAULT_AUTHENTICATOR_RETURN_TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ConnectionsController.class)
public class ConnectionsControllerIntegrationTests extends MockMvcTestAbs {
	@Test
	public void givenValidRequest_whenMakeCreateConnectionRequest_thenReturnOKAndRedirectToEnrollPage() throws Exception {
		//given
		ArgumentCaptor<ClientConnectionEntity> connectionCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);
		given(connectionsRepository.save(any(ClientConnectionEntity.class))).willReturn(testConnection);
		given(providerApi.getAuthorizationPageUrl("auth_token")).willReturn("https://localhost/admin/enroll?secret=auth_token");
		CreateConnectionRequest requestData = new CreateConnectionRequest(new CreateConnectionRequest.Data(
				publicKey,
				DEFAULT_AUTHENTICATOR_RETURN_TO,
				"android",
				"token",
				null
		));
		String json = new ObjectMapper().writeValueAsString(requestData);

		//when
		mvc.perform(post(CONNECTIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(json))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.connect_url", Matchers.is("https://localhost/admin/enroll?secret=auth_token")))
				.andExpect(jsonPath("$.data.id", Matchers.is("2")));

		//then
		Mockito.verify(connectionsRepository).save(connectionCaptor.capture());
		ClientConnection connection = connectionCaptor.getValue();
		assertThat(connection.getPublicKeyString()).isEqualTo(requestData.data.publicKey);
		assertThat(connection.getPushToken()).isEqualTo(requestData.data.pushToken);
		assertThat(connection.getPlatform()).isEqualTo(requestData.data.platform);
		assertThat(connection.getReturnUrl()).isEqualTo(requestData.data.returnUrl);
		assertThat(connection.getAccessToken()).isEmpty();
		assertThat(connection.getUserId()).isNull();
	}

	@Test
	public void givenValidRequestWithConnectQueryParam_whenMakeCreateConnectionRequest_thenReturnOKAndReturnTo() throws Exception {
		//given
		ArgumentCaptor<ClientConnectionEntity> connectionCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);
		given(providerApi.findUserIdByAuthorizationSessionSecret("connectQuery")).willReturn("1");
		given(connectionsRepository.save(any(ClientConnectionEntity.class))).willReturn(testAuthorizedConnection);
		CreateConnectionRequest requestData = new CreateConnectionRequest(new CreateConnectionRequest.Data(
				"key",
				"authenticator//connect",
				"android",
				"token",
				"connectQuery"
		));
		String json = new ObjectMapper().writeValueAsString(requestData);

		//when
		mvc.perform(post(CONNECTIONS_REQUEST_PATH)
			.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
			.content(json))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.connect_url", Matchers.startsWith(DEFAULT_AUTHENTICATOR_RETURN_TO)))
			.andExpect(jsonPath("$.data.id", Matchers.is("1")));

		//then
		Mockito.verify(connectionsRepository).save(connectionCaptor.capture());
		ClientConnection connection = connectionCaptor.getValue();
		assertThat(connection.getPublicKeyString()).isEqualTo(requestData.data.publicKey);
		assertThat(connection.getPushToken()).isEqualTo(requestData.data.pushToken);
		assertThat(connection.getPlatform()).isEqualTo(requestData.data.platform);
		assertThat(connection.getReturnUrl()).isEqualTo(requestData.data.returnUrl);
		assertThat(connection.getAccessToken()).isNotEmpty();
		assertThat(connection.getUserId()).isEqualTo("1");
	}

	@Test
	public void givenInvalidRequest_whenMakeCreateConnectionRequest_thenReturnBadRequest() throws Exception {
		//given
		CreateConnectionRequest requestData = new CreateConnectionRequest();

		//when
		mvc.perform(post(CONNECTIONS_REQUEST_PATH)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.content(new ObjectMapper().writeValueAsString(requestData)))

		//then
				.andExpect(status().isBadRequest());
	}

	@Test
	public void revokeConnectionTest_returnSuccess() throws Exception {
		//given
		String requestUrl = "http://localhost" + CONNECTIONS_REQUEST_PATH;
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));
		String signature = TestTools.createSignature(
				HttpMethod.DELETE.toString(),
				requestUrl,
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		given(connectionsRepository.findByAccessTokenAndRevokedFalse("access_token")).willReturn(testAuthorizedConnection);
		ArgumentCaptor<ClientConnectionEntity> connectionCaptor = ArgumentCaptor.forClass(ClientConnectionEntity.class);

		//when
		mvc.perform(delete(CONNECTIONS_REQUEST_PATH)
				.accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "access_token")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature)
		)

		//then
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.access_token", Matchers.is("access_token")))
				.andExpect(jsonPath("$.data.success", Matchers.is(true)));


		Mockito.verify(connectionsRepository).save(connectionCaptor.capture());
		assertThat(connectionCaptor.getValue().getRevoked()).isTrue();
	}
}
