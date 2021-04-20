/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2021 Salt Edge Inc.
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
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AuthorizationsConfirmServiceTests extends MockServiceTestAbs {
	@Autowired
	private AuthorizationsConfirmService testService;
	@MockBean
	private AuthorizationsRepository authorizationsRepository;

	private final ClientConnectionEntity testConnection = createAuthenticatedConnection();

	@Before
	public void setUp() throws Exception {
		AuthorizationEntity testAuthorization = new AuthorizationEntity();
		testAuthorization.setId(1L);
		testAuthorization.setUserId(String.valueOf(1));
		testAuthorization.setAuthorizationCode("123456");

		given(authorizationsRepository.findFirstByIdAndUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
			eq(1L),
			eq("1"),
			any(Instant.class))
		).willReturn(testAuthorization);
	}

	@Test
	public void givenInvalidParams_whenConfirmAuthorization_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.confirmAuthorization(null, null, null, null, null, null));
	}

	@Test
	public void givenValidParams_whenConfirmAuthorization_thenSaveAndReturnTrue() {
		//given

		//when
		boolean result = testService.confirmAuthorization(testConnection, 1L, "123456", true, "GEO:52.506931;13.144558", "biometrics");

		//then
		assertThat(result).isTrue();

		verify(authorizationsRepository).findFirstByIdAndUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
			eq(1L),
			eq("1"),
			any(Instant.class)
		);
		ArgumentCaptor<AuthorizationEntity> entityCaptor = ArgumentCaptor.forClass(AuthorizationEntity.class);
		verify(authorizationsRepository).save(entityCaptor.capture());

		assertThat(entityCaptor.getValue().getConfirmed()).isTrue();
		assertThat(entityCaptor.getValue().getAuthorizationCode()).isEqualTo("123456");
		assertThat(entityCaptor.getValue().getUserId()).isEqualTo("1");
		assertThat(entityCaptor.getValue().getConfirmLocation()).isEqualTo("GEO:52.506931;13.144558");
		assertThat(entityCaptor.getValue().getConfirmAuthorizationType()).isEqualTo("biometrics");

		verify(serviceProvider).onAuthorizationConfirmed(entityCaptor.getValue());
	}

	@Test
	public void givenInvalidParams_whenConfirmAuthorization_thenReturnFalse() {
		//given

		//when
		boolean result = testService.confirmAuthorization(testConnection, 1L, "64321", true, "GEO:52.506931;13.144558", "biometrics");

		//then
		assertThat(result).isFalse();

		verify(authorizationsRepository).findFirstByIdAndUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
			eq(1L),
			eq("1"),
			any(Instant.class)
		);
		verifyNoMoreInteractions(authorizationsRepository);
		verifyNoInteractions(serviceProvider);
	}
}
