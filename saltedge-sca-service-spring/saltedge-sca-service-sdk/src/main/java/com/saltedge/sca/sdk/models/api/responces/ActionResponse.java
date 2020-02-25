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
package com.saltedge.sca.sdk.models.api.responces;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.saltedge.sca.sdk.ScaSdkConstants;
import org.springframework.lang.NonNull;

import javax.validation.constraints.NotNull;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

public class ActionResponse {
    @JsonProperty(ScaSdkConstants.KEY_DATA)
    @NotNull
    public Data data;

    public ActionResponse() {
    }

    public ActionResponse(Boolean success, String connectionId, String authorizationId) {
        this.data = new Data(success, connectionId, authorizationId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionResponse that = (ActionResponse) o;
        return Objects.equal(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(data);
    }

    @Override
    public String toString() {
        return "ActionResponse{data=" + data + '}';
    }

    public static class Data {
        @JsonProperty(KEY_SUCCESS)
        @NotNull
        public Boolean success;

        @JsonProperty(KEY_CONNECTION_ID)
        public String connectionId;

        @JsonProperty(KEY_AUTHORIZATION_ID)
        public String authorizationId;

        public Data() {
        }

        public Data(@NonNull Boolean success, String connectionId, String authorizationId) {
            this.success = success;
            this.connectionId = connectionId;
            this.authorizationId = authorizationId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return Objects.equal(success, data.success) &&
                    Objects.equal(connectionId, data.connectionId) &&
                    Objects.equal(authorizationId, data.authorizationId);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(success, connectionId, authorizationId);
        }

        @Override
        public String toString() {
            return "Data{" + "success=" + success + ", connectionId='" + connectionId + '\'' + ", authorizationId='" + authorizationId + '\'' + '}';
        }
    }
}
