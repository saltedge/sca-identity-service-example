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
package com.saltedge.sca.sdk.services;

import com.saltedge.sca.sdk.controllers.ConfigurationController;
import com.saltedge.sca.sdk.models.AuthenticateAction;
import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.tools.EnvironmentTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;
import static com.saltedge.sca.sdk.tools.UrlTools.createUserAuthErrorUrl;
import static com.saltedge.sca.sdk.tools.UrlTools.createUserAuthSuccessUrl;


@Service
@Validated
public class ScaSdkService {
    private Logger log = LoggerFactory.getLogger(ScaSdkService.class);
    @Autowired
    private Environment env;
    @Autowired
    private AuthorizationsService authorizationsService;
    @Autowired
    private ClientConnectionsService connectionsService;
    @Autowired
    private AuthenticateActionsService actionsService;

    /**
     * Returns list of client connections (Authenticators) related to user.
     * Can be shown for future administration (revoking).
     *
     * @param userId unique identifier of user of Service Provider
     * @return list of client connections
     */
    public List<ClientConnection> getClientConnections(@NotEmpty String userId) {
        return connectionsService.getConnections(userId);
    }

    /**
     * Marks client connection (Authenticator) as revoked.
     * Authenticator will receive ConnectionNotFound error.
     *
     * @param connectionId unique id of connection
     */
    public void revokeConnection(@NotNull Long connectionId) {
        connectionsService.revokeConnection(connectionId);
    }

    /**
     * Creates App Link for linking of Salt Edge Authenticator app.
     * Can be encoded in QR code.
     *
     * @return app link string ("authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration")
     */
    public String createConnectAppLink() {
        return createConnectAppLink("");
    }

    /**
     * Creates App Link for linking of Salt Edge Authenticator app.
     * Can be encoded in QR code.
     *
     * @param connectSecret unique authentication code which will be included in app link.
     *                      Authentication session secret code is created
     *                      when user already authenticated and want to connect Authenticator app
     * @return app link string ("  authenticator://saltedge.com/connect?configuration=https://saltedge.com/configuration&connect_query=A12345678")
     */
    public String createConnectAppLink(String connectSecret) {
        String identityServiceUrl = EnvironmentTools.getScaServiceUrl(env);
        String link = APP_LINK_PREFIX_CONNECT + "?configuration=" + identityServiceUrl + ConfigurationController.CONFIGURATION_REQUEST_PATH;
        return (StringUtils.isEmpty(connectSecret)) ? link : link + "&" + KEY_CONNECT_QUERY + "=" + connectSecret;
    }

    /**
     * Creates Authorization entity for future confirmation/denying by user.
     *
     * @param userId unique identifier of user of Service Provider
     * @param confirmationCode unique code related to current Authorization action
     * @param title string which will be shown to user of Authenticator App
     * @param description string which will be shown to user of Authenticator App
     * @return Authorization entity
     */
    public Authorization createAuthorization(
            @NotEmpty String userId,
            @NotEmpty String confirmationCode,
            @NotEmpty String title,
            @NotEmpty String description
    ) {
        return authorizationsService.createAuthorization(userId, confirmationCode, title, description);
    }

    /**
     * Return list of Authorization actions related to user.
     *
     * @param userId unique identifier of user of Service Provider
     * @return list of Authorizations
     */
    public List<Authorization> getAllAuthorizations(@NotEmpty String userId) {
        return authorizationsService.getAllAuthorizations(userId);
    }

    /**
     * Return single pending Authorization.
     *
     * @param authorizationId unique identifier of pending authorization
     * @return Authorizations object or null
     */
    public Authorization getAuthorizationById(@NotNull Long authorizationId) {
        return authorizationsService.getAuthorization(authorizationId);
    }

    /**
     * Notifies SCA Module what user successfully authenticated in enrollment flow.
     *
     * @param enrollSessionSecret unique code of enrollment session.
     *                          Provided by SCA Module in `ServiceProvider.getAuthorizationPageUrl()`
     * @param userId unique identifier of user of Service Provider
     * @return final redirect url string for Salt Edge Authenticator app
     * @see com.saltedge.sca.sdk.provider.ServiceProvider#getAuthorizationPageUrl(String)
     */
    public String onUserAuthenticationSuccess(
            @NotNull String enrollSessionSecret,
            @NotEmpty String userId)
    {
        ClientConnectionEntity connection = connectionsService.authenticateConnection(enrollSessionSecret, userId);
        if (connection == null) {
            return createUserAuthErrorUrl(null, "SESSION_STOPPED", "Authentication session stopped.");
        } else if (connection.isAuthSessionExpired()) {
            return createUserAuthErrorUrl(connection.getReturnUrl(), "SESSION_EXPIRED", "Authentication Session Expired.");
        } else {
            return createUserAuthSuccessUrl(connection.getReturnUrl(), String.valueOf(connection.getId()), connection.getAccessToken());
        }
    }

    /**
     * Notifies SCA Module what user authentication failed (enrollment flow).
     *
     * @param enrollSessionSecret unique code of enrollment session.
     *                            Provided by SCA Module in `ServiceProvider.getAuthorizationPageUrl()`
     * @param errorMessage human readable error message which will ne shown to user of Authenticator app
     * @return final redirect url string for Salt Edge Authenticator app
     * @see com.saltedge.sca.sdk.provider.ServiceProvider#getAuthorizationPageUrl(String)
     */
    public String onUserAuthenticationFail(String enrollSessionSecret, String errorMessage) {
        String returnUrl = connectionsService.getConnectionReturnUrl(enrollSessionSecret);
        return createUserAuthErrorUrl(returnUrl, "AUTHENTICATION_FAILED", errorMessage);
    }

    /**
     * Creates SCA Instant Action (AuthenticateAction) entity.
     *
     * @param actionCode unique code (type) of Action. (e.g. "login-action", "payment-action")
     * @param uuid unique identifier of Action
     * @param actionExpiresAt time when Action will be expired. Can be null.
     * @return AuthenticateAction entity
     */
    public AuthenticateAction createAction(
            @NotEmpty String actionCode,
            String uuid,
            LocalDateTime actionExpiresAt
    ) {
        return actionsService.createAction(actionCode, uuid, actionExpiresAt);
    }

    /**
     * Finds AuthenticateAction entity.
     *
     * @param actionUUID unique identifier of Action
     * @return AuthenticateAction entity or null
     */
    public AuthenticateAction getActionByUUID(@NotNull String actionUUID) {
        if (StringUtils.isEmpty(actionUUID)) return null;
        return actionsService.getActionByUUID(actionUUID);
    }

    /**
     * Creates App Link for authenticate an action (e.g. login, payment) in Salt Edge Authenticator app.
     * Can be encoded in QR code.
     *
     * @param actionUUID unique identifier of Action
     * @return app link string ("authenticator://saltedge.com/action?action_uuid=123456&connect_url=http://someurl.com&return_to=http://return.com")
     */
    public String createAuthenticateActionAppLink(@NotEmpty String actionUUID) {
        String identityServiceUrl = EnvironmentTools.getScaServiceUrl(env);
        return APP_LINK_PREFIX_ACTION + "?" + KEY_ACTION_UUID + "=" + actionUUID + "&" + KEY_CONNECT_URL + "=" + identityServiceUrl;
    }
}
