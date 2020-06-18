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
import com.saltedge.sca.sdk.models.AuthenticateAction;
import com.saltedge.sca.sdk.models.persistent.AuthenticateActionEntity;
import com.saltedge.sca.sdk.models.persistent.AuthenticateActionsRepository;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ActionsServiceTests extends MockServiceTestAbs {
	@Autowired
	private ActionsService testService;
	@MockBean
	private AuthorizationsRepository authorizationsRepository;
	@MockBean
	private AuthenticateActionsRepository actionsRepository;
	@MockBean
	private ClientNotificationService clientNotificationService;

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
