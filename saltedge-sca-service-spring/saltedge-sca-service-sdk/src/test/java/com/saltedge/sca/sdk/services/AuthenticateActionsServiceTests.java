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

import com.saltedge.sca.sdk.MockServiceTestAbs;
import com.saltedge.sca.sdk.errors.BadRequest;
import com.saltedge.sca.sdk.errors.NotFound;
import com.saltedge.sca.sdk.models.AuthenticateAction;
import com.saltedge.sca.sdk.models.api.responces.ActionResponse;
import com.saltedge.sca.sdk.models.persistent.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthenticateActionsServiceTests extends MockServiceTestAbs {
	@Autowired
	private ActionsService testService;
	@MockBean
	private AuthorizationsRepository authorizationsRepository;
	@MockBean
	private AuthenticateActionsRepository actionsRepository;
	@MockBean
	private ClientNotificationService clientNotificationService;

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
		entity.setExpiresAt(Instant.MIN);
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
		action.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
		given(actionsRepository.findFirstByUuid("action1")).willReturn(action);
		given(serviceProvider.onAuthenticateAction(any(AuthenticateAction.class))).willReturn(null);

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
	public void givenValidAction_whenOnNewAuthenticatedAction_thenReturnWithoutScaIds() {
		//given
		ClientConnectionEntity connection = new ClientConnectionEntity();
		connection.setId(1L);
		connection.setUserId("user1");

		AuthorizationEntity savedAuthorization = new AuthorizationEntity(
				"titleValue",
				"descriptionValue",
				Instant.now().plus(5, ChronoUnit.MINUTES),
				"authorizationCode",
				"user1"
		);
		savedAuthorization.setId(2L);
		given(authorizationsRepository.save(any(AuthorizationEntity.class))).willReturn(savedAuthorization);

		AuthenticateActionEntity savedAction = new AuthenticateActionEntity();
		savedAction.setExpiresAt(Instant.now().plus(5, ChronoUnit.MINUTES));
		given(actionsRepository.findFirstByUuid("action1")).willReturn(savedAction);
		given(serviceProvider.onAuthenticateAction(any(AuthenticateAction.class))).willReturn(10L);

		assertThat(savedAction.getUserId()).isNull();

		//when
		ActionResponse result = testService.onNewAuthenticatedAction("action1", connection);

		//then
		ArgumentCaptor<AuthenticateActionEntity> actionCaptor = ArgumentCaptor.forClass(AuthenticateActionEntity.class);
		verify(actionsRepository).save(actionCaptor.capture());
		assertThat(actionCaptor.getValue().getUserId()).isEqualTo("user1");
		assertThat(result).isEqualTo(new ActionResponse(true, "1", "10"));
	}

	@Test
	public void givenInvalidActionCode_whenCreateAction_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.createAction("", "", null));
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
