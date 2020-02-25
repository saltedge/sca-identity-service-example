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
package com.saltedge.sca.sdk.errors;

import com.saltedge.sca.sdk.models.api.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;

/**
 * Global error handler for a Spring REST API
 */
@ControllerAdvice
public class ApiExceptionsHandler extends ResponseEntityExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(ApiExceptionsHandler.class);

    @ExceptionHandler({
            BadRequest.class,
            NotFound.class,
            Unauthorized.class
    })
    public ResponseEntity<ErrorResponse> handleCustomException(Exception ex, WebRequest request) {
        HttpStatus errorStatus = ex instanceof HttpErrorParams ? ((HttpErrorParams) ex).getErrorStatus() : HttpStatus.BAD_REQUEST;
        ErrorResponse error = new ErrorResponse(ex);
        log.error(error.toString());
        ex.printStackTrace();
        return ResponseEntity.status(errorStatus).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        ErrorResponse error = new ErrorResponse("WrongRequestFormat", e.getMessage());
        log.error(e.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
