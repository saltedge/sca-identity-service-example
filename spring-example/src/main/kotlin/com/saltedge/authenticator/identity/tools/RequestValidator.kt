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

import com.saltedge.authenticator.identity.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.identity.HEADER_KEY_EXPIRES_AT
import com.saltedge.authenticator.identity.HEADER_KEY_SIGNATURE
import com.saltedge.authenticator.identity.error.*
import com.saltedge.authenticator.identity.model.Connection
import com.saltedge.authenticator.identity.model.ConnectionsRepository
import javax.servlet.http.HttpServletRequest

fun HttpServletRequest.validateRequest(requestBody: String = "", connectionsRepository: ConnectionsRepository?): Connection? {
    val accessToken = this.getHeader(HEADER_KEY_ACCESS_TOKEN) ?: throw AccessTokenNotFoundException()
    if (accessToken.isBlank()) throw AccessTokenNotFoundException()
    val expiresAt = this.getHeader(HEADER_KEY_EXPIRES_AT)?.toIntOrNull() ?: throw SignatureExpiredException()
    if (expiresAt <= nowUtcSeconds) throw SignatureExpiredException()
    val signature = this.getHeader(HEADER_KEY_SIGNATURE)
    if (signature == null || signature.isBlank()) throw SignatureNotFoundException()

    val connection: Connection? = connectionsRepository?.findByAccessTokenAndRevokedFalse(accessToken) ?: throw ConnectionNotFoundException()
    val publicKey = connection?.publicKey?.toPublicKey() ?: throw ConnectionNotFoundException()

    val requestUrl = if (this.requestURL.toString().contains("//localhost")) this.requestURL.toString()
    else this.requestURL.toString().replace("http://", "https://")

    val verifySuccess = SignTools.verify(
            signature = signature,
            requestMethod = this.method,
            requestUrl = requestUrl,
            expiresAt = expiresAt.toString(),
            requestBody = requestBody,
            publicKey = publicKey
    )
    if (!verifySuccess) throw InvalidSignatureException()
    return connection
}