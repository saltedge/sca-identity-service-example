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
package com.saltedge.sca.sdk.models.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.models.api.EncryptedAuthorization;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.tools.CryptTools;

import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

public class AuthorizationConverter {
    public static EncryptedAuthorization encryptAuthorization(AuthorizationEntity authorization, Long connectionId, PublicKey publicKey) {
        Map<String, String> authorizationHash = new HashMap<>();
        authorizationHash.put(ScaSdkConstants.KEY_ID, String.valueOf(authorization.getId()));
        authorizationHash.put(ScaSdkConstants.KEY_CONNECTION_ID, String.valueOf(connectionId));
        authorizationHash.put(KEY_TITLE, authorization.getTitle());
        authorizationHash.put("description", authorization.getDescription());
        authorizationHash.put(KEY_AUTHORIZATION_CODE, authorization.getAuthorizationCode());
        authorizationHash.put(KEY_CREATED_AT, authorization.getCreatedAtUTC());
        authorizationHash.put(KEY_EXPIRES_AT, authorization.getExpiresAtUTC());

        EncryptedAuthorization encryptedAuthorization;
        try {
            String authorizationJson = new ObjectMapper().writeValueAsString(authorizationHash);
            encryptedAuthorization = createEncryptedAuthorization(authorizationJson, publicKey);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            encryptedAuthorization = new EncryptedAuthorization();
        }
        encryptedAuthorization.id = String.valueOf(authorization.getId());
        encryptedAuthorization.connectionId = String.valueOf(connectionId);
        return encryptedAuthorization;
    }

    public static EncryptedAuthorization createEncryptedAuthorization(String data, PublicKey publicKey) {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);

        return new EncryptedAuthorization(
                "AES-256-CBC",
                Base64.getEncoder().encodeToString(CryptTools.encryptRsa(key, publicKey)),
                Base64.getEncoder().encodeToString(CryptTools.encryptRsa(iv, publicKey)),
                Base64.getEncoder().encodeToString(CryptTools.encryptAes(data, key, iv))
        );
    }
}
