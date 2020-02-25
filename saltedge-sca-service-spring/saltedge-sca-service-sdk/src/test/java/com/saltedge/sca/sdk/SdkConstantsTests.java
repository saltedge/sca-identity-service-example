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
package com.saltedge.sca.sdk;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SdkConstantsTests {
	@Test
	public void baseValuesTest() {
		assertThat(ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH).isEqualTo("/api/authenticator/v1");
		assertThat(ScaSdkConstants.APP_LINK_PREFIX_CONNECT).isEqualTo("authenticator://saltedge.com/connect");
		assertThat(ScaSdkConstants.APP_LINK_PREFIX_ACTION).isEqualTo("authenticator://saltedge.com/action");
		assertThat(ScaSdkConstants.AUTHORIZATION_DEFAULT_LIFETIME_MINUTES).isEqualTo(5);
		assertThat(ScaSdkConstants.CONNECTION_DEFAULT_AUTH_SESSION_MINUTES).isEqualTo(5);
		assertThat(ScaSdkConstants.CONNECTION_DEFAULT_ACCESS_TOKEN_MINUTES).isEqualTo(24 * 60);
	}
}
