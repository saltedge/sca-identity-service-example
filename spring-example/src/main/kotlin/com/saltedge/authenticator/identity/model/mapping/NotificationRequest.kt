/*
 * This file is part of the Salt Edge Authenticator distribution
 * (https://github.com/saltedge/sca-identity-service-example).
 * Copyright (c) 2019 Salt Edge Inc.
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
package com.saltedge.authenticator.identity.model.mapping

import com.fasterxml.jackson.annotation.JsonProperty
import com.saltedge.authenticator.identity.model.Authorization
import com.saltedge.authenticator.identity.model.Connection
import com.saltedge.authenticator.identity.tools.toIso8601

class NotificationsRequest(connections: List<Connection>, authorization: Authorization) {
    @get:JsonProperty("data")
    var data: List<NotificationRequest> = connections.filter { it.pushToken.isNotEmpty() }.map { NotificationRequest(it, authorization) }
}

class NotificationRequest(connection: Connection, authorization: Authorization) {
    @get:JsonProperty("title") var title: String = "Authorization Request"
    @get:JsonProperty("body") var body: String = "Spring Service is requesting authorization. Tap to proceed."
    @get:JsonProperty("push_token") var pushToken: String = connection.pushToken
    @get:JsonProperty("platform") var platform: String = connection.platform
    @get:JsonProperty("data") var data: NotificationIds = NotificationIds(
            connectionId = connection.id.toString(),
            authorizationId = authorization.id.toString()
    )
    @get:JsonProperty("expires_at") var expiresAt: String = authorization.expiresAt.toIso8601()

}

class NotificationIds(
        @get:JsonProperty("connection_id") var connectionId: String,
        @get:JsonProperty("authorization_id") var authorizationId: String
)
