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

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

public class Notification {
    @JsonProperty(KEY_TITLE)
    public String title;

    @JsonProperty("body")
    public String body;

    @JsonProperty(KEY_PUSH_TOKEN)
    public String pushToken;

    @JsonProperty(KEY_PLATFORM)
    public String platform;

    @JsonProperty(KEY_DATA)
    public Data data;

    @JsonProperty(KEY_EXPIRES_AT)
    public String expiresAt;

    public Notification() {
    }

    public Notification(String title, String body, String pushToken, String platform, String connectionId, String authorizationId, String expiresAt) {
        this.title = title;
        this.body = body;
        this.pushToken = pushToken;
        this.platform = platform;
        this.data = new Data(connectionId, authorizationId);
        this.expiresAt = expiresAt;
    }

    public static class Data {
        @JsonProperty(ScaSdkConstants.KEY_CONNECTION_ID)
        public String connectionId;

        @JsonProperty(ScaSdkConstants.KEY_AUTHORIZATION_ID)
        public String authorizationId;

        public Data() {
        }

        public Data(String connectionId, String authorizationId) {
            this.connectionId = connectionId;
            this.authorizationId = authorizationId;
        }
    }
}
