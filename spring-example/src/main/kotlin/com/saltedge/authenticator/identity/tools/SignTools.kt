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

import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

object SignTools {

    fun verify(signature: String,
               requestMethod: String,
               requestUrl: String,
               expiresAt: String,
               requestBody: String,
               publicKey: PublicKey): Boolean {
        val data = "${requestMethod.toLowerCase()}|$requestUrl|$expiresAt|$requestBody"
        return verify(signature = signature, data = data, publicKey = publicKey)
    }

    fun verify(signature: String, data: String, publicKey: PublicKey): Boolean {
        return try {
            val sign = Signature.getInstance("SHA256withRSA")
            sign.initVerify(publicKey)
            sign.update(data.toByteArray(StandardCharsets.UTF_8))
            sign.verify(Base64.getDecoder().decode(signature))
        } catch (e: IllegalArgumentException) {
            false
        } catch (e: Exception) {
            e.printStackTrace()//TODO log
            false
        }
    }
}

fun String.toPublicKey(): PublicKey? {
    return try {
        val publicKeyContent = this.replace("\\n".toRegex(), "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
        val kf = KeyFactory.getInstance("RSA")
        val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
        kf.generatePublic(keySpecX509) as RSAPublicKey
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
