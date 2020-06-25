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
import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.api.ScaConsent;
import com.saltedge.sca.sdk.models.api.ScaEncryptedEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class EncryptedEntityFactoryTests {
	@Test
	public void encryptAuthorizationTest() throws Exception {
		Authorization model = new AuthorizationEntity(
				"title",
				"description",
				Instant.parse("2020-03-01T00:00:00Z"),
				"authorizationCode",
				"userId"
		);
		model.setCreatedAt(Instant.parse("2020-01-01T00:00:00Z"));
		PublicKey publicKey = TestTools.getRsaPublicKey();
		PrivateKey privateKey = TestTools.getRsaPrivateKey();

		ScaEncryptedEntity encryptedObject = EncryptedEntityFactory.encryptAuthorization(model, 1L, publicKey);

		byte[] key = CryptTools.decryptRsa(Base64.getDecoder().decode(Objects.requireNonNull(encryptedObject).key), privateKey);
		byte[] iv = CryptTools.decryptRsa(Base64.getDecoder().decode(encryptedObject.iv), privateKey);

		assertThat(key.length).isEqualTo(32);
		assertThat(iv.length).isEqualTo(16);

		String decryptedData = new String(CryptTools.decryptAes(Base64.getDecoder().decode(encryptedObject.data), key, iv));

		assertThat(decryptedData).isEqualTo("{\"expires_at\":\"2020-03-01T00:00:00Z\",\"connection_id\":\"1\",\"authorization_code\":\"authorizationCode\",\"description\":\"description\",\"created_at\":\"2020-01-01T00:00:00Z\",\"id\":\"null\",\"title\":\"title\"}");
	}

	@Test
	public void encryptConsentTest() throws Exception {
		ScaConsent model = new ScaConsent(
				"id1",
				"userId",
				Instant.parse("2020-01-01T00:00:00Z"),
				Instant.parse("2020-03-01T00:00:00Z"),
				"tpp name",
				Lists.list()
		);
		PublicKey publicKey = TestTools.getRsaPublicKey();
		PrivateKey privateKey = TestTools.getRsaPrivateKey();

		ScaEncryptedEntity encryptedObject = EncryptedEntityFactory.encryptConsent(model, 1L, publicKey);

		byte[] key = CryptTools.decryptRsa(Base64.getDecoder().decode(encryptedObject.key), privateKey);
		byte[] iv = CryptTools.decryptRsa(Base64.getDecoder().decode(encryptedObject.iv), privateKey);

		assertThat(key.length).isEqualTo(32);
		assertThat(iv.length).isEqualTo(16);

		String decryptedData = new String(CryptTools.decryptAes(Base64.getDecoder().decode(encryptedObject.data), key, iv));

		assertThat(decryptedData).isEqualTo("{\"id\":\"id1\",\"user_id\":\"userId\",\"created_at\":\"2020-01-01T00:00:00Z\",\"expires_at\":\"2020-03-01T00:00:00Z\",\"consent_type\":\"aisp\",\"tpp_name\":\"tpp name\",\"accounts\":[]}");
	}
}
