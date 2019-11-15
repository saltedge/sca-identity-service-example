/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.identity.tools

import com.saltedge.authenticator.identity.TestTools
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.security.SecureRandom
import java.util.*

class SignToolsTests {

	private fun generate(size: Int): String {
		val key = ByteArray(size)
		SecureRandom().nextBytes(key)
		return Base64.getEncoder().encodeToString(key).take(size)
	}

	@Test
	fun verifyTest1() {
		val publicKey = TestTools.rsaPublicKey
		val privateKey = TestTools.rsaPrivateKey
		val data = "POST|https://example.org:80/user_id/|1234567890|{\"data\": 1}"
		val signature = TestTools.createSignature(data = data, privateKey = privateKey)

		assertThat(
				SignTools.verify(
						signature,
						requestMethod = "POST",// should be lowercase
						requestUrl = "https://example.org:80/user_id/",
						expiresAt = "1234567890",
						requestBody = "{\"data\": 1}",
						publicKey = publicKey)
		).isFalse()
	}

	@Test
	fun verifyTest2() {
		val publicKey = TestTools.rsaPublicKey
		val privateKey = TestTools.rsaPrivateKey
		val data = "POST|https://example.org:80/user_id/|1234567890|{\"data\": 1}"
		val signature = TestTools.createSignature(data = data, privateKey = privateKey)

		assertThat(SignTools.verify(signature = signature, data = data, publicKey = publicKey)).isTrue()
	}
}
