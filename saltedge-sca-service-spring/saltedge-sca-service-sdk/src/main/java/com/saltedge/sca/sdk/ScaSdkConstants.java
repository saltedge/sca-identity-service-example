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
package com.saltedge.sca.sdk;

public class ScaSdkConstants {
    public final static String AUTHENTICATOR_API_BASE_PATH = "/api/authenticator/v1";
    public final static String APP_LINK_PREFIX_CONNECT = "authenticator://saltedge.com/connect";
    public final static String APP_LINK_PREFIX_ACTION = "authenticator://saltedge.com/action";

    public final static String HEADER_KEY_ACCESS_TOKEN = "access-token";
    public final static String HEADER_KEY_EXPIRES_AT = "expires-at";
    public final static String HEADER_KEY_SIGNATURE = "signature";

    public final static String KEY_DATA = "data";
    public final static String KEY_ERROR_MESSAGE = "error_message";
    public final static String KEY_ERROR_CLASS = "error_class";
    public final static String KEY_ID = "id";
    public final static String KEY_USER_ID = "user_id";
    public final static String KEY_SECRET = "secret";
    public final static String KEY_ACCESS_TOKEN = "access_token";
    public final static String KEY_CONNECTION_ID = "connection_id";
    public final static String KEY_AUTHORIZATION_ID = "authorization_id";
    public final static String KEY_AUTHORIZATION_CODE = "authorization_code";
    public final static String KEY_ACTION_UUID = "action_uuid";
    public final static String KEY_PLATFORM = "platform";
    public final static String KEY_PUSH_TOKEN = "push_token";
    public final static String KEY_TITLE = "title";
    public final static String KEY_EXPIRES_AT = "expires_at";
    public final static String KEY_CREATED_AT = "created_at";
    public final static String KEY_CONNECT_URL = "connect_url";
    public final static String KEY_CONNECT_QUERY = "connect_query";
    public final static String KEY_SUCCESS = "success";

    public final static int AUTHORIZATION_DEFAULT_LIFETIME_MINUTES = 5;
    public final static int CONNECTION_DEFAULT_AUTH_SESSION_MINUTES = 5;
}
