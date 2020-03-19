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
import com.saltedge.sca.sdk.models.UserIdentity;

/**
 * Interface for communication between SCA Module and Service Provider application.
 * Provides required by SCA module information and receives Action and Authorization events.
 * Service Provider application should implement `@Service` which implements `ServiceProvider` interface
 */
public interface ServiceProvider {
    /**
     * Provides URL of authentication page of Service Provider
     * for redirection in Authenticator app.
     *
     * @param enrollSessionSecret code related to Authenticator enrollment flow (Created by SDK)
     * @return url string
     */
    String getAuthorizationPageUrl(String enrollSessionSecret);

    /**
     * Find User (Customer) entity by authentication session secret code.
     * Authentication session secret code is created when user already authenticated and want to connect Authenticator app.
     * Authentication session secret code should be created by Authentication Service.
     *
     * @param sessionSecret code. (Created by Service Provider)
     * @return UserIdentity object with userId (optional) and accessToken (optional) value.
     */
    UserIdentity getUserIdByAuthenticationSessionSecret(String sessionSecret);

    /**
     * Provides code name of Service Provider
     *
     * @return code
     */
    String getProviderCode();

    /**
     * Provides human readable name of Service Provider
     *
     * @return name
     */
    String getProviderName();

    /**
     * Provides logo image of Service Provider
     *
     * @return url string
     */
    String getProviderLogoUrl();

    /**
     * Provides email of Service Provider for clients support
     *
     * @return email string
     */
    String getProviderSupportEmail();

    /**
     * Notifies application about receiving new authenticated Action request.
     * It can be Sign-in to portal action or Payment action which requires authentication.
     *
     * @param action entity with uuid and userId
     * @return return authorization id if SCA confirmation is required or null.
     */
    Long onAuthenticateAction(AuthenticateAction action);

    /**
     * Notifies application about confirmation or denying of SCA Authorization
     *
     * @param authorization entity with unique authorizationCode and isConfirmed fields
     */
    void onAuthorizationConfirmed(Authorization authorization);
}
