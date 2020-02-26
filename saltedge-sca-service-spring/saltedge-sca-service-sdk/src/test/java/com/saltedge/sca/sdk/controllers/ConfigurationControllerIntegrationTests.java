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
package com.saltedge.sca.sdk.controllers;

import com.saltedge.sca.sdk.MockMvcTestAbs;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.saltedge.sca.sdk.controllers.ConfigurationController.CONFIGURATION_REQUEST_PATH;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ConfigurationController.class)
public class ConfigurationControllerIntegrationTests extends MockMvcTestAbs {
	@Test
	public void getConfigurationTest() throws Exception {
		given(providerApi.getProviderCode()).willReturn("spring-demobank");
		given(providerApi.getProviderName()).willReturn("Spring Demobank");
		given(providerApi.getProviderLogoUrl()).willReturn("");
		given(providerApi.getProviderSupportEmail()).willReturn("support@spring-demobank.com");

		mvc.perform(MockMvcRequestBuilders.get(CONFIGURATION_REQUEST_PATH))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.connect_url", Matchers.is("https://your_host.org")))
				.andExpect(jsonPath("$.data.code", Matchers.is("spring-demobank")))
				.andExpect(jsonPath("$.data.name", Matchers.is("Spring Demobank")))
				.andExpect(jsonPath("$.data.logo_url", Matchers.is("")))
				.andExpect(jsonPath("$.data.support_email", Matchers.is("support@spring-demobank.com")))
				.andExpect(jsonPath("$.data.version", Matchers.is("1")));
	}
}
