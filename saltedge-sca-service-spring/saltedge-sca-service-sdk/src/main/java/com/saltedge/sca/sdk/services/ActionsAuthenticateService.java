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

import com.saltedge.sca.sdk.errors.BadRequest;
import com.saltedge.sca.sdk.errors.NotFound;
import com.saltedge.sca.sdk.models.api.responces.ActionResponse;
import com.saltedge.sca.sdk.models.persistent.AuthenticateActionEntity;
import com.saltedge.sca.sdk.models.persistent.AuthenticateActionsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Service
@Validated
public class ActionsAuthenticateService {
    private final Logger log = LoggerFactory.getLogger(ActionsAuthenticateService.class);
    @Autowired
    private AuthenticateActionsRepository actionsRepository;
    @Autowired
    private ServiceProvider serviceProvider;

    public ActionResponse onNewAuthenticatedAction(@NotEmpty String actionUUID, @NotNull ClientConnectionEntity connection) throws NotFound.ActionNotFound {
        AuthenticateActionEntity action = actionsRepository.findFirstByUuid(actionUUID);
        if (action == null) throw new NotFound.ActionNotFound();
        if (action.isExpired()) throw new BadRequest.ActionExpired();

        action.setUserId(connection.getUserId());
        actionsRepository.save(action);

        Long authorizationId = serviceProvider.onAuthenticateAction(action);

        if (authorizationId != null) {
            return new ActionResponse(true, String.valueOf(connection.getId()), String.valueOf(authorizationId));
        } else {
            return new ActionResponse(false, null, null);
        }
    }
}
