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
package com.saltedge.authenticator.identity.error

import org.springframework.http.HttpStatus

interface CustomExceptionValues {
    val errorStatus: HttpStatus
    val errorClass: String
    val errorMessage: String
}

class AccessTokenNotFoundException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.UNAUTHORIZED
    override val errorClass: String = "AccessTokenNotFound"
    override val errorMessage: String = "AccessTokenNotFound"
}

class AuthorizationNotFoundException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.NOT_FOUND
    override val errorClass: String = "AuthorizationNotFound"
    override val errorMessage: String = "AuthorizationNotFound"
}

class ConnectionNotFoundException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.UNAUTHORIZED
    override val errorClass: String = "ConnectionNotFound"
    override val errorMessage: String = "ConnectionNotFound"
}

class InvalidSignatureException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.BAD_REQUEST
    override val errorClass: String = "InvalidSignature"
    override val errorMessage: String = "InvalidSignature"
}

class SignatureExpiredException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.BAD_REQUEST
    override val errorClass: String = "SignatureExpired"
    override val errorMessage: String = "SignatureExpired"
}

class SignatureNotFoundException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.BAD_REQUEST
    override val errorClass: String = "SignatureNotFound"
    override val errorMessage: String = "SignatureNotFound"
}

class UserNotFoundException : RuntimeException(), CustomExceptionValues {
    override val errorStatus: HttpStatus = HttpStatus.NOT_FOUND
    override val errorClass: String = "UserNotFound"
    override val errorMessage: String = "UserNotFound"
}
