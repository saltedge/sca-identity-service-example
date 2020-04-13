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
package com.saltedge.sca.sdk.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Instant;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

/**
 * Consent object fields description
 */
@Validated
public class Consent {
    @JsonProperty(KEY_ID)
    @NotEmpty
    public String id;

    @JsonProperty(KEY_TITLE)
    @NotEmpty
    public String title;

    @JsonProperty(KEY_DESCRIPTION)
    @NotEmpty
    public String description;

    @JsonProperty(KEY_CREATED_AT)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant createdAt;

    @JsonProperty(KEY_EXPIRES_AT)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant expiresAt;

    public Consent() {
    }

    public Consent(
            @NotEmpty String id,
            @NotEmpty String title,
            @NotEmpty String description,
            @NotNull Instant createdAt,
            @NotNull Instant expiresAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
