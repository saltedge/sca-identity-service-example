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

import com.saltedge.authenticator.identity.model.mapping.EncryptedAuthorization
import java.security.PublicKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptTools {
    fun encrypt(data: String, publicKey: PublicKey): EncryptedAuthorization {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        return EncryptedAuthorization(
            algorithm = "AES-256-CBC",
            key = Base64.getEncoder().encodeToString(encryptRsa(key, publicKey)),
            iv = Base64.getEncoder().encodeToString(encryptRsa(iv, publicKey)),
            data = Base64.getEncoder().encodeToString(encryptAes(data, key, iv))
        )
    }

    fun encryptAes(data: String, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
        return cipher.doFinal(data.toByteArray())
    }

    fun encryptRsa(data: ByteArray, publicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(data)
    }
}
