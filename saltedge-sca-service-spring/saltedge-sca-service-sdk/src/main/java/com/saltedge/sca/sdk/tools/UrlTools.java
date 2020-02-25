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

import com.google.common.collect.ImmutableMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

public class UrlTools {
    public final static String DEFAULT_AUTHENTICATOR_RETURN_TO = "authenticator://oauth/redirect";

    public static String createUserAuthSuccessUrl(String returnUrl, String connectionId, String accessToken) {
        String returnUrlValue = StringUtils.isEmpty(returnUrl) ? DEFAULT_AUTHENTICATOR_RETURN_TO : returnUrl;
        if (StringUtils.isEmpty(connectionId) || StringUtils.isEmpty(accessToken)) return returnUrlValue;
        else return addParamsToUrl(returnUrlValue, ImmutableMap.of(KEY_ID, connectionId, KEY_ACCESS_TOKEN, accessToken));
    }

    public static String createUserAuthErrorUrl(String returnUrl, String errorClass, String errorMessage) {
        String returnUrlValue = StringUtils.isEmpty(returnUrl) ? DEFAULT_AUTHENTICATOR_RETURN_TO : returnUrl;
        String errorClassValue = StringUtils.isEmpty(errorClass) ? "UnknownError" : errorClass;
        String errorMessageValue = errorMessage == null ? "" : errorMessage;
        return addParamsToUrl(returnUrlValue, ImmutableMap.of(KEY_ERROR_CLASS, errorClassValue, KEY_ERROR_MESSAGE, errorMessageValue));
    }

    private static String addParamsToUrl(String url, Map<String, String> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        params.forEach(builder::queryParam);
        return builder.toUriString();
    }
}
