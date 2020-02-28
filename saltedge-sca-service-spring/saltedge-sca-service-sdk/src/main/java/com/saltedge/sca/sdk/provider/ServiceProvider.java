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
package com.saltedge.sca.sdk.provider;

import com.saltedge.sca.sdk.models.AuthenticateAction;
import com.saltedge.sca.sdk.models.Authorization;

/**
 * Interface for communication between SCA Service and Service Provider application.
 * Service Provider application should implement `@Service` which `implements ProviderApi`
 */
public interface ServiceProvider {
    String findUserIdByAuthorizationSessionSecret(String sessionSecret);
    String getAuthorizationPageUrl(String sessionSecret);
    String getProviderCode();
    String getProviderName();
    String getProviderLogoUrl();
    String getProviderSupportEmail();
    Long onAuthenticateAction(AuthenticateAction action);
    void onAuthorizationConfirmed(Authorization authorization);
}
