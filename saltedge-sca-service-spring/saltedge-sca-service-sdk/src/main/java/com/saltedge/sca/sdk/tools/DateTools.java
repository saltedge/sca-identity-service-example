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
package com.saltedge.sca.sdk.tools;

import java.time.Instant;

public class DateTools {

    /**
     * Checks if Expiration Datetime is passed
     *
     * @param expirationDatetime Instant time
     * @return true if expirationDatetime is before now
     */
    public static boolean dateIsExpired(Instant expirationDatetime) {
        return expirationDatetime != null && expirationDatetime.isBefore(Instant.now());
    }

    /**
     * Gets the number of seconds from the Java epoch of 1970-01-01T00:00:00Z.
     *
     * @return seconds count
     */
    public static Integer nowUtcSeconds() {
        return Math.toIntExact((Instant.now().getEpochSecond()));
    }
}
