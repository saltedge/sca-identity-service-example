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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTools {
    /**
     * Converts LocalDateTime to String with datetime in ISO 8601 format
     * Quoted "Z" to indicate UTC, no timezone offset
     *
     * @param date LocalDateTime
     * @return datetime string
     */
    public static String convertDateToIso8601(LocalDateTime date) {
        LocalDateTime utc = date.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
        return utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
    }

    /**
     * Checks if Expiration Datetime is passed
     *
     * @param expirationDatetime LocalDateTime
     * @return true if expirationDatetime is before now
     */
    public static boolean dateIsExpired(LocalDateTime expirationDatetime) {
        return expirationDatetime != null && expirationDatetime.isBefore(LocalDateTime.now());
    }

    /**
     * Get current UTC time
     *
     * @return current UTC seconds (from 01-01-1970)
     */
    public static Integer nowUtcSeconds() {
        return Math.toIntExact((new Date().getTime() / 1000L));
    }
}
