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

import static org.assertj.core.api.Assertions.assertThat;

public class CodeBuilderTests {
	@Test
	public void generateRandomStringTest() throws Exception {
		String resultString = CodeBuilder.generateRandomString();

		assertThat(resultString).hasSize(32).isNotBlank();
	}

	@Test
	public void generateRandomStringWithSizeTest() throws Exception {
		String resultString = CodeBuilder.generateRandomString(16);

		assertThat(resultString).hasSize(16).isNotBlank();
	}

	@Test
	public void generatePaymentAuthorizationCodeTest() throws Exception {
		String resultString = CodeBuilder.generatePaymentAuthorizationCode(
				"Amazon Prime",
				"100.10 USD",
				123456789L,
				"1",
				"Payment for Amazon Kindle"
		);

		assertThat(resultString).isNotBlank();
	}

	@Test
	public void generateAuthorizationCodeTest() throws Exception {
		String resultString = CodeBuilder.generateAuthorizationCode(
				"1",
				"Authentication Request",
				"Salt Edge Authenticator authentication request",
				123456789L
		);

		assertThat(resultString).isNotBlank();
	}
}
