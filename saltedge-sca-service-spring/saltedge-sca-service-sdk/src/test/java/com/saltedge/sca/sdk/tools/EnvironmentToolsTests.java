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
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@TestPropertySource(properties = {
		"sca_service.url = http://sca.host.org",
		"sca_push_service.url = http://push.host.org",
		"sca_push_service.app_id = ApplicationID",
		"sca_push_service.app_secret = ApplicationSECRET"
})
public class EnvironmentToolsTests {
	@Autowired
	private Environment env;

	@Test
	public void getScaServiceUrlTest() {
		assertThat(EnvironmentTools.getScaServiceUrl(env)).isEqualTo("http://sca.host.org");
	}

	@Test
	public void getPushServiceUrlTest() {
		assertThat(EnvironmentTools.getPushServiceUrl(env)).isEqualTo("http://push.host.org");
	}

	@Test
	public void getPushAppIdTest() {
		assertThat(EnvironmentTools.getPushAppId(env)).isEqualTo("ApplicationID");
	}

	@Test
	public void getPushAppSecret() {
		assertThat(EnvironmentTools.getPushAppSecret(env)).isEqualTo("ApplicationSECRET");
	}
}
