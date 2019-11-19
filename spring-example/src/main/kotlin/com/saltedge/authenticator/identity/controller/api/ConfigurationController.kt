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
package com.saltedge.authenticator.identity.controller.api

import com.saltedge.authenticator.identity.AUTHENTICATOR_API_BASE_PATH
import com.saltedge.authenticator.identity.model.mapping.Configuration
import com.saltedge.authenticator.identity.model.mapping.ConfigurationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

const val CONFIGURATION_REQUEST_PATH: String = "$AUTHENTICATOR_API_BASE_PATH/configuration"

@RestController
@RequestMapping(CONFIGURATION_REQUEST_PATH)
class ConfigurationController {

    @GetMapping
    fun getConfiguration(request: HttpServletRequest): ResponseEntity<ConfigurationResponse> {
        return ResponseEntity.ok(ConfigurationResponse(Configuration(connectUrl = "https://${request.serverName}")))
    }
}
