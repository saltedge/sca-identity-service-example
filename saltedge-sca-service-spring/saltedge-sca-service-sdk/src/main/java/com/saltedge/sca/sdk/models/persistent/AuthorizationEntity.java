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
package com.saltedge.sca.sdk.models.persistent;

import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.AuthorizationStatus;
import com.saltedge.sca.sdk.tools.DateTools;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.Instant;

/**
 * @see com.saltedge.sca.sdk.models.Authorization
 */
@Entity(name = "Transaction_Authorization")
@Table(name = "Transaction_Authorization")
public class AuthorizationEntity extends BaseEntity implements Authorization {
    @Column(nullable = false)
    private Instant expiresAt;

    private Boolean confirmed;

    @Column(length = 64)
    private String confirmLocation = "";

    @Column(length = 64)
    private String confirmAuthorizationType = "";

    @Column(length = 4096)
    private String title = "";

    @Column(length = 4096, nullable = false)
    private String description = "";

    @Column(length = 4096, nullable = false)
    private String authorizationCode = "";

    @Column(length = 4096)
    private String userId;

    public AuthorizationEntity() {}//Default constructor for Repository

    public AuthorizationEntity(
            String title,
            String description,
            Instant expiresAt,
            String authorizationCode,
            String userId
    ) {
        this.title = title;
        this.description = description;
        this.expiresAt = expiresAt;
        this.authorizationCode = authorizationCode;
        this.userId = userId;
    }

    public Boolean isExpired() {
        return DateTools.dateIsExpired(this.expiresAt);
    }

    @Override
    public AuthorizationStatus getStatus() {
        if (confirmed != null) return confirmed ? AuthorizationStatus.CONFIRMED : AuthorizationStatus.DENIED;
        if (this.isExpired()) return AuthorizationStatus.EXPIRED;
        return AuthorizationStatus.WAITING_CONFIRMATION;
    }

    @Override
    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public Boolean getConfirmed() {
        return confirmed;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getAuthorizationCode() {
        return authorizationCode;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getConfirmLocation() {
        return confirmLocation == null ? "" : confirmLocation;
    }

    @Override
    public String getConfirmAuthorizationType() {
        return confirmAuthorizationType == null ? (confirmed == null ? "" : "unknown") : confirmAuthorizationType;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setConfirmLocation(String confirmLocation) {
        this.confirmLocation = confirmLocation;
    }

    public void setConfirmAuthorizationType(String confirmAuthorizationType) {
        this.confirmAuthorizationType = confirmAuthorizationType;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
