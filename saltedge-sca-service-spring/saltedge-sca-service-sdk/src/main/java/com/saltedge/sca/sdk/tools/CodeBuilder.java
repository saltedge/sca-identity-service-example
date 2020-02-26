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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;

public class CodeBuilder {
    public static final String DEFAULT_SALT = CodeBuilder.generateRandomString(16);

    public static String generateRandomString() {
        return generateRandomString(32);
    }

    public static String generateRandomString(Integer size) {
        int arraySize = (size == null) ? 32 : size;
        byte[] array = new byte[arraySize];
        new Random().nextBytes(array);
        return new String(Base64.getUrlEncoder().encode(array)).substring(0, arraySize);
    }

    public static String generatePaymentAuthorizationCode(
            String payeeDetails,
            String amount,
            Long createdAt,
            String userId,
            String description
    ) {
        String templateString = payeeDetails + "|" + amount + "|" + createdAt + "|" + userId + "|" + description + "|" + DEFAULT_SALT;
        byte[] hashBytes = templateString.getBytes(StandardCharsets.UTF_8);
        try {
            hashBytes = MessageDigest.getInstance("SHA-256").digest(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().withoutPadding().encodeToString(hashBytes);
    }

    public static String generateAuthorizationCode(String userId, String title, String description, Long timeStamp) {
        String templateString = title + "|" + timeStamp + "|" + userId + "|" + description + "|" + DEFAULT_SALT;
        byte[] hashBytes = templateString.getBytes(StandardCharsets.UTF_8);
        try {
            hashBytes = MessageDigest.getInstance("SHA-256").digest(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
