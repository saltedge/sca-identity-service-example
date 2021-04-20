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

import com.saltedge.sca.sdk.models.persistent.AuthorizationEntity;
import com.saltedge.sca.sdk.models.persistent.AuthorizationsRepository;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Service
@Validated
public class AuthorizationsConfirmService {
  private final Logger log = LoggerFactory.getLogger(AuthorizationsConfirmService.class);
  @Autowired
  private AuthorizationsRepository authorizationsRepository;
  @Autowired
  private ServiceProvider serviceProvider;

  public boolean confirmAuthorization(
    @NotNull ClientConnectionEntity connection,
    @NotNull Long authorizationId,
    @NotNull String authorizationCode,
    @NotNull Boolean confirmAuthorization,
    String geolocation,
    String authorizationType
  ) {
    AuthorizationEntity authorization = AuthorizationsCollector.findActiveAuthorization(
      authorizationsRepository,
      connection,
      authorizationId
    );

    boolean canUpdateAuthorization = authorization.getAuthorizationCode().equals(authorizationCode);
    if (canUpdateAuthorization) {
      authorization.setConfirmed(confirmAuthorization);
      authorization.setConfirmLocation(geolocation);
      authorization.setConfirmAuthorizationType(authorizationType);
      authorizationsRepository.save(authorization);
      serviceProvider.onAuthorizationConfirmed(authorization);
    }
    return canUpdateAuthorization;
  }
}
