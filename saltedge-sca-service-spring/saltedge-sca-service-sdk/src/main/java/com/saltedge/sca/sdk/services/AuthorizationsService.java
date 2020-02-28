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
import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.api.EncryptedAuthorization;
import com.saltedge.sca.sdk.models.converter.AuthorizationConverter;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.tools.CodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class AuthorizationsService {
    private Logger log = LoggerFactory.getLogger(AuthorizationsService.class);
    @Autowired
    private AuthorizationsRepository authorizationsRepository;
    @Autowired
    private ClientNotificationService clientNotificationService;
    @Autowired
    private ServiceProvider serviceProvider;

    public Authorization createAuthorization(
            @NotEmpty String userId,
            @NotEmpty String confirmationCode,
            @NotEmpty String title,
            @NotEmpty String description
    ) {
        Authorization authorization = createAndSaveAuthorization(userId, confirmationCode, title, description);
        clientNotificationService.sendNotificationsForUser(userId, authorization);
        return authorization;
    }

    public List<Authorization> getAllAuthorizations(@NotEmpty String userId) {
        return authorizationsRepository.findByUserId(userId).stream().map(item -> (Authorization) item).collect(Collectors.toList());
    }

    public Authorization getAuthorization(Long authorizationId) {
        if (StringUtils.isEmpty(authorizationId)) return null;
        return authorizationsRepository.findById(authorizationId).orElse(null);
    }

    public List<EncryptedAuthorization> getActiveAuthorizations(@NotNull ClientConnectionEntity connection) {
        return AuthorizationsCollector.collectActiveAuthorizations(authorizationsRepository, connection);
    }

    public EncryptedAuthorization getActiveAuthorization(@NotNull ClientConnectionEntity connection, @NotNull Long authorizationId) {
        return AuthorizationsCollector.collectActiveAuthorization(authorizationsRepository, connection, authorizationId);
    }

    public boolean confirmAuthorization(
            @NotNull ClientConnectionEntity connection,
            @NotNull Long authorizationId,
            @NotNull String authorizationCode,
            @NotNull boolean confirmAuthorization
    ) {
        AuthorizationEntity authorization = AuthorizationsCollector.findActiveAuthorization(
                authorizationsRepository,
                connection,
                authorizationId
        );

        boolean canUpdateAuthorization = authorization.getAuthorizationCode().equals(authorizationCode);
        if (canUpdateAuthorization) {
            authorization.setConfirmed(confirmAuthorization);
            authorizationsRepository.save(authorization);
            serviceProvider.onAuthorizationConfirmed(authorization);
        }
        return canUpdateAuthorization;
    }

    private Authorization createAndSaveAuthorization(
            String userId,
            String confirmationCode,
            String title,
            String description
    ) {
        String titleValue = (title == null) ? "Authorization Request" : title;
        String descriptionValue = (description == null) ? "Confirm your identity" : description;
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(ScaSdkConstants.AUTHORIZATION_DEFAULT_LIFETIME_MINUTES);
        AuthorizationEntity model = new AuthorizationEntity(
                titleValue,
                descriptionValue,
                expiresAt,
                confirmationCode,
                userId
        );
        return authorizationsRepository.save(model);
    }
}
