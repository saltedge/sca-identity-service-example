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
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import static com.saltedge.sca.sdk.ScaSdkConstants.KEY_CONNECT_URL;

/**
 * SCA Service configuration data
 */
@Validated
public class ConfigurationData {
    @JsonProperty(KEY_CONNECT_URL)
    @NotNull
    public String connectUrl;

    @JsonProperty("code")
    @NotNull
    public String code;

    @JsonProperty("name")
    @NotNull
    public String name;

    @JsonProperty("logo_url")
    @NotNull
    public String logoUrl;

    @JsonProperty("support_email")
    @NotNull
    public String supportEmail;

    @JsonProperty("version")
    @NotNull
    public String version = "1";

    public ConfigurationData() {
    }

    public ConfigurationData(
            @NonNull String connectUrl,
            @NonNull String code,
            @NonNull String name,
            @NonNull String logoUrl,
            @NonNull String supportEmail
    ) {
        this.connectUrl = connectUrl;
        this.code = code;
        this.name = name;
        this.logoUrl = logoUrl;
        this.supportEmail = supportEmail;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getVersion() {
        return version;
    }
}
