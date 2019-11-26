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
package com.saltedge.authenticator.identity.controller.admin

import com.saltedge.authenticator.identity.model.*
import com.saltedge.authenticator.identity.model.mapping.NotificationsRequest
import com.saltedge.authenticator.identity.tools.generateAuthorizationCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.ModelAndView
import java.util.*

@Controller
@RequestMapping(value = ["/admin/authorizations"])
class AuthorizationsAdminController {
    @Autowired
    private var usersRepository: UsersRepository? = null
    @Autowired
    private var connectionsRepository: ConnectionsRepository? = null
    @Autowired
    private var authorizationsRepository: AuthorizationsRepository? = null
    @Autowired
    private val env: Environment? = null
    @Autowired
    private val restTemplate: RestTemplate? = null

    @PostMapping
    fun createDemoAuthorization(
        @RequestParam("user_id") userId: Long,
        @RequestParam title: String? = null,
        @RequestParam description: String? = null
    ): ModelAndView {
        usersRepository?.findById(userId)?.get()?.let { user ->
            createAuthorizationModel(
                title = title ?: "Payment for 100.00 EUR",
                description = description ?: "Confirm payment 100.00 EUR from account GB1234567890 to Salt Edge Air Ticket Processor",
                user = user
            ).apply {
                authorizationsRepository?.save(this)
                sendAuthorizationNotifications(authorization = this, user = user)
            }
        }
        return ModelAndView("redirect:/admin/users?user_id=$userId")
    }

    private fun createAuthorizationModel(title: String, description: String, user: User): Authorization {
        val createdAt: Long = Date().time

        return Authorization(
            title = title,
            description = description,
            user = user,
            createdAt = createdAt,
            authorizationCode = generateAuthorizationCode(
                title = title,
                description = description,
                userId = user.id ?: 0,
                createdAt = createdAt
            )
        )
    }

    private fun sendAuthorizationNotifications(authorization: Authorization, user: User) {
        val connections = connectionsRepository?.findByUserAndRevokedFalse(user) ?: return
        sendAuthorizationNotifications(connections, authorization)
    }

    private fun sendAuthorizationNotifications(connections: List<Connection>, authorization: Authorization) {
        val pushServiceUrl = env?.getProperty("push_service.url") ?: return
        val pushServiceAppId = env.getProperty("push_service.app_id") ?: return
        val pushServiceAppSecret = env.getProperty("push_service.app_secret") ?: return

        if (pushServiceUrl.isNotEmpty() && pushServiceAppId.isNotEmpty() && pushServiceAppSecret.isNotEmpty()) {
            val result = restTemplate?.postForEntity(pushServiceUrl, NotificationsRequest(connections, authorization), String::class.java)
            println("sendAuthorizationNotifications result:${result?.statusCode}")
        } else {
            println("No valid Push Service Params")
        }
    }

    @Bean
    fun rest(): RestTemplate {
        return RestTemplate()
    }
}
