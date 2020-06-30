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
import com.saltedge.sca.sdk.models.api.ScaConsent;
import com.saltedge.sca.sdk.models.api.ScaConsentSharedData;
import com.saltedge.sca.sdk.models.api.ScaEncryptedEntity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentsServiceTests extends MockServiceTestAbs {
	@Autowired
	private ConsentsService testService;

	@Test
	public void givenInvalidParams_whenGetActiveConsents_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.getActiveConsents(null));
	}

	@Test
	public void givenValidParams_whenGetActiveConsents_thenReturnEncryptedEntitiesList() {
		//given
		ClientConnectionEntity connection = createAuthenticatedConnection();
		ScaConsent model = new ScaConsent(
				"id1",
				"userId",
				Instant.parse("2020-01-01T00:00:00Z"),
				Instant.parse("2020-03-01T00:00:00Z"),
				"tpp name",
				Lists.list(),
				new ScaConsentSharedData(true, true)
		);
		given(serviceProvider.getActiveConsents(String.valueOf(connection.getId()))).willReturn(Lists.list(model));

		//when
		List<ScaEncryptedEntity> result = testService.getActiveConsents(connection);

		//then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).connectionId).isEqualTo(connection.getId().toString());
		assertThat(result.get(0).id).isEqualTo("id1");
	}

	@Test
	public void givenInvalidParams_whenRevokeConsent_thenThrowConstraintViolationException() {
		assertThrows(ConstraintViolationException.class, () -> testService.revokeConsent("", null));
	}

	@Test
	public void givenValidParams_whenRevokeConsent_thenReturnEncryptedEntitiesList() {
		//given
		ClientConnectionEntity connection = createAuthenticatedConnection();
		given(serviceProvider.revokeConsent(connection.getUserId(), "id1")).willReturn(true);

		//when
		boolean result = testService.revokeConsent("id1", connection);

		//then
		assertThat(result).isTrue();
	}
}
