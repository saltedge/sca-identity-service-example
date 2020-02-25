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
package com.saltedge.sca.sdk.models.persistent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

public interface AuthorizationsRepository extends JpaRepository<AuthorizationEntity, Long> {
    @NonNull
    List<AuthorizationEntity> findByUserId(@NonNull String id);

    @NonNull
    List<AuthorizationEntity> findByUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
            @NonNull String userId,
            @NonNull LocalDateTime currentDate
    );

    AuthorizationEntity findFirstByIdAndUserIdAndExpiresAtGreaterThanAndConfirmedIsNull(
            @NonNull Long id,
            @NonNull String userId,
            @NonNull LocalDateTime currentDate
    );
}
