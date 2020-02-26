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

import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.models.api.requests.CreateConnectionRequest;
import com.saltedge.sca.sdk.models.api.responces.CreateConnectionResponse;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionsRepository;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.tools.CodeBuilder;
import com.saltedge.sca.sdk.tools.UrlTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
public class ClientConnectionsService {
    private Logger log = LoggerFactory.getLogger(ClientConnectionsService.class);
    @Autowired
    private ClientConnectionsRepository connectionsRepository;
    @Autowired
    private ServiceProvider providerApi;

    public CreateConnectionResponse createConnection(@NotNull CreateConnectionRequest.Data data, String authorizationSessionSecret) {
        String userId = StringUtils.isEmpty(authorizationSessionSecret) ? null : providerApi.findUserIdByAuthorizationSessionSecret(authorizationSessionSecret);
        ClientConnectionEntity connection = createClientConnectionEntity(data, userId);
        String authenticationUrl = createConnectionResponseUrl(connection);
        return new CreateConnectionResponse(String.valueOf(connection.getId()), authenticationUrl);
    }

    public List<ClientConnection> getConnections(@NotEmpty String userId) {
        return connectionsRepository.findByUserId(userId).stream().map(item -> (ClientConnection) item).collect(Collectors.toList());
    }

    public String getConnectionReturnUrl(@NotNull String authSessionSecret) {
        ClientConnectionEntity entity = connectionsRepository.findByAuthSessionSecret(authSessionSecret);
        return (entity == null) ? null : entity.getReturnUrl();
    }

    public void revokeConnection(@NotNull Long connectionId) {
        Optional<ClientConnectionEntity> optConnection = connectionsRepository.findById(connectionId);
        optConnection.ifPresent(this::revokeConnection);
    }

    public void revokeConnection(@NotNull ClientConnectionEntity connection) {
        connection.setRevoked(true);
        connectionsRepository.save(connection);
    }

    public ClientConnectionEntity authenticateConnection(@NotEmpty String authSessionSecret, @NotEmpty String userId) {
        ClientConnectionEntity entity = connectionsRepository.findByAuthSessionSecret(authSessionSecret);
        return (entity == null || entity.isAuthSessionExpired()) ? entity : authenticateClientConnection(entity, userId);
    }

    private ClientConnectionEntity createClientConnectionEntity(CreateConnectionRequest.Data requestData, String userId) {
        ClientConnectionEntity entity = new ClientConnectionEntity();
        entity.setPublicKey(requestData.getPublicKey());
        entity.setPushToken(requestData.getPushToken());
        entity.setPlatform(requestData.getPlatform());
        entity.setReturnUrl(requestData.getReturnUrl());
        return (userId == null) ? createAuthenticationToken(entity) : authenticateClientConnection(entity, userId);
    }

    private String createConnectionResponseUrl(@NotNull ClientConnectionEntity connection) {
        if (connection.isAuthenticated()) {
            return UrlTools.createUserAuthSuccessUrl(
                    connection.getReturnUrl(),
                    String.valueOf(connection.getId()),
                    connection.getAccessToken()
            );
        } else {
            return providerApi.getAuthorizationPageUrl(connection.getAuthSessionSecret());
        }
    }

    private ClientConnectionEntity authenticateClientConnection(ClientConnectionEntity entity, String userId) {
        entity.setAccessToken(CodeBuilder.generateRandomString());
        entity.setAccessTokenExpiresAt(LocalDateTime.now().plusMinutes(ScaSdkConstants.CONNECTION_DEFAULT_ACCESS_TOKEN_MINUTES));
        entity.setUserId(userId);
        return connectionsRepository.save(entity);
    }

    private ClientConnectionEntity createAuthenticationToken(ClientConnectionEntity entity) {
        entity.setAuthToken(CodeBuilder.generateRandomString());
        entity.setAuthTokenExpiresAt(LocalDateTime.now().plusMinutes(ScaSdkConstants.CONNECTION_DEFAULT_AUTH_SESSION_MINUTES));
        return connectionsRepository.save(entity);
    }
}
