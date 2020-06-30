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
import static com.saltedge.sca.sdk.ScaSdkConstants.KEY_NAME;

/**
 * SCA Service configuration data.
 * Used by mobile client for creation of new connection between SCA Service and mobile client.
 */
@Validated
public class ScaProviderConfigurationData {
    /**
     * Base URL of SCA service
     */
    @JsonProperty(KEY_CONNECT_URL)
    @NotNull
    public String connectUrl;

    /**
     * Code of Bank
     */
    @JsonProperty("code")
    @NotNull
    public String code;

    /**
     * Human readable name of Bank
     */
    @JsonProperty(KEY_NAME)
    @NotNull
    public String name;

    /**
     * URL of logo image of Bank
     */
    @JsonProperty("logo_url")
    @NotNull
    public String logoUrl;

    /**
     * Email address of Clients support service of Bank
     */
    @JsonProperty("support_email")
    @NotNull
    public String supportEmail;

    /**
     * Version of SCA SDK, by default is 1
     */
    @JsonProperty("version")
    @NotNull
    public String version = "1";

    /**
     * Consent Management feature is available for mobile clients or not
     */
    @JsonProperty("consent_management")
    @NotNull
    public Boolean consentManagementIsSupported = true;

    public ScaProviderConfigurationData() {
    }

    public ScaProviderConfigurationData(
            @NonNull String connectUrl,
            @NonNull String code,
            @NonNull String name,
            @NonNull String logoUrl,
            @NonNull String supportEmail,
            @NotNull Boolean consentManagementIsSupported
    ) {
        this.connectUrl = connectUrl;
        this.code = code;
        this.name = name;
        this.logoUrl = logoUrl;
        this.supportEmail = supportEmail;
        this.consentManagementIsSupported = consentManagementIsSupported;
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

    public Boolean getConsentManagementIsSupported() {
        return consentManagementIsSupported;
    }
}
