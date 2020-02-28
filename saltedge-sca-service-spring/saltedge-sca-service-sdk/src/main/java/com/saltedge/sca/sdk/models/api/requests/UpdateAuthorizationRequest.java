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
package com.saltedge.sca.sdk.models.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.saltedge.sca.sdk.ScaSdkConstants;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.saltedge.sca.sdk.ScaSdkConstants.KEY_AUTHORIZATION_CODE;

public class UpdateAuthorizationRequest extends AuthenticatedRequestAbs {
    @JsonProperty(ScaSdkConstants.KEY_DATA)
    @NotNull
    @Valid
    public Data data;

    public UpdateAuthorizationRequest() {
    }

    public UpdateAuthorizationRequest(@NotNull Boolean confirm, @NotNull String authorizationCode) {
        this.data = new Data(confirm, authorizationCode);
    }

    public static class Data {
        @JsonProperty("confirm")
        @NotNull
        public Boolean confirmAuthorization;

        @JsonProperty(KEY_AUTHORIZATION_CODE)
        @NotNull
        public String authorizationCode;

        public Data() {
        }

        public Data(@NotNull Boolean confirmAuthorization, @NotNull String authorizationCode) {
            this.confirmAuthorization = confirmAuthorization;
            this.authorizationCode = authorizationCode;
        }
    }
}
