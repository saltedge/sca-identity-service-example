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
package com.saltedge.authenticator.identity

import com.saltedge.authenticator.identity.tools.toPublicKey
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

object TestTools {

    private const val publicKeyFileName = "public_key.pem"
    private const val privateKeyFileName = "private_key.pem"

    val rsaPublicKeyString: String = readKeyFile(publicKeyFileName)

    /**
     * This method read public_key.pem file from resources
     * and converts to RSA PublicKey.
     * @return PublicKey This returns PublicKey from pem file.
     */
    val rsaPublicKey: PublicKey = rsaPublicKeyString.toPublicKey()!!

    /**
     * This method read private_key.pem file from resources
     * and converts to RSA PrivateKey.
     * @return PublicKey This returns PrivateKey from pem file.
     */
    val rsaPrivateKey: PrivateKey
        get() {
            var privateKeyPEM = readKeyFile(privateKeyFileName)
            privateKeyPEM = privateKeyPEM.replace("\\n".toRegex(), "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
            val keySpec = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyPEM))
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec)
        }

    fun createSignature(
            requestMethod: String,
            requestUrl: String,
            expiresAt: String,
            requestBody: String,
            privateKey: PrivateKey
    ): String {
        return createSignature("${requestMethod.toLowerCase()}|$requestUrl|$expiresAt|$requestBody", privateKey)
    }

    fun createSignature(data: String, privateKey: PrivateKey): String {
        val sign = Signature.getInstance("SHA256withRSA").apply { initSign(privateKey) }
        sign.update(data.toByteArray(Charset.forName("UTF-8")))
        return Base64.getEncoder().encodeToString(sign.sign())
    }

    @Throws(IOException::class, NullPointerException::class)
    private fun readKeyFile(filename: String): String {
        val result = StringBuilder("")
        val classLoader = javaClass.classLoader
        val file = File(classLoader.getResource(filename)!!.file)

        Scanner(file).use { scanner ->
            while (scanner.hasNextLine()) {
                val line = scanner.nextLine()
                result.append(line).append("\n")
            }
            scanner.close()
        }
        return result.toString()
    }
}
