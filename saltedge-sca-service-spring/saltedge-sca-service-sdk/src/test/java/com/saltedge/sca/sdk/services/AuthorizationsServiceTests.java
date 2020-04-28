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
import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthorizationsServiceTests extends MockServiceTestAbs {
	@Autowired
	private AuthorizationsService testService;
	@MockBean
	private AuthorizationsRepository testRepository;
	@MockBean
	private ClientNotificationService notificationService;

	@Test
	public void givenInvalidParams_whenCreateAuthorization_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.createAuthorization(null, null, null, null));
		assertThrows(ConstraintViolationException.class, () -> testService.createAuthorization("", "", "", ""));
	}

	@Test
	public void givenValidParams_whenCreateAuthorization_thenSaveAndReturnNewAuthorization_AndSendNotification() {
		//given
		AuthorizationEntity savedEntity = new AuthorizationEntity();
		savedEntity.setId(1L);
		given(testRepository.save(any(AuthorizationEntity.class))).willReturn(savedEntity);

		//when
		testService.createAuthorization("1", "code", "test title", "test desc");

		//then
		ArgumentCaptor<AuthorizationEntity> entityCaptor = ArgumentCaptor.forClass(AuthorizationEntity.class);
		verify(testRepository).save(entityCaptor.capture());
		verify(notificationService).sendNotificationsForUser("1", savedEntity);

		assertThat(entityCaptor.getValue().getExpiresAt()).isAfter(Instant.now());
		assertThat(entityCaptor.getValue().getConfirmed()).isNull();
		assertThat(entityCaptor.getValue().getTitle()).isEqualTo("test title");
		assertThat(entityCaptor.getValue().getDescription()).isEqualTo("test desc");
		assertThat(entityCaptor.getValue().getAuthorizationCode()).isEqualTo("code");
		assertThat(entityCaptor.getValue().getUserId()).isEqualTo("1");
	}

	@Test
	public void givenInvalidParams_whenGetAuthorizations_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.getAllAuthorizations(null));
		assertThrows(ConstraintViolationException.class, () -> testService.getAllAuthorizations(""));
	}

	@Test
	public void givenValidParams_whenGetAuthorizations_thenReturnListOfAuthorizations() {
		//given
		AuthorizationEntity savedEntity = new AuthorizationEntity();
		savedEntity.setId(1L);
		given(testRepository.findByUserId("1")).willReturn(Lists.list(savedEntity));

		//when
		List<Authorization> result = testService.getAllAuthorizations("1");

		//then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getId()).isEqualTo(1L);
	}
}
