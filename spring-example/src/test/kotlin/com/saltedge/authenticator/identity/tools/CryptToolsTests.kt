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
import java.security.PrivateKey
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptToolsTests {

	@Test
	fun encryptTest() {
		val publicKey = TestTools.rsaPublicKey!!
		val privateKey = TestTools.rsaPrivateKey!!
		val data = "{\"data\": 1}"

		val encryptedObject = CryptTools.encrypt(data = data, publicKey = publicKey)

		val key = decryptRsa(Base64.getDecoder().decode(encryptedObject.key), privateKey)
		val iv = decryptRsa(Base64.getDecoder().decode(encryptedObject.iv), privateKey)

		assertThat(key.size).isEqualTo(32)
		assertThat(iv.size).isEqualTo(16)

		val decryptedData = String(decryptAes(Base64.getDecoder().decode(encryptedObject.data), key, iv))

		assertThat(decryptedData).isEqualTo(data)
	}

	@Test
	fun encryptRsaTest() {
		val publicKey = TestTools.rsaPublicKey!!
		val privateKey = TestTools.rsaPrivateKey!!

		val data = "{\"data\": 1}"

		val encryptedData = CryptTools.encryptRsa(data = data.toByteArray(), publicKey = publicKey)

		val decryptedData = String(decryptRsa(encryptedData, privateKey))

		assertThat(decryptedData).isEqualTo(data)
	}

	@Test
	fun encryptAesTest() {
		val data = "{\"data\": 1}"
		val aesKey = byteArrayOf(
				65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45,
				65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45
		)
		val aesIV = byteArrayOf(65, 1, 2, 23, 4, 5, 6, 7, 32, 21, 10, 11, 12, 13, 84, 45)

		val encryptedData = CryptTools.encryptAes(data = data, key = aesKey, iv = aesIV)

		val decryptedData = String(decryptAes(encryptedData, aesKey, aesIV))

		assertThat(decryptedData).isEqualTo(data)
	}

	private fun decryptRsa(data: ByteArray, privateKey: PrivateKey): ByteArray {
		val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
		cipher.init(Cipher.DECRYPT_MODE, privateKey)
		return cipher.doFinal(data)
	}

	private fun decryptAes(encryptedData: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
		val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
		cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
		return cipher.doFinal(encryptedData)
	}
}
