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
import com.saltedge.sca.sdk.models.api.EncryptedAuthorization;
import com.saltedge.sca.sdk.models.api.requests.EmptyAuthenticatedRequest;
import com.saltedge.sca.sdk.models.api.requests.UpdateAuthorizationRequest;
import com.saltedge.sca.sdk.models.api.responces.AuthorizationResponse;
import com.saltedge.sca.sdk.models.api.responces.AuthorizationsResponse;
import com.saltedge.sca.sdk.models.api.responces.UpdateAuthorizationResponse;
import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsCollector;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * REST Controller designated for serving Service Provider's SCA authorizations
 * @see {https://github.com/saltedge/sca-identity-service-example/blob/master/docs/IDENTITY_SERVICE_API.md#show-authorizations-list}
 */
@RestController
@RequestMapping(AuthorizationsController.AUTHORIZATIONS_REQUEST_PATH)
@Validated
public class AuthorizationsController {
    public final static String AUTHORIZATIONS_REQUEST_PATH = ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH + "/authorizations";
    @Autowired
    private AuthorizationsRepository authorizationsRepository;

    @GetMapping
    public ResponseEntity<AuthorizationsResponse> getAuthorizations(EmptyAuthenticatedRequest request) {
        List<EncryptedAuthorization> result = AuthorizationsCollector.collectActiveAuthorizations(
                authorizationsRepository,
                request.getConnection()
        );
        return ResponseEntity.ok(new AuthorizationsResponse(result));
    }

    @GetMapping("/{" + ScaSdkConstants.KEY_AUTHORIZATION_ID + "}")
    public ResponseEntity<AuthorizationResponse> getAuthorization(
            @PathVariable(ScaSdkConstants.KEY_AUTHORIZATION_ID) @NotNull Long authorizationId,
            EmptyAuthenticatedRequest request
    ) {
        EncryptedAuthorization authorization = AuthorizationsCollector.collectActiveAuthorization(
                authorizationsRepository,
                request.getConnection(),
                authorizationId
        );
        return ResponseEntity.ok(new AuthorizationResponse(authorization));
    }

    @PutMapping("/{" + ScaSdkConstants.KEY_AUTHORIZATION_ID + "}")
    public ResponseEntity<UpdateAuthorizationResponse> updateAuthorization(
            @PathVariable(ScaSdkConstants.KEY_AUTHORIZATION_ID) @NotNull Long authorizationId,
            @Valid UpdateAuthorizationRequest request
    ) {
        AuthorizationEntity authorization = AuthorizationsCollector.findActiveAuthorization(
                authorizationsRepository,
                request.getConnection(),
                authorizationId
        );

        boolean confirmSuccess = authorization.getAuthorizationCode().equals(request.data.authorizationCode);
        if (confirmSuccess) {
            authorization.setConfirmed(request.data.confirm);
            authorizationsRepository.save(authorization);
        }
        return ResponseEntity.ok(new UpdateAuthorizationResponse(confirmSuccess, authorizationId.toString()));
    }
}
