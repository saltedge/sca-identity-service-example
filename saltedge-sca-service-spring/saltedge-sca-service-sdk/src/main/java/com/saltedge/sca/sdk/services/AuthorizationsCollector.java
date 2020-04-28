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

import com.saltedge.sca.sdk.errors.NotFound;
import com.saltedge.sca.sdk.models.api.EncryptedEntity;
import com.saltedge.sca.sdk.tools.EncryptedEntityFactory;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;

import java.security.PublicKey;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Collect methods for SCA Authorizations
 */
public class AuthorizationsCollector {

    /**
     * Collects Active (not expired and not confirmed) Authorization entities by User ID and encrypts them.
     *
     * @param authorizationsRepository
     * @param connection to Client Authenticator with RSA Public Key
     * @return list of Encrypted Authorizations
     */
    public static List<EncryptedEntity> collectActiveAuthorizations(
            AuthorizationsRepository authorizationsRepository,
            ClientConnectionEntity connection
    ) {
        PublicKey publicKey = connection.getPublicKey();

        List<AuthorizationEntity> authorizations = authorizationsRepository.findByUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
                connection.getUserId(),
                Instant.now()
        );

        return authorizations.stream()
                .map(item -> EncryptedEntityFactory.encryptAuthorization(item, connection.getId(), publicKey))
                .collect(Collectors.toList());
    }

    /**
     * Collects Active (not expired and not confirmed) Authorization entity by ID and User ID, and encrypts it.
     *
     * @param authorizationsRepository
     * @param connection
     * @param authorizationId of required entity
     * @return EncryptedAuthorization object
     */
    public static EncryptedEntity collectActiveAuthorization(
            AuthorizationsRepository authorizationsRepository,
            ClientConnectionEntity connection,
            Long authorizationId
    ) {
        PublicKey publicKey = connection.getPublicKey();
        AuthorizationEntity authorization = findActiveAuthorization(authorizationsRepository, connection, authorizationId);
        return EncryptedEntityFactory.encryptAuthorization(authorization, connection.getId(), publicKey);
    }

    /**
     * Collects Active (not expired and not confirmed) Authorization entity by ID and User ID
     *
     * @param authorizationsRepository
     * @param connection
     * @param authorizationId
     * @return AuthorizationEntity object
     */
    public static AuthorizationEntity findActiveAuthorization(
            AuthorizationsRepository authorizationsRepository,
            ClientConnectionEntity connection,
            Long authorizationId
    ) {
        AuthorizationEntity authorization = authorizationsRepository.findFirstByIdAndUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
                authorizationId,
                connection.getUserId(),
                Instant.now()
        );
        if (authorization == null) throw new NotFound.AuthorizationNotFound();
        return authorization;
    }
}
