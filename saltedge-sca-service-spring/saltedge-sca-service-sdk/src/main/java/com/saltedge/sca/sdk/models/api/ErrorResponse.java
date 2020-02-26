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
package com.saltedge.sca.sdk.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.errors.HttpErrorParams;

public class ErrorResponse {
    @JsonProperty(ScaSdkConstants.KEY_ERROR_CLASS)
    public String errorClass;

    @JsonProperty(ScaSdkConstants.KEY_ERROR_MESSAGE)
    public String errorMessage;

    public ErrorResponse() { }

    public ErrorResponse(String errorClass, String errorMessage) {
        this.errorClass = errorClass;
        this.errorMessage = errorMessage;
    }

    public ErrorResponse(Exception ex) {
        errorClass = ex.getClass().getSimpleName();
        errorMessage = ex.getLocalizedMessage();
        if (ex instanceof HttpErrorParams) {
            errorClass = ((HttpErrorParams) ex).getErrorClass();
            errorMessage = ((HttpErrorParams) ex).getErrorMessage();
        }
    }

    public ErrorResponse(HttpErrorParams params) {
        errorClass = params.getErrorClass();
        errorMessage = params.getErrorMessage();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ErrorResponse that = (ErrorResponse) o;
        return Objects.equal(errorClass, that.errorClass) &&
                Objects.equal(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(errorClass, errorMessage);
    }

    @Override
    public String toString() {
        return "ErrorResponse{" +
                "errorClass='" + errorClass + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}