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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

class CreateConnectionRequest(@get:JsonProperty("data") var data: CreateConnectionRequestData? = null)

class CreateConnectionRequestData(
    @get:JsonProperty("public_key") var publicKey: String = "",
    @get:JsonProperty("return_url") var returnUrl: String = "",
    @get:JsonProperty("platform") var platform: String = "",
    @get:JsonProperty("push_token") var pushToken: String = "",
    @get:JsonProperty("connect_query") var connectQuery: String? = null
) {
    @JsonIgnore
    fun isValid(): Boolean {
        return publicKey.isNotEmpty() && returnUrl.isNotEmpty()
    }
}
