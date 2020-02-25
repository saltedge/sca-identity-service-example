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
package com.saltedge.sca.sdk.tools;

import org.junit.Test;

import static com.saltedge.sca.sdk.tools.UrlTools.DEFAULT_AUTHENTICATOR_RETURN_TO;
import static org.assertj.core.api.Assertions.assertThat;

public class UrlTests {
	@Test
	public void createUserAuthSuccessUrlTest() {
		String result = UrlTools.createUserAuthSuccessUrl(null, null, null);

		assertThat(result).isEqualTo("authenticator://oauth/redirect");

		result = UrlTools.createUserAuthSuccessUrl("", null, null);

		assertThat(result).isEqualTo("authenticator://oauth/redirect");

		result = UrlTools.createUserAuthSuccessUrl("http://callback.host.org", null, null);

		assertThat(result).isEqualTo("http://callback.host.org");

		result = UrlTools.createUserAuthSuccessUrl("http://callback.host.org", "1", null);

		assertThat(result).isEqualTo("http://callback.host.org");

		result = UrlTools.createUserAuthSuccessUrl("http://callback.host.org", "1", "");

		assertThat(result).isEqualTo("http://callback.host.org");

		result = UrlTools.createUserAuthSuccessUrl("http://callback.host.org", null, "token");

		assertThat(result).isEqualTo("http://callback.host.org");

		result = UrlTools.createUserAuthSuccessUrl("http://callback.host.org", "", "token");

		assertThat(result).isEqualTo("http://callback.host.org");

		result = UrlTools.createUserAuthSuccessUrl("http://callback.host.org", "1", "token");

		assertThat(result).isEqualTo("http://callback.host.org?id=1&access_token=token");
	}

	@Test
	public void createUserAuthErrorUrlTest() {
		String result = UrlTools.createUserAuthErrorUrl(null, null, null);

		assertThat(result).isEqualTo("authenticator://oauth/redirect?error_class=UnknownError&error_message=");

		result = UrlTools.createUserAuthErrorUrl(DEFAULT_AUTHENTICATOR_RETURN_TO, "AnyError", "Any error");

		assertThat(result).isEqualTo("authenticator://oauth/redirect?error_class=AnyError&error_message=Any%20error");
	}
}
