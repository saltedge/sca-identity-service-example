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

data class ConfigurationResponse(@get:JsonProperty("data") val data: Configuration)

data class Configuration(
    @get:JsonProperty("connect_url") val connectUrl: String,
    @get:JsonProperty("code") val code: String = "spring-demobank",
    @get:JsonProperty("name") var name: String = "Spring Demobank",
    @get:JsonProperty("logo_url") val logoUrl: String = "https://s3-media1.fl.yelpcdn.com/bphoto/9J0LUrYkKYuwcICwQztkxw/ls.jpg",
    @get:JsonProperty("support_email") val supportEmail: String = "support@spring-demobank.com",
    @get:JsonProperty("version") val version: String =  "1"
)
