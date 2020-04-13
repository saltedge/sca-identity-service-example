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

import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.Objects;

/**
 * Wrapper for data which identify the User (Customer)
 */
@Validated
public class UserIdentity {
    @NotEmpty private String userId;
    private String accessToken;
    private Instant accessTokenExpiresAt;

    public UserIdentity(@NotEmpty String userId) {
        this.userId = userId;
    }

    /**
     * Constructor
     *
     * @param userId unique identifier of User (Customer)
     * @param accessToken unique code for verification of access rights to SCA Service (optional)
     * @param accessTokenExpiresAt expiration time (UTC) of accessToken (optional)
     */
    public UserIdentity(@NotEmpty String userId, String accessToken, Instant accessTokenExpiresAt) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public boolean isAuthenticated() {
        return !StringUtils.isEmpty(this.userId);
    }

    public String getUserId() {
        return userId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Instant getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserIdentity that = (UserIdentity) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(accessTokenExpiresAt, that.accessTokenExpiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, accessToken, accessTokenExpiresAt);
    }
}
