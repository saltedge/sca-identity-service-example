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
import com.saltedge.sca.sdk.models.api.ScaEncryptedEntity;
import com.saltedge.sca.sdk.models.api.requests.DefaultAuthenticatedRequest;
import com.saltedge.sca.sdk.models.api.responces.ScaCollectionResponse;
import com.saltedge.sca.sdk.models.api.responces.ScaRevokeConsentResponse;
import com.saltedge.sca.sdk.services.ConsentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

/**
 * REST Controller designated for serving Consent Management
 */
@RestController
@RequestMapping(ConsentsController.CONSENTS_REQUEST_PATH)
public class ConsentsController {
    public final static String CONSENTS_REQUEST_PATH = ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH + "/consents";
    @Autowired
    protected ConsentsService consentsService;

    /**
     * Collect and return active consents
     *
     * @param request with Connection entity
     * @return response with collection with EncryptedEntity
     */
    @GetMapping
    public ResponseEntity<ScaCollectionResponse<ScaEncryptedEntity>> getActiveConsents(DefaultAuthenticatedRequest request) {
        return ResponseEntity.ok(new ScaCollectionResponse<>(consentsService.getActiveConsents(request.getConnection())));
    }

    /**
     * Revoke Consent (mark as revoked) and return operation result
     *
     * @param consentId unique Consent identifier
     * @param request with Connection entity
     * @return response with RevokeConsentResponse object
     */
    @DeleteMapping("/{" + ScaSdkConstants.KEY_ID + "}")
    public ResponseEntity<ScaRevokeConsentResponse> revokeConsent(
            @PathVariable(ScaSdkConstants.KEY_ID) @NotEmpty String consentId,
            DefaultAuthenticatedRequest request
    ) {
        boolean result = consentsService.revokeConsent(consentId, request.getConnection());
        return ResponseEntity.ok(new ScaRevokeConsentResponse(result, consentId));
    }
}
