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

import com.saltedge.authenticator.identity.model.mapping.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(
            AccessTokenNotFoundException::class,
            ConnectionNotFoundException::class,
            AuthorizationNotFoundException::class,
            InvalidSignatureException::class,
            SignatureExpiredException::class,
            SignatureNotFoundException::class,
            UserNotFoundException::class
    )
    fun handleCustomException(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        val values = ex as? CustomExceptionValues
        val errors = ErrorResponse(
                errorClass = values?.errorClass ?: ex.javaClass.simpleName,
                errorMessage = values?.errorMessage ?: ex.localizedMessage
        )
        return ResponseEntity.status(values?.errorStatus ?: HttpStatus.BAD_REQUEST).body(errors)
    }
}