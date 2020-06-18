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
import com.saltedge.sca.sdk.models.api.EncryptedEntity;
import com.saltedge.sca.sdk.models.api.requests.DefaultAuthenticatedRequest;
import com.saltedge.sca.sdk.models.api.requests.UpdateAuthorizationRequest;
import com.saltedge.sca.sdk.models.api.responces.AuthorizationResponse;
import com.saltedge.sca.sdk.models.api.responces.CollectionResponse;
import com.saltedge.sca.sdk.models.api.responces.UpdateAuthorizationResponse;
import com.saltedge.sca.sdk.services.AuthorizationsConfirmService;
import com.saltedge.sca.sdk.services.AuthorizationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * REST Controller designated for serving SCA Authorizations
 * @see {https://github.com/saltedge/sca-identity-service-example/blob/master/docs/IDENTITY_SERVICE_API.md#show-authorizations-list}
 */
@RestController
@RequestMapping(AuthorizationsController.AUTHORIZATIONS_REQUEST_PATH)
@Validated
public class AuthorizationsController {
    public final static String AUTHORIZATIONS_REQUEST_PATH = ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH + "/authorizations";
    @Autowired
    private AuthorizationsService authorizationsService;
    @Autowired
    private AuthorizationsConfirmService authorizationsConfirmService;

    @GetMapping
    public ResponseEntity<CollectionResponse<EncryptedEntity>> getActiveAuthorizations(DefaultAuthenticatedRequest request) {
        List<EncryptedEntity> result = authorizationsService.getActiveAuthorizations(request.getConnection());
        return ResponseEntity.ok(new CollectionResponse<>(result));
    }

    @GetMapping("/{" + ScaSdkConstants.KEY_AUTHORIZATION_ID + "}")
    public ResponseEntity<AuthorizationResponse> getActiveAuthorization(
            @PathVariable(ScaSdkConstants.KEY_AUTHORIZATION_ID) @NotNull Long authorizationId,
            DefaultAuthenticatedRequest request
    ) {
        EncryptedEntity authorization = authorizationsService.getActiveAuthorization(request.getConnection(), authorizationId);
        return ResponseEntity.ok(new AuthorizationResponse(authorization));
    }

    @PutMapping("/{" + ScaSdkConstants.KEY_AUTHORIZATION_ID + "}")
    public ResponseEntity<UpdateAuthorizationResponse> confirmAuthorization(
            @PathVariable(ScaSdkConstants.KEY_AUTHORIZATION_ID) @NotNull Long authorizationId,
            @Valid UpdateAuthorizationRequest request
    ) {
        boolean result = authorizationsConfirmService.confirmAuthorization(
                request.getConnection(),
                authorizationId,
                request.data.authorizationCode,
                request.data.confirmAuthorization
        );
        return ResponseEntity.ok(new UpdateAuthorizationResponse(result, authorizationId.toString()));
    }
}
