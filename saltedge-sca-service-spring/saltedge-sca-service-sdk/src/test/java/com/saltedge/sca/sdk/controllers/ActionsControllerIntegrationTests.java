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
import com.saltedge.sca.sdk.TestTools;
import com.saltedge.sca.sdk.models.api.responces.ScaActionResponse;
import com.saltedge.sca.sdk.tools.DateTools;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;
import static com.saltedge.sca.sdk.controllers.ActionsController.ACTIONS_REQUEST_PATH;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ActionsController.class)
public class ActionsControllerIntegrationTests extends MockMvcTestAbs {
	@Test
	public void putActionTest_returnSuccess() throws Exception {
		ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
		given(connectionsRepository.findByAccessTokenAndRevokedFalse("accessToken")).willReturn(testAuthorizedConnection);
		given(actionsAuthenticateService.onNewAuthenticatedAction("123", testAuthorizedConnection)).willReturn(new ScaActionResponse(true, null, null));
		String expiresAt = String.valueOf((DateTools.nowUtcSeconds() + 60));
		String signature = TestTools.createSignature(
				"put",
				"http://localhost" + ACTIONS_REQUEST_PATH + "/123",
				expiresAt,
				"",
				TestTools.getRsaPrivateKey()
		);

		mvc.perform(MockMvcRequestBuilders.put(ACTIONS_REQUEST_PATH + "/123").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
				.header(HEADER_KEY_ACCESS_TOKEN, "accessToken")
				.header(HEADER_KEY_EXPIRES_AT, expiresAt)
				.header(HEADER_KEY_SIGNATURE, signature))

				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.success", Matchers.is(true)));
	}
}
