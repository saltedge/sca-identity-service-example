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

import com.saltedge.sca.sdk.TestTools;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class SignToolsTests {
	@Test
	public void verifyTest1() throws Exception {
		PublicKey publicKey = TestTools.getRsaPublicKey();
		PrivateKey privateKey = TestTools.getRsaPrivateKey();
		String data = "POST|https://example.org:80/user_id/|1234567890|{\"data\": 1}";
		String signature = TestTools.createSignature(data, privateKey);

		assertThat(
				SignTools.verify(
						signature,
						"POST",// should be lowercase
						"https://example.org:80/user_id/",
						"1234567890",
						"{\"data\": 1}",
						publicKey)
		).isFalse();
	}

	@Test
	public void verifyTest2() throws Exception {
		PublicKey publicKey = TestTools.getRsaPublicKey();
		PrivateKey privateKey = TestTools.getRsaPrivateKey();
		String data = "POST|https://example.org:80/user_id/|1234567890|{\"data\": 1}";
		String signature = TestTools.createSignature(data, privateKey);

		assertThat(SignTools.verify(signature, data, publicKey)).isTrue();
	}

	private String generate(int size) {
		byte[] key = new byte[size];
		new SecureRandom().nextBytes(key);
		return Base64.getEncoder().encodeToString(key).substring(0, size);
	}
}
