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
package com.saltedge.sca.sdk.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.models.Authorization;
import com.saltedge.sca.sdk.models.Consent;
import com.saltedge.sca.sdk.models.api.EncryptedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

public class EncryptedEntityFactory {
    private static Logger log = LoggerFactory.getLogger(EncryptedEntityFactory.class);
    /**
     * Encrypt Authorization and create EncryptedEntity with encrypted data
     *
     * @param authorization object needs to be encrypted
     * @param connectionId unique ID of Connection object
     * @param publicKey material is used to encrypt KEY, IV resulting fields
     * @return EncryptedEntity object
     */
    public static EncryptedEntity encryptAuthorization(
            Authorization authorization,
            Long connectionId,
            PublicKey publicKey
    ) {
        try {
            Map<String, String> authorizationHash = new HashMap<>();
            authorizationHash.put(ScaSdkConstants.KEY_ID, String.valueOf(authorization.getId()));
            authorizationHash.put(ScaSdkConstants.KEY_CONNECTION_ID, String.valueOf(connectionId));
            authorizationHash.put(KEY_TITLE, authorization.getTitle());
            authorizationHash.put(KEY_DESCRIPTION, authorization.getDescription());
            authorizationHash.put(KEY_AUTHORIZATION_CODE, authorization.getAuthorizationCode());
            authorizationHash.put(KEY_CREATED_AT, authorization.getCreatedAt().toString());
            authorizationHash.put(KEY_EXPIRES_AT, authorization.getExpiresAt().toString());

            return createEncryptedEntity(
                    String.valueOf(authorization.getId()),
                    String.valueOf(connectionId),
                    authorizationHash,
                    publicKey
            );
        } catch (Exception e) {
            log.error("encryptAuthorization", e);
            return null;
        }
    }

    /**
     * Encrypt Consent and create EncryptedEntity with encrypted data
     *
     * @param consent object needs to be encrypted
     * @param connectionId unique ID of Connection object
     * @param publicKey material is used to encrypt KEY, IV resulting fields
     * @return EncryptedEntity object
     */
    public static EncryptedEntity encryptConsent(
            Consent consent,
            Long connectionId,
            PublicKey publicKey
    ) {
        return createEncryptedEntity(
                String.valueOf(consent.getId()),
                String.valueOf(connectionId),
                consent,
                publicKey
        );
    }

    private static EncryptedEntity createEncryptedEntity(
            String entityId,
            String connectionId,
            Object dataObject,
            PublicKey publicKey
    ) {
        EncryptedEntity encryptedEntity;
        try {
            String jsonString = JsonTools.createDefaultMapper().writeValueAsString(dataObject);
            encryptedEntity = createEncryptedEntity(jsonString, publicKey);
        } catch (JsonProcessingException e) {
            log.error("createEncryptedEntity", e);
            encryptedEntity = new EncryptedEntity();
        }
        encryptedEntity.id = entityId;
        encryptedEntity.connectionId = connectionId;
        return encryptedEntity;
    }

    private static EncryptedEntity createEncryptedEntity(String data, PublicKey publicKey) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        return new EncryptedEntity(
                "AES-256-CBC",
                Base64.getEncoder().encodeToString(CryptTools.encryptRsa(key, publicKey)),
                Base64.getEncoder().encodeToString(CryptTools.encryptRsa(iv, publicKey)),
                Base64.getEncoder().encodeToString(CryptTools.encryptAes(data, key, iv))
        );
    }
}
