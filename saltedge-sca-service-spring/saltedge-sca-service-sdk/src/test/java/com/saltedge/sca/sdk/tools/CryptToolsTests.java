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

import static org.assertj.core.api.Assertions.assertThat;

public class CryptToolsTests {
	@Test
	public void encryptRsaTest() throws Exception {
		PublicKey publicKey = TestTools.getRsaPublicKey();
		PrivateKey privateKey = TestTools.getRsaPrivateKey();
		String data = "{\"data\": 1}";

		byte[] encryptedData = CryptTools.encryptRsa(data.getBytes(), publicKey);
		String decryptedData = new String(CryptTools.decryptRsa(encryptedData, privateKey));

		assertThat(decryptedData).isEqualTo(data);
	}

	@Test
	public void encryptAesTest() throws Exception {
		String data = "{\"data\": 1}";
		byte[] aesKey = new byte[]{
				65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45,
				65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45
		};
		byte[] aesIV = new byte[]{65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45};

		byte[] encryptedData = CryptTools.encryptAes(data, aesKey, aesIV);

		String decryptedData = new String(CryptTools.decryptAes(encryptedData, aesKey, aesIV));

		assertThat(decryptedData).isEqualTo(data);
	}
}
