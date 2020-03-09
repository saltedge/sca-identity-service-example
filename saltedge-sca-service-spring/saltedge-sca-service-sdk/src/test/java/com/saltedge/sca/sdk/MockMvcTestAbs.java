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

import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionsRepository;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.services.AuthenticateActionsService;
import com.saltedge.sca.sdk.services.AuthorizationsService;
import com.saltedge.sca.sdk.services.ClientConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.saltedge.sca.sdk.tools.UrlTools.DEFAULT_AUTHENTICATOR_RETURN_TO;

abstract public class MockMvcTestAbs {
    @Autowired
    protected MockMvc mvc;
    @MockBean
    protected ServiceProvider providerApi;
    @MockBean
    protected ClientConnectionsRepository connectionsRepository;
    @MockBean
    protected ClientConnectionsService connectionsService;
    @MockBean
    protected AuthorizationsService authorizationsService;
    @MockBean
    protected AuthenticateActionsService actionsService;

    protected String publicKey;
    protected ClientConnectionEntity testAuthorizedConnection;
    protected ClientConnectionEntity testConnection;

    public MockMvcTestAbs() {
        try {
            publicKey = TestTools.getRsaPublicKeyPemString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        testAuthorizedConnection = new ClientConnectionEntity();
        testAuthorizedConnection.setId(1L);
        testAuthorizedConnection.setReturnUrl(DEFAULT_AUTHENTICATOR_RETURN_TO);
        testAuthorizedConnection.setAccessToken("access_token");

        testAuthorizedConnection.setPublicKey(publicKey);
        testAuthorizedConnection.setUserId(String.valueOf(1));

        testConnection = new ClientConnectionEntity();
        testConnection.setId(2L);
        testConnection.setReturnUrl(DEFAULT_AUTHENTICATOR_RETURN_TO);
        testConnection.setAuthToken("auth_token");
        testConnection.setPublicKey(publicKey);
    }
}
