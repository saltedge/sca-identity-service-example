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
package com.saltedge.sca.sdk.services;

import com.saltedge.sca.sdk.models.UserIdentity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = {
		"sca_service.url = http://sca.host.org",
		"sca_push_service.url = http://push.host.org",
		"sca_push_service.app_id = ApplicationID",
		"sca_push_service.app_secret = ApplicationSECRET"
})
public class ScaSdkServiceTests {
	@Autowired
	private ScaSdkService testService;
	@MockBean
	private ServiceProvider serviceProvider;
	@MockBean
	private AuthorizationsService authorizationsService;
	@MockBean
	private ClientConnectionsService connectionsService;
	@MockBean
	private AuthenticateActionsService actionsService;

	@Test
	public void givenConnectionsService_whenGetClientConnections_thenReturnListOfConnections() {
		//given
		// ClientConnectionsService

		//when
		testService.getClientConnections("1");

		//then
		verify(connectionsService).getConnections("1");
	}

	@Test
	public void givenEmptyConnectSecret_whenCreateConnectAppLink_thenReturnAppLink() {
		//given
		// Test properties

		//when
		String result = testService.createConnectAppLink("");

		//then
		assertThat(result).isEqualTo("authenticator://saltedge.com/connect?configuration=http://sca.host.org/api/authenticator/v1/configuration");
	}

	@Test
	public void givenConnectSecret_whenCreateConnectAppLink_thenReturnAppLinkWithConnectQuery() {
		//given
		// Test properties

		//when
		String result = testService.createConnectAppLink("secret");

		//then
		assertThat(result).isEqualTo("authenticator://saltedge.com/connect?configuration=http://sca.host.org/api/authenticator/v1/configuration&connect_query=secret");
	}

	@Test
	public void givenInvalidActionUUID_whenCreateActionAppLink_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.createAuthenticateActionAppLink(""));
	}

	@Test
	public void givenInvalidActionUUID_whenCreateActionAppLink_thenReturnAppLinkWithConnectQuery() {
		//given
		// Test properties

		//when
		String result = testService.createAuthenticateActionAppLink("123");

		//then
		assertThat(result).isEqualTo("authenticator://saltedge.com/action?action_uuid=123&connect_url=http://sca.host.org");
	}

	@Test
	public void givenConnectionsService_whenCreateAuthorization_thenCreateAuthorization() {
		//given
		// AuthorizationsService

		//when
		testService.createAuthorization("1", "code", "test title", "test desc");

		//then
		verify(authorizationsService).createAuthorization("1", "code", "test title", "test desc");
	}

	@Test
	public void givenConnectionsService_whenGetAuthorizations_thenReturnListOfAuthorizations() {
		//given
		// AuthorizationsService

		//when
		testService.getAllAuthorizations("1");

		//then
		verify(authorizationsService).getAllAuthorizations("1");
	}

	@Test
	public void givenNoConnectionBySecret_whenOnUserAuthenticationSuccess_thenReturnError() {
		//given
		UserIdentity identity = new UserIdentity("1");
		given(connectionsService.authenticateConnection("secret", identity)).willReturn(null);

		//when
		String result = testService.onUserAuthenticationSuccess("secret", "1", "accessToken", null);

		//then
		assertThat(result).isEqualTo("authenticator://oauth/redirect?error_class=AUTH_SESSION_MISSING&error_message=Authentication%20session%20is%20missing.");
	}

	@Test
	public void givenConnectionWithExpiredAuthSession_whenOnUserAuthenticationSuccess_thenReturnError() {
		//given
		UserIdentity identity = new UserIdentity("1");
		ClientConnectionEntity initialConnection = new ClientConnectionEntity();
		initialConnection.setAuthTokenExpiresAt(LocalDateTime.MIN);
		initialConnection.setReturnUrl("my-app://return");
		given(connectionsService.authenticateConnection("secret", identity)).willReturn(initialConnection);

		//when
		String result = testService.onUserAuthenticationSuccess("secret", "1", null, null);

		//then
		assertThat(result).isEqualTo("my-app://return?error_class=AUTH_SESSION_EXPIRED&error_message=Authentication%20Session%20is%20expired.");
	}

	@Test
	public void givenAuthenticatedConnection_whenOnUserAuthenticationSuccess_thenReturnSuccessReturnToUrl() {
		//given
		UserIdentity identity = new UserIdentity("1");
		ClientConnectionEntity initialConnection = new ClientConnectionEntity();
		initialConnection.setAuthTokenExpiresAt(LocalDateTime.MAX);
		initialConnection.setId(1L);
		initialConnection.setReturnUrl("my-app://return");
		initialConnection.setAccessToken("accessToken");
		given(connectionsService.authenticateConnection("secret", identity)).willReturn(initialConnection);

		//when
		String result = testService.onUserAuthenticationSuccess("secret", "1", null, null);

		//then
		assertThat(result).isEqualTo("my-app://return?id=1&access_token=accessToken");
	}

	@Test
	public void givenNoConnectionBySecret_whenOnUserAuthenticationFail_thenReturnFailRedirectToDefaultUrl() {
		//given
		given(connectionsService.getConnectionReturnUrl("secret")).willReturn(null);

		//when
		String result = testService.onUserAuthenticationFail("secret", "test error");

		//then
		assertThat(result).isEqualTo("authenticator://oauth/redirect?error_class=AUTHENTICATION_FAILED&error_message=test%20error");
	}

	@Test
	public void givenConnectionBySecret_whenOnUserAuthenticationFail_thenReturnFailRedirectToDefaultUrl() {
		//given
		given(connectionsService.getConnectionReturnUrl("secret")).willReturn("my-app://return");

		//when
		String result = testService.onUserAuthenticationFail("secret", "test error");

		//then
		assertThat(result).isEqualTo("my-app://return?error_class=AUTHENTICATION_FAILED&error_message=test%20error");
	}

	@Test
	public void givenActionsService_whenCreateAction_thenCallCreateAction() {
		//given
		// ActionsService

		//when
		testService.createAction("action_code", "uuid", null);

		//then
		verify(actionsService).createAction("action_code", "uuid", null);
	}

	@Test
	public void givenActionsService_whenGetActionByUUID_thenCallGetActionByUUID() {
		//given
		// ActionsService

		//when
		testService.getActionByUUID("action_uuid");

		//then
		verify(actionsService).getActionByUUID("action_uuid");
	}
}
