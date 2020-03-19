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

import com.saltedge.sca.example.model.User
import com.saltedge.sca.example.services.UsersService
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_CONNECTION_ID
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_USER_ID
import com.saltedge.sca.sdk.services.ScaSdkService
import com.saltedge.sca.sdk.tools.CodeBuilder
import freemarker.template.Configuration
import freemarker.template.Template
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer
import java.io.StringWriter
import java.io.Writer
import java.time.LocalDateTime

const val DASHBOARD_PATH = "/users/dashboard"

@Controller
@RequestMapping(DASHBOARD_PATH)
class UserDashboardController {
    @Autowired
    private lateinit var usersService: UsersService
    @Autowired
    private lateinit var scaSdkService: ScaSdkService

    @Autowired
    private lateinit var freeMarkerConfig: FreeMarkerConfigurer

    companion object {
        fun redirectToDashboard(userId: Long): ModelAndView = ModelAndView("redirect:$DASHBOARD_PATH?user_id=$userId")

        fun createDashboardLink(userId: Long): String = "$DASHBOARD_PATH?user_id=$userId"
    }

    @GetMapping
    fun showDashboard(
            @RequestParam(value = KEY_USER_ID, required = false) userId: Long?
    ): ModelAndView {
        val user = userId?.let { usersService.findUser(it) } ?: return ModelAndView("users_dashboard_denied")
        return ModelAndView("users_dashboard").addDashboardData(user)
    }

    @PostMapping
    fun updateDashboard(
            @RequestParam(value = KEY_USER_ID) userId: Long,
            @RequestParam(value = KEY_CONNECTION_ID) connectionId: Long?,
            @RequestParam("action") actionCode: String?
    ): ModelAndView {
        val user = usersService.findUser(userId) ?: return ModelAndView("users_dashboard_denied")

        when (actionCode) {
            "revoke_connection" -> revokeConnection(connectionId = connectionId)
            "create_authorization" -> createAuthorization(user = user)
        }
        return redirectToDashboard(userId)
    }

    private fun ModelAndView.addDashboardData(user: User): ModelAndView {
        val connections = scaSdkService.getClientConnections(user.id.toString())
        val userConnectSecret = usersService.getOrCreateUserConnectSecret(user.id)
        val appLink: String = scaSdkService.createConnectAppLink(userConnectSecret)
        val authorizations = scaSdkService.getAllAuthorizations(user.id.toString()).sortedByDescending { it.id }
        this.addObject("user", user)
                .addObject("connections", connections)
                .addObject("authenticator_link", appLink)
                .addObject("qr_img_src", "$SCA_CONNECT_QR_PATH?$KEY_USER_ID=${user.id}")
                .addObject("authorizations", authorizations)
        return this
    }

    private fun revokeConnection(connectionId: Long?) {
        connectionId?.let { scaSdkService.revokeConnection(it) }
    }

    private fun createAuthorization(user: User) {
        val amount = (1 until 200).random().toDouble()
        val amountString = "%.2f".format(amount) + " EUR"
        val fromAccount = "GB1234567890"
        val toAccount = "Salt Edge Payment Processor"
        val textDescription = createTextDescription(amountString, fromAccount, toAccount)

        scaSdkService.createAuthorization(
                user.id.toString(),
                CodeBuilder.generateRandomString(),
                "Demo Payment for $amountString",
                createHTMLDescription(amountString, fromAccount, toAccount, textDescription)
        )
    }

    private fun createTextDescription(amountString: String, fromAccount: String, toAccount: String): String {
        return "Confirm payment of $amountString from account $fromAccount to $toAccount"
    }

    private fun createHTMLDescription(
            amountString: String,
            fromAccount: String,
            toAccount: String,
            textDescription: String
    ): String {
        return try {
            val dataMap = mapOf(
                    "amount" to amountString,
                    "from_account" to fromAccount,
                    "to_account" to toAccount,
                    "payment_description" to textDescription,
                    "date" to LocalDateTime.now().toString()
            )

            val templateName = "payment_authorization.ftl"
            val configuration = Configuration(Configuration.VERSION_2_3_29)
            configuration.setClassForTemplateLoading(this.javaClass, "/templates/");
            val temp: Template = configuration.getTemplate(templateName)
            val out: Writer = StringWriter()
            temp.process(dataMap, out)
            out.toString()
        } catch(e: Exception) {
            e.printStackTrace()
            textDescription
        }
    }
}
