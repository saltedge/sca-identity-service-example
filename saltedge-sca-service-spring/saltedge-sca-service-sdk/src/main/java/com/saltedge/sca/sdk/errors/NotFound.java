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

import org.springframework.http.HttpStatus;

/**
 * Set of NotFound (404) errors
 */
public abstract class NotFound extends RuntimeException implements HttpErrorParams {
    @Override
    public HttpStatus getErrorStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public String getErrorClass() {
        return getClass().getSimpleName();
    }

    /* NotFound successors  */

    public static class AuthorizationNotFound extends NotFound {
        @Override
        public String getErrorMessage() {
            return "Authorization Not Found.";
        }
    }

    public static class ActionNotFound extends NotFound {
        @Override
        public String getErrorMessage() {
            return "Action Not Found.";
        }
    }
}
