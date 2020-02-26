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

import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.models.api.requests.Notification;
import com.saltedge.sca.sdk.models.api.requests.NotificationsRequest;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionsRepository;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.tools.EnvironmentTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ClientNotificationService {
    private Logger log = LoggerFactory.getLogger(ClientNotificationService.class);
    @Autowired
    Environment env;
    @Autowired
    ClientConnectionsRepository connectionsRepository;
    @Autowired
    private ServiceProvider providerApi;
    @Autowired
    private RestTemplate restTemplate;

    public void sendNotificationsForUser(String userId, Authorization authorization) {
        List<ClientConnection> connections = connectionsRepository.findByUserIdAndRevokedFalse(userId)
                .stream().map(item -> (ClientConnection) item).collect(Collectors.toList());
        sendNotificationForConnections(connections, authorization);
    }

    public void sendNotificationForConnections(List<ClientConnection> connections, Authorization authorization) {
        String providerName = providerApi.getProviderName();
        String pushServiceUrl = EnvironmentTools.getPushServiceUrl(env);
        String pushServiceAppId = EnvironmentTools.getPushAppId(env);
        String pushServiceAppSecret = EnvironmentTools.getPushAppSecret(env);

        if (StringUtils.isEmpty(pushServiceUrl) && StringUtils.isEmpty(pushServiceAppId) && StringUtils.isEmpty(pushServiceAppSecret)) {
            log.error("Authorization Notification [id: " + authorization.getId() + "] Error. No valid Push Service Params");
        } else {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json");
            headers.add("App-id", pushServiceAppId);
            headers.add("App-secret", pushServiceAppSecret);
            NotificationsRequest body = new NotificationsRequest(createNotificationsList(connections, authorization, providerName));
            HttpEntity<NotificationsRequest> request = new HttpEntity<>(body, headers);

            try {
                ResponseEntity<String> result = restTemplate.postForEntity(Objects.requireNonNull(pushServiceUrl), request, String.class);
            } catch (Exception e) {
                log.error("ClientNotificationService", e);
            }
        }
    }

    private List<Notification> createNotificationsList(List<ClientConnection> connections, Authorization authorization, String providerName) {
        return connections.stream()
                .filter(item -> !StringUtils.isEmpty(item.getPushToken()))
                .map(item -> createNotification(item, authorization, providerName))
                .collect(Collectors.toList());
    }

    private Notification createNotification(ClientConnection connection, Authorization authorization, String providerName) {
        return new Notification(
                "Authorization Request",
                providerName + " is requesting authorization. Tap to proceed.",
                connection.getPushToken(),
                connection.getPlatform(),
                connection.getId().toString(),
                authorization.getId().toString(),
                authorization.getExpiresAtUTC()
        );
    }

    @Bean
    RestTemplate rest() {
        return new RestTemplate();
    }
}
