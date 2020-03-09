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
package com.saltedge.sca.sdk.controllers;

import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.models.api.requests.CreateConnectionRequest;
import com.saltedge.sca.sdk.models.api.requests.EmptyAuthenticatedRequest;
import com.saltedge.sca.sdk.models.api.responces.CreateConnectionResponse;
import com.saltedge.sca.sdk.models.api.responces.RevokeConnectionResponse;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.services.ClientConnectionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller designated for serving Service Provider's SCA Authenticator connections
 */
@RestController
@RequestMapping(ConnectionsController.CONNECTIONS_REQUEST_PATH)
@Validated
class ConnectionsController {
    public final static String CONNECTIONS_REQUEST_PATH = ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH + "/connections";
    @Autowired
    private ClientConnectionsService connectionsService;

    /**
     * Create the new Client related model (i.e. Client Connection) and return Connect URL for future user authentication.
     *
     * @param newConnectionRequest
     * @return response
     */
    @PostMapping
    public ResponseEntity<CreateConnectionResponse> createConnection(
            @Valid @RequestBody CreateConnectionRequest newConnectionRequest
    ) {
        if (newConnectionRequest == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        String authorizationSessionSecret = newConnectionRequest.getData().getConnectQuery();
        CreateConnectionResponse result = connectionsService.createConnection(
                newConnectionRequest.getData(),
                authorizationSessionSecret
        );
        return ResponseEntity.ok(result);
    }

    @DeleteMapping
    public ResponseEntity<RevokeConnectionResponse> revokeConnection(EmptyAuthenticatedRequest request) {
        ClientConnectionEntity connection = request.getConnection();
        connectionsService.revokeConnection(connection);
        return ResponseEntity.ok(new RevokeConnectionResponse(true, connection.getAccessToken()));
    }
}
