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

import com.saltedge.sca.sdk.models.ClientConnection;
import com.saltedge.sca.sdk.models.Consent;
import com.saltedge.sca.sdk.models.api.EncryptedEntity;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.tools.EncryptedEntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.security.PublicKey;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Validated
public class ConsentsService {
    private Logger log = LoggerFactory.getLogger(ConsentsService.class);
    @Autowired
    private ServiceProvider serviceProvider;

    public List<EncryptedEntity> getActiveConsents(@NotNull ClientConnection clientConnection) {
        PublicKey publicKey = clientConnection.getPublicKey();

        List<Consent> consents = serviceProvider.getActiveConsents(clientConnection.getUserId());

        return consents.stream()
                .map(item -> EncryptedEntityFactory.encryptConsent(item, clientConnection.getId(), publicKey))
                .collect(Collectors.toList());
    }


    public boolean revokeConsent(@NotEmpty String consentId, @NotNull ClientConnection connection) {
        return serviceProvider.revokeConsent(connection.getUserId(), consentId);
    }
}
