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
package com.saltedge.sca.example.controller

import com.saltedge.sca.sdk.ScaSdkConstants
import com.saltedge.sca.sdk.provider.ScaSDKCallbackService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

const val AUTHORIZATIONS_PATH = "/authorizations"

@Controller
@RequestMapping(AUTHORIZATIONS_PATH)
class PendingAuthorizationsController {
    private val log = LoggerFactory.getLogger(PaymentsController::class.java)
    @Autowired
    private lateinit var scaSdkService: ScaSDKCallbackService

    private val template = "payments_order"

    @GetMapping("/{" + ScaSdkConstants.KEY_AUTHORIZATION_ID + "}")
    fun showAuthorization(
        @PathVariable(ScaSdkConstants.KEY_AUTHORIZATION_ID) authorizationId: Long
    ): ModelAndView {
        val authorization = scaSdkService.getAuthorizationById(authorizationId)
        return ModelAndView("container").addObject("data", authorization?.description ?: "NO DATA")
    }
}
