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
package com.saltedge.sca.sdk;

import com.saltedge.sca.sdk.tools.KeyTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Scanner;

public class TestTools {
    private final static String publicKeyFileName = "public_key.pem";
    private final static String privateKeyFileName = "private_key.pem";


    public static String getRsaPublicKeyPemString() throws Exception {
        return readKeyFile(publicKeyFileName);
    }

    /**
     * This method read public_key.pem file from resources
     * and converts to RSA PublicKey.
     * @return PublicKey This returns PublicKey from pem file.
     */
    public static PublicKey getRsaPublicKey() throws Exception {
        String rsaPublicKeyString = readKeyFile(publicKeyFileName);
        return KeyTools.convertPemStringToPublicKey(rsaPublicKeyString);
    }

    /**
     * This method read private_key.pem file from resources
     * and converts to RSA PrivateKey.
     * @return PublicKey This returns PrivateKey from pem file.
     */
    public static PrivateKey getRsaPrivateKey() throws Exception {
        String privateKeyPEM = readKeyFile(privateKeyFileName);
        privateKeyPEM = privateKeyPEM.replace("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getMimeDecoder().decode(privateKeyPEM));
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public static String createSignature(
            String requestMethod,
            String requestUrl,
            String expiresAt,
            String requestBody,
            PrivateKey privateKey
    ) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        return createSignature(requestMethod.toLowerCase() + "|" + requestUrl + "|" + expiresAt + "|" + requestBody, privateKey);
    }

    public static String createSignature(String data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sign.sign());
    }

    public static String readKeyFile(String filename) throws FileNotFoundException, ClassNotFoundException {
        StringBuilder result = new StringBuilder("");
        Class cls = Class.forName("com.saltedge.sca.sdk.TestTools");
        ClassLoader classLoader = cls.getClassLoader();
        File file = new File(classLoader.getResource(filename).getFile());

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                result.append(line).append("\n");
            }
        }
        return result.toString();
    }
}
