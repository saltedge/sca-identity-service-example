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

import com.google.common.collect.ImmutableList;
import com.saltedge.sca.sdk.errors.BadRequest;
import com.saltedge.sca.sdk.errors.NotFound;
import com.saltedge.sca.sdk.models.AuthenticateAction;
import com.saltedge.sca.sdk.models.api.responces.ActionResponse;
import com.saltedge.sca.sdk.models.persistent.*;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActionsServiceTests {
	@Autowired
	private ActionsService testService;
	@MockBean
	private AuthorizationsRepository authorizationsRepository;
	@MockBean
	private AuthenticateActionsRepository actionsRepository;
	@MockBean
	private ClientNotificationService clientNotificationService;
	@MockBean
	private ServiceProvider providerApi;

	@Test
	public void givenInvalidParams_whenOnNewAuthenticatedAction_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.onNewAuthenticatedAction(null, null));
		assertThrows(ConstraintViolationException.class, () -> testService.onNewAuthenticatedAction("", null));
	}

	@Test
	public void givenRequiredEntityInDb_whenOnNewAuthenticatedAction_thenThrowActionNotFoundException() {
		//given
		given(actionsRepository.findFirstByUuid("id1")).willReturn(null);

		//then
		assertThrows(NotFound.ActionNotFound.class, () -> {
			//when
			testService.onNewAuthenticatedAction("id1", new ClientConnectionEntity());
		});
	}

	@Test
	public void givenExpiredAction_whenOnNewAuthenticatedAction_thenThrowActionExpiredException() {
		//given
		AuthenticateActionEntity entity = new AuthenticateActionEntity();
		entity.setExpiresAt(LocalDateTime.MIN);
		given(actionsRepository.findFirstByUuid("id1")).willReturn(entity);

		//then
		assertThrows(BadRequest.ActionExpired.class, () -> {
			//when
			testService.onNewAuthenticatedAction("id1", new ClientConnectionEntity());
		});
	}

	@Test
	public void givenValidAction_whenOnNewAuthenticatedAction_thenReturnSuccessWithoutSca() {
		//given
		ClientConnectionEntity connection = new ClientConnectionEntity();
		connection.setUserId("user1");
		AuthenticateActionEntity action = new AuthenticateActionEntity();
		action.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		action.setRequireSca(false);
		given(actionsRepository.findFirstByUuid("action1")).willReturn(action);

		assertThat(action.getUserId()).isNull();

		//when
		ActionResponse result = testService.onNewAuthenticatedAction("action1", connection);

		//then
		ArgumentCaptor<AuthenticateActionEntity> captor = ArgumentCaptor.forClass(AuthenticateActionEntity.class);
		verify(actionsRepository).save(captor.capture());
		assertThat(captor.getValue().getUserId()).isEqualTo("user1");
		assertThat(result).isEqualTo(new ActionResponse(true, null, null));
	}

	@Test
	public void givenValidAction_whenOnNewAuthenticatedAction_thenReturnSuccessAndCreateDefaultAuthorization() {
		//given
		ClientConnectionEntity connection = new ClientConnectionEntity();
		connection.setId(1L);
		connection.setUserId("user1");

		AuthorizationEntity savedAuthorization = new AuthorizationEntity("titleValue", "descriptionValue", LocalDateTime.now().plusMinutes(5), "authorizationCode", "user1");
		savedAuthorization.setId(2L);
		given(authorizationsRepository.save(any(AuthorizationEntity.class))).willReturn(savedAuthorization);

		AuthenticateActionEntity savedAction = new AuthenticateActionEntity();
		savedAction.setExpiresAt(LocalDateTime.now().plusMinutes(5));
		savedAction.setRequireSca(true);
		given(actionsRepository.findFirstByUuid("action1")).willReturn(savedAction);

		assertThat(savedAction.getUserId()).isNull();

		//when
		ActionResponse result = testService.onNewAuthenticatedAction("action1", connection);

		//then
		ArgumentCaptor<AuthenticateActionEntity> actionCaptor = ArgumentCaptor.forClass(AuthenticateActionEntity.class);
		verify(actionsRepository).save(actionCaptor.capture());
		assertThat(actionCaptor.getValue().getUserId()).isEqualTo("user1");

		ArgumentCaptor<AuthorizationEntity> authorizationCaptor = ArgumentCaptor.forClass(AuthorizationEntity.class);
		verify(authorizationsRepository).save(authorizationCaptor.capture());
		AuthorizationEntity newAuthorization = authorizationCaptor.getValue();
		assertThat(newAuthorization.getUserId()).isEqualTo("user1");
		assertThat(newAuthorization.getTitle()).isEqualTo("Authorization Request");
		assertThat(newAuthorization.getDescription()).isEqualTo("Confirm your identity");
		assertThat(newAuthorization.getExpiresAt()).isAfter(LocalDateTime.now());

		verify(clientNotificationService).sendNotificationForConnections(ImmutableList.of(connection), savedAuthorization);
		assertThat(result).isEqualTo(new ActionResponse(true, "1", "2"));
	}

	@Test
	public void givenInvalidActionCode_whenOnCreateAction_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.createAction(""));
	}

	@Test
	public void givenActionCode_whenGetActionByUUID_thenSaveNewActionWithCodeAndNewUUID() {
		//given
		AuthenticateActionEntity action = new AuthenticateActionEntity();
		action.setCode("test_code");
		action.setUuid("action1");
		given(actionsRepository.findFirstByUuid("action1")).willReturn(action);

		//when
		AuthenticateAction result = testService.getActionByUUID("action1");

		//then
		assertThat(result.getCode()).isEqualTo("test_code");
		assertThat(result.getUUID()).isEqualTo("action1");
	}

	@Test
	public void givenInvalidUUID_whenGetActionByUUID_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.getActionByUUID(""));
	}
}
