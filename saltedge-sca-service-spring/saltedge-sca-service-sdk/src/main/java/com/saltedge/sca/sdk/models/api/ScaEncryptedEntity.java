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
import com.saltedge.sca.sdk.ScaSdkConstants;

public class ScaEncryptedEntity {
    @JsonProperty(ScaSdkConstants.KEY_ID)
    public String id = "";

    @JsonProperty(ScaSdkConstants.KEY_CONNECTION_ID)
    public String connectionId = "";

    @JsonProperty("algorithm")
    public String algorithm;

    @JsonProperty("key")
    public String key;

    @JsonProperty("iv")
    public String iv;

    @JsonProperty(ScaSdkConstants.KEY_DATA)
    public String data;

    public ScaEncryptedEntity() {
    }

    public ScaEncryptedEntity(String algorithm, String key, String iv, String data) {
        this.algorithm = algorithm;
        this.key = key;
        this.iv = iv;
        this.data = data;
    }

    public ScaEncryptedEntity(String id, String connectionId, String algorithm, String key, String iv, String data) {
        this(algorithm, key, iv, data);
        this.id = id;
        this.connectionId = connectionId;
    }
}
