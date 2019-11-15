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
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

val DEFAULT_SALT = generateRandomString(size = 16)

fun generateRandomString(size: Int = 32): String {
    val array = ByteArray(size)
    Random().nextBytes(array)
    return String(Base64.getUrlEncoder().encode(array))
}

fun generateAuthorizationCode(payeeDetails: String, amount: String, createdAt: Long, userId: Long, description: String): String {
    val templateString = "$payeeDetails|$amount|$createdAt|$userId|$description|$DEFAULT_SALT"
    val hashBytes = MessageDigest.getInstance("SHA-256").digest(templateString.toByteArray(StandardCharsets.UTF_8));
    return Base64.getEncoder().withoutPadding().encodeToString(hashBytes);
}

fun generateAuthorizationCode(createdAt: Long, userId: Long, title: String, description: String): String {
    val templateString = "$title|$createdAt|$userId|$description|$DEFAULT_SALT"
    val hashBytes = MessageDigest.getInstance("SHA-256").digest(templateString.toByteArray(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(hashBytes);
}

fun Long.toIso8601(): String = Date(this).toIso8601()

fun Date.toIso8601(): String {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'") // Quoted "Z" to indicate UTC, no timezone offset
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(this)
}

val nowUtcSeconds: Int
    get() = (Date().time / 1000L).toInt()