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

import com.saltedge.sca.sdk.errors.Unauthorized;
import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.tools.DateTools;
import com.saltedge.sca.sdk.tools.KeyTools;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.security.PublicKey;
import java.time.Instant;

@Entity(name = "Client_Connection")
@Table(name = "Client_Connection")
public class ClientConnectionEntity extends BaseEntity implements ClientConnection {
    @Column(length = 4096)
    private String publicKey = "";

    @Column(length = 4096)
    private String pushToken = "";

    @Column(length = 32)
    private String platform = "";

    @Column(length = 4096)
    private String returnUrl = "";

    @Column(length = 4096)
    private String authSessionSecret = "";

    @Column
    private Instant authSessionSecretExpiresAt = null;

    @Column(length = 4096)
    private String accessToken = "";

    @Column
    private Instant accessTokenExpiresAt = null;

    @Column
    private Boolean revoked = false;

    @Column
    private String userId;

    public ClientConnectionEntity() { }//Default constructor for Repository

    @Override
    public String getPublicKeyString() {
        return publicKey;
    }

    @Override
    public String getPlatform() {
        return platform;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Instant getAccessTokenExpiresAt() {
        return accessTokenExpiresAt;
    }

    @Override
    public String getPushToken() {
        return pushToken;
    }

    @Override
    public Boolean getRevoked() {
        return revoked;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public PublicKey getPublicKey() {
        if (StringUtils.isEmpty(publicKey)) throw new Unauthorized.ConnectionNotFound();
        PublicKey key = KeyTools.convertPemStringToPublicKey(publicKey);
        if (key == null) throw new Unauthorized.ConnectionNotFound();
        return key;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public String getAuthSessionSecret() {
        return authSessionSecret;
    }

    public Instant getAuthSessionSecretExpiresAt() {
        return authSessionSecretExpiresAt;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public void setAuthToken(String authSessionSecret) {
        this.authSessionSecret = authSessionSecret;
    }

    public void setAuthTokenExpiresAt(Instant authTokenExpiresAt) {
        this.authSessionSecretExpiresAt = authTokenExpiresAt;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) {
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean hasAuthSessionExpired() {
        return DateTools.dateIsExpired(this.authSessionSecretExpiresAt);
    }

    public boolean isAuthenticated() {
        return !StringUtils.isEmpty(this.userId);
    }
}
