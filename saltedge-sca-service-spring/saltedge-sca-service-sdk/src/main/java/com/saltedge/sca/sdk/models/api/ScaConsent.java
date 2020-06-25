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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

/**
 * Consent object fields description
 */
@Validated
public class ScaConsent {
    /**
     * Unique identifier of consent model
     */
    @JsonProperty(KEY_ID)
    @NotEmpty
    public String id;

    /**
     * Unique identifier of user to which consent belongs
     */
    @JsonProperty(KEY_USER_ID)
    @NotEmpty
    public String userId;

    /**
     * date and time when the consent was created
     */
    @JsonProperty(KEY_CREATED_AT)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant createdAt;

    /**
     * date and time when the consent will expire
     */
    @JsonProperty(KEY_EXPIRES_AT)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant expiresAt;

    /**
     * Consent type.
     * supported values: [aisp]
     */
    @JsonProperty(KEY_CONSENT_TYPE)
    @NotEmpty
    public String consentType = "aisp";

    /**
     * the name of the third party provider / application to which user has provided consent
     */
    @JsonProperty(KEY_TPP_NAME)
    @NotEmpty
    public String tppName;

    /**
     * the information of accounts which were shared by user with third party provider
     */
    @JsonProperty(KEY_ACCOUNTS)
    @NotNull
    public List<ScaAccount> accounts;

    public ScaConsent() {
    }

    public ScaConsent(
            @NotEmpty String id,
            @NotEmpty String userId,
            @NotNull Instant createdAt,
            @NotNull Instant expiresAt,
            @NotEmpty String tppName,
            @NotNull List<ScaAccount> accounts
    ) {
        this.id = id;
        this.userId = userId;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.tppName = tppName;
        this.accounts = accounts;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getConsentType() {
        return consentType;
    }

    public String getTppName() {
        return tppName;
    }

    public List<ScaAccount> getAccounts() {
        return accounts;
    }
}
