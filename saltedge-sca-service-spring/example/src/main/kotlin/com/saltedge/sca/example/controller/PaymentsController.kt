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

import com.saltedge.sca.example.model.PaymentOrderEntity
import com.saltedge.sca.example.services.PaymentsService
import com.saltedge.sca.example.services.UserAuthService
import com.saltedge.sca.example.services.UsersService
import com.saltedge.sca.example.tools.COOKIE_PAYMENT_ACTION
import com.saltedge.sca.example.tools.saveActionCookie
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_ACTION_UUID
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletResponse

const val PAYMENTS_ORDER_PATH = "/payments/order"
const val PAYMENTS_ORDER_SIGN_IN_PATH = "/payments/sign_in"

@Controller
class PaymentsController {
    private val log = LoggerFactory.getLogger(PaymentsController::class.java)
    @Autowired
    private lateinit var paymentsService: PaymentsService
    @Autowired
    private lateinit var userAuthService: UserAuthService
    @Autowired
    private lateinit var usersService: UsersService

    private val template = "payments_order"

    @GetMapping(PAYMENTS_ORDER_PATH)
    fun showPaymentOrder(
            @CookieValue(value = COOKIE_PAYMENT_ACTION, defaultValue = "") savedPaymentUUID: String,
            @RequestParam(value = "create_new", required = false) createNew: Boolean?,
            response: HttpServletResponse
    ): ModelAndView {
        val payment: PaymentOrderEntity = paymentsService.getOrCreatePaymentOrder(savedPaymentUUID, createNew ?: false)
        if (savedPaymentUUID != payment.uuid) saveActionCookie(COOKIE_PAYMENT_ACTION, payment.uuid, response)
        return ModelAndView(template)
                .addObject("payment", payment)
                .addObject("userName", payment.userId?.let { usersService.findUser(it) }?.name ?: "Unknown")
                .addObject("authenticator_link", paymentsService.createAuthenticateActionAppLink(payment.uuid))
                .addObject("qr_img_src", "$SCA_ACTION_QR_PATH?$KEY_ACTION_UUID=${payment.uuid}")
    }

    @PostMapping(PAYMENTS_ORDER_SIGN_IN_PATH)
    fun onSubmitSignInCredentials(
            @RequestParam username: String,
            @RequestParam password: String,
            @RequestParam("payment_uuid") paymentUUID: String
    ): ModelAndView {
        val userId: Long? = userAuthService.findUserId(username = username, password = password)
        return when {
            userId != null -> {
                paymentsService.authenticatePayment(paymentUUID, userId)
                ModelAndView("redirect:$PAYMENTS_ORDER_PATH")
            }
            else -> {
                ModelAndView(template).addObject("error", "Invalid credentials")
            }
        }
    }
}
