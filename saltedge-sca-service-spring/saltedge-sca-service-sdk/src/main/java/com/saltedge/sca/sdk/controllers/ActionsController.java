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
import com.saltedge.sca.sdk.models.api.requests.EmptyAuthenticatedRequest;
import com.saltedge.sca.sdk.models.api.responces.ActionResponse;
import com.saltedge.sca.sdk.services.AuthenticateActionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;

/**
 * REST Controller designated for serving SCA Actions
 */
@RestController
@RequestMapping(ActionsController.ACTIONS_REQUEST_PATH)
public class ActionsController {
    public final static String ACTIONS_REQUEST_PATH = ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH + "/action";
    @Autowired
    protected AuthenticateActionsService actionsService;

    /**
     * Receives request of authenticated action
     *
     * @param actionUUID unique action identifier
     * @param request with Connection entity
     * @return ActionResponse response
     */
    @PostMapping("/{" + ScaSdkConstants.KEY_ACTION_UUID + "}")
    public ResponseEntity<ActionResponse> onSubmitAction(
            @PathVariable(ScaSdkConstants.KEY_ACTION_UUID) @NotEmpty String actionUUID,
            EmptyAuthenticatedRequest request
    ) {
        ActionResponse result = actionsService.onNewAuthenticatedAction(actionUUID, request.getConnection());
        return ResponseEntity.ok(result);
    }
}
