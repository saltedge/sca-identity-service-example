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

public class ActionProcessResult {
    private boolean processedWithSuccess;
    private String actionAuthorizationId;

    public ActionProcessResult(boolean processedWithSuccess) {
        this.processedWithSuccess = processedWithSuccess;
    }

    public ActionProcessResult(boolean processedWithSuccess, String actionAuthorizationId) {
        this.processedWithSuccess = processedWithSuccess;
        this.actionAuthorizationId = actionAuthorizationId;
    }

    public boolean isProcessedWithSuccess() {
        return processedWithSuccess;
    }

    public String getActionAuthorizationId() {
        return actionAuthorizationId;
    }
}
