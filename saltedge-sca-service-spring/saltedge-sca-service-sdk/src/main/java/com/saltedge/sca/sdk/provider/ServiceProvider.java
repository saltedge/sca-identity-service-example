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

import com.saltedge.sca.sdk.models.*;
import com.saltedge.sca.sdk.models.api.ScaConsent;
import com.saltedge.sca.sdk.models.api.ScaProviderConfigurationData;

import java.util.List;

/**
 * Interface for communication between SCA Module and Service Provider application.
 * Provides required by SCA module information and receives Action and Authorization events.
 * Service Provider application should implement `@Service` which implements `ServiceProvider` interface
 */
public interface ServiceProvider {
    /**
     * Gives to SDK human readable Bank name for notifications
     *
     * @return name string
     */
    String getProviderName();

    /**
     * Gives to SDK SCA Service configuration data.
     *
     * @return SCAProviderConfigurationData object
     * @see ScaProviderConfigurationData
     */
    ScaProviderConfigurationData getProviderConfiguration();

    /**
     * Provides URL of authentication page of Service Provider
     * for redirection in Authenticator app for authentication step of enrollment flow.
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
     * @see UserIdentity
     */
    UserIdentity getUserIdByAuthenticationSessionSecret(String sessionSecret);

    /**
     * Notifies application about receiving new authenticated Action request.
     * It can be Sign-in to portal action or Payment action which requires authentication.
     *
     * @param action entity with uuid and userId
     * @return return authorization content (title, description) for creating Authorization object (SCA confirmation),
     *         if null then user will receive Instant action error.
     * @see AuthenticateAction
     * @see AuthorizationContent
     */
    AuthorizationContent onAuthenticateAction(AuthenticateAction action);

    /**
     * Notifies application about confirmation or denying of SCA Authorization
     *
     * @param authorization an entity with unique authorizationCode and isConfirmed fields
     * @see Authorization
     */
    void onAuthorizationConfirmed(Authorization authorization);

    /**
     * Provides list of Consents for User
     * Return empty list if consent management is not supported.
     *
     * @param userId an unique identifier of User (Customer) of ASPSP (Financial Institution)
     * @return list of Consent objects
     * @see ScaConsent
     */
    List<ScaConsent> getActiveConsents(String userId);

    /**
     * Revoke Consent. Mark as revoked and notify concerned parties.
     * Return always FALSE if consent management is not supported.
     *
     * @param userId an unique identifier of User (Customer) of ASPSP (Financial Institution)
     * @param consentId an unique identifier of Consent of User
     * @return result of operation, TRUE is success
     */
    boolean revokeConsent(String userId, String consentId);
}
