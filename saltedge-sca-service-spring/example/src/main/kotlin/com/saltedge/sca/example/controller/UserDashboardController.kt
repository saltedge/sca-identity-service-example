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

import com.saltedge.sca.example.model.ConsentEntity
import com.saltedge.sca.example.model.ConsentsRepository
import com.saltedge.sca.example.model.UserEntity
import com.saltedge.sca.example.services.UsersService
import com.saltedge.sca.example.tools.AuthorizationTemplate
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_CONNECTION_ID
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_USER_ID
import com.saltedge.sca.sdk.models.api.ScaAccount
import com.saltedge.sca.sdk.provider.ScaSDKCallbackService
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

const val DASHBOARD_PATH = "/users/dashboard"

@Controller
@RequestMapping(DASHBOARD_PATH)
class UserDashboardController {
    @Autowired
    private lateinit var freeMarkerConfig: FreeMarkerConfigurer
    @Autowired
    private lateinit var usersService: UsersService
    @Autowired
    private lateinit var scaSdkService: ScaSDKCallbackService
    @Autowired
    private lateinit var consentsRepository: ConsentsRepository

    companion object {
        fun redirectToDashboard(userId: Long): ModelAndView = ModelAndView("redirect:$DASHBOARD_PATH?user_id=$userId")

        fun createDashboardLink(userId: Long): String = "$DASHBOARD_PATH?user_id=$userId"
    }

    @GetMapping
    fun showDashboard(
            @RequestParam(value = KEY_USER_ID, required = false) userId: Long?
    ): ModelAndView {
        return showConnections(userId)
    }

    @GetMapping("/connections")
    fun showConnections(
            @RequestParam(value = KEY_USER_ID, required = false) userId: Long?
    ): ModelAndView {
        val user = userId?.let { usersService.findUser(it) } ?: return ModelAndView("users_dashboard_denied")
        val connections = scaSdkService.getClientConnections(user.id.toString())
        val userConnectSecret = usersService.getOrCreateUserConnectSecret(user.id)
        val appLink: String = scaSdkService.createConnectAppLink(userConnectSecret)
        return ModelAndView("users_dashboard_connections")
                .addObject("user", user)
                .addObject("connections", connections)
                .addObject("authenticator_link", appLink)
                .addObject("qr_img_src", "$SCA_CONNECT_QR_PATH?$KEY_USER_ID=${user.id}")
    }

    @PostMapping("/connections/revoke")
    fun revokeConnection(
            @RequestParam(value = KEY_USER_ID) userId: Long,
            @RequestParam(value = KEY_CONNECTION_ID) connectionId: Long?
    ): ModelAndView {
        val user = usersService.findUser(userId) ?: return ModelAndView("users_dashboard_denied")
        revokeConnection(connectionId = connectionId)
        return ModelAndView("redirect:$DASHBOARD_PATH/connections?user_id=$userId")
    }

    @GetMapping("/authorizations")
    fun showAuthorizations(
            @RequestParam(value = KEY_USER_ID, required = false) userId: Long?
    ): ModelAndView {
        val user = userId?.let { usersService.findUser(it) } ?: return ModelAndView("users_dashboard_denied")
        val authorizations = scaSdkService.getAllAuthorizations(user.id.toString()).sortedByDescending { it.id }
        return ModelAndView("users_dashboard_authorizations")
                .addObject("user", user)
                .addObject("authorizations", authorizations)
    }

    @PostMapping("/authorizations/create")
    fun createDemoPaymentAuthorizationForUser(@RequestParam(value = KEY_USER_ID) userId: Long): ModelAndView {
        val user = usersService.findUser(userId) ?: return ModelAndView("users_dashboard_denied")
        createDemoPaymentAuthorization(user = user)
        return ModelAndView("redirect:$DASHBOARD_PATH/authorizations?user_id=$userId")
    }

    @GetMapping("/consents")
    fun showConsents(
            @RequestParam(value = KEY_USER_ID, required = false) userId: Long?
    ): ModelAndView {
        val user = userId?.let { usersService.findUser(it) } ?: return ModelAndView("users_dashboard_denied")
        val consents: List<ConsentEntity> = consentsRepository.findAllByUserId(userId).sortedByDescending { it.id }
        return ModelAndView("users_dashboard_consents")
                .addObject("user", user)
                .addObject("consents", consents)
    }

    @PostMapping("/consents/create")
    fun createConsentForUser(@RequestParam(value = KEY_USER_ID) userId: Long): ModelAndView {
        val user = usersService.findUser(userId) ?: return ModelAndView("users_dashboard_denied")
        createConsent(user = user)
        return ModelAndView("redirect:$DASHBOARD_PATH/consents?user_id=$userId")
    }

    private fun revokeConnection(connectionId: Long?) {
        connectionId?.let { scaSdkService.revokeConnection(it) }
    }

    private fun createDemoPaymentAuthorization(user: UserEntity) {
        val amount = (1 until 200).random().toDouble()
        val amountString = "€ " + "%.2f".format(amount)
        val fromAccount = "DE89 3704 0044 0532 0130 00"
        val payeeAccount = "RO49 AAAA 1B31 0075 9384 0000"
        val payeeName = "Salt Edge SCA Service Example (Spring)"
        val textDescription = createTextDescription(amountString, fromAccount, payeeName)

        scaSdkService.createAuthorization(
                user.id.toString(),
                CodeBuilder.generateRandomString(),
                "Payment confirmation",
                AuthorizationTemplate.createHTMLDescriptionForPisp(amountString, fromAccount, payeeName, payeeAccount, textDescription)
        )
    }

    private fun createTextDescription(amountString: String, fromAccount: String, toAccount: String): String {
        return "Confirm payment of $amountString from account $fromAccount to $toAccount"
    }

    private fun createConsent(user: UserEntity) {
        val consent = ConsentEntity(
                tppName = "Example Dashboard",
                accounts = listOf(
                        ScaAccount("Checking account (GB)", "22334455", "11-22-33", null),
                        ScaAccount("Credit card account (DE)", null, null, "DE89 3704 0044 0532 0130 00"),
                        ScaAccount("Demo account", null, null, null)
                ),
                expiresAt = Instant.now().plus(7, ChronoUnit.DAYS),
                user = user
        )
        consentsRepository.save(consent)
    }
}
