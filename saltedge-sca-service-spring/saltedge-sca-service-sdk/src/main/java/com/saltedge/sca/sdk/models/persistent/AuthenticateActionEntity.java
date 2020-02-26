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

import com.saltedge.sca.sdk.models.ActionStatus;
import com.saltedge.sca.sdk.models.AuthenticateAction;
import com.saltedge.sca.sdk.models.converter.StringMapConverter;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;

@Entity(name = "Authenticate_Action")
@Table(name = "Authenticate_Action")
public class AuthenticateActionEntity extends BaseEntity implements AuthenticateAction {
    @Column(nullable = false, length = 256)
    private String code = "";

    @Column(nullable = false, length = 4096, unique = true)
    private String uuid = "";

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean requireSca = false;

    @Column(length = 4096)
    private String title;

    @Column(length = 4096)
    private String description;

    @Column(length = 4096)
    private String userId;

    @Column(name = "extra", nullable = false)
    @Convert(converter = StringMapConverter.class)
    public Map<String, String> extra;

    public AuthenticateActionEntity() { }

    public AuthenticateActionEntity(String code, String uuid) {
        this.code = code;
        this.uuid = uuid;
        this.requireSca = false;
    }

    public AuthenticateActionEntity(
            String code,
            String uuid,
            Boolean requireSca,
            String title,
            String description
    ) {
        this.code = code;
        this.uuid = uuid;
        this.expiresAt = LocalDateTime.now().plusMinutes(5);
        this.requireSca = requireSca;
        this.title = title;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public Boolean getRequireSca() {
        return requireSca;
    }

    public void setRequireSca(Boolean requireSca) {
        this.requireSca = requireSca;
    }

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public Map<String, String> getExtra() {
        return extra;
    }

    public void setExtra(Map<String, String> extra) {
        this.extra = extra;
    }

    @Override
    public Boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    @Override
    public Boolean isAuthenticated() {
        return userId != null;
    }

    @Override
    public ActionStatus getActionStatus() {
        if (this.isExpired()) return ActionStatus.EXPIRED;
        else if (this.isAuthenticated()) return ActionStatus.AUTHENTICATED;
        else return ActionStatus.WAITING_CONFIRMATION;
    }
}
