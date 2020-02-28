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

    public List<ClientConnection> getClientConnections(@NotEmpty String userId) {
        return connectionsService.getConnections(userId);
    }

    public void revokeConnection(@NotNull Long connectionId) {
        connectionsService.revokeConnection(connectionId);
    }

    public String createConnectAppLink() {
        return createConnectAppLink("");
    }

    public String createConnectAppLink(String connectSecret) {
        String identityServiceUrl = EnvironmentTools.getScaServiceUrl(env);
        String link = APP_LINK_PREFIX_CONNECT + "?configuration=" + identityServiceUrl + ConfigurationController.CONFIGURATION_REQUEST_PATH;
        return (StringUtils.isEmpty(connectSecret)) ? link : link + "&" + KEY_CONNECT_QUERY + "=" + connectSecret;
    }

    public Authorization createAuthorization(@NotEmpty String userId, @NotEmpty String confirmationCode, @NotEmpty String title, @NotEmpty String description) {
        return authorizationsService.createAuthorization(userId, confirmationCode, title, description);
    }

    public List<Authorization> getAllAuthorizations(@NotEmpty String userId) {
        return authorizationsService.getAllAuthorizations(userId);
    }

    public String onUserAuthenticationSuccess(@NotNull String authSessionSecret, @NotEmpty String userId) {
        ClientConnectionEntity connection = connectionsService.authenticateConnection(authSessionSecret, userId);
        if (connection == null) {
            return createUserAuthErrorUrl(null, "SESSION_STOPPED", "Authentication session stopped.");
        } else if (connection.isAuthSessionExpired()) {
            return createUserAuthErrorUrl(connection.getReturnUrl(), "SESSION_EXPIRED", "Authentication Session Expired.");
        } else {
            return createUserAuthSuccessUrl(connection.getReturnUrl(), String.valueOf(connection.getId()), connection.getAccessToken());
        }
    }

    public String onUserAuthenticationFail(String authSessionSecret, String errorMessage) {
        String returnUrl = connectionsService.getConnectionReturnUrl(authSessionSecret);
        return createUserAuthErrorUrl(returnUrl, "AUTHENTICATION_FAILED", errorMessage);
    }

    public AuthenticateAction createAction(
            @NotEmpty String actionCode,
            String uuid,
            LocalDateTime actionExpiresAt
    ) {
        return actionsService.createAction(actionCode, uuid, actionExpiresAt);
    }

    public AuthenticateAction getActionByUUID(@NotNull String actionUUID) {
        if (StringUtils.isEmpty(actionUUID)) return null;
        return actionsService.getActionByUUID(actionUUID);
    }

    public String createAuthenticateActionAppLink(@NotEmpty String actionUUID) {
        String identityServiceUrl = EnvironmentTools.getScaServiceUrl(env);
        return APP_LINK_PREFIX_ACTION + "?" + KEY_ACTION_UUID + "=" + actionUUID + "&" + KEY_CONNECT_URL + "=" + identityServiceUrl;
    }
}
