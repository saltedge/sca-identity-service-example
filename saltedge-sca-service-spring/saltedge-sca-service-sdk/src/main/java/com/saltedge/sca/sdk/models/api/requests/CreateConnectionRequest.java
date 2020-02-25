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
import com.saltedge.sca.sdk.validation.PublicKeyConstraint;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

public class CreateConnectionRequest {
    @JsonProperty(ScaSdkConstants.KEY_DATA)
    @NotNull
    @Valid
    public CreateConnectionRequest.Data data;

    public CreateConnectionRequest() {
    }

    public CreateConnectionRequest(CreateConnectionRequest.Data data) {
        this.data = data;
    }

    public static class Data {
        @JsonProperty("public_key")
        @PublicKeyConstraint
        public String publicKey = "";

        @JsonProperty("return_url")
        @NotNull
        public String returnUrl = "";

        @JsonProperty(KEY_PLATFORM)
        @NotEmpty
        public String platform = "";

        @JsonProperty(KEY_PUSH_TOKEN)
        @NotNull
        public String pushToken = "";

        @JsonProperty(KEY_CONNECT_QUERY)
        public String connectQuery;

        public Data() {
        }

        public Data(String publicKey, @NotNull String returnUrl, @NotEmpty String platform, @NotNull String pushToken, String connectQuery) {
            this.publicKey = publicKey;
            this.returnUrl = returnUrl;
            this.platform = platform;
            this.pushToken = pushToken;
            this.connectQuery = connectQuery;
        }
    }
}
