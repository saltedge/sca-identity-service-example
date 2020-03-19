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

import com.saltedge.sca.example.model.PaymentOrder
import com.saltedge.sca.example.services.PaymentsService
import com.saltedge.sca.example.services.UsersService
import com.saltedge.sca.example.tools.COOKIE_AUTHENTICATION_ACTION
import com.saltedge.sca.example.tools.clearActionCookie
import com.saltedge.sca.sdk.ScaSdkConstants
import com.saltedge.sca.sdk.errors.NotFound
import com.saltedge.sca.sdk.models.ActionStatus
import com.saltedge.sca.sdk.services.ScaSdkService
import com.saltedge.sca.sdk.tools.QrTools
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletResponse

const val SCA_CONNECT_QR_PATH = "/sca/connect/qr"
const val SCA_ACTION_QR_PATH = "/sca/action/qr"
const val SCA_LOGIN_STATUS_PATH = "/sca/login/status"
const val SCA_PAYMENT_STATUS_PATH = "/sca/payment/status"
const val SCA_ACTION_LOGIN = "authenticate_login_action"
const val SCA_ACTION_PAYMENT = "authenticate_payment_action"

@Controller
class ScaController {
    private val log = LoggerFactory.getLogger(ScaController::class.java)
    @Autowired
    private lateinit var scaSdkService: ScaSdkService
    @Autowired
    private lateinit var usersService: UsersService
    @Autowired
    private lateinit var paymentsService: PaymentsService

    @GetMapping(SCA_CONNECT_QR_PATH)
    fun getConnectQRImage(
            @RequestParam(ScaSdkConstants.KEY_USER_ID) userId: Long?,
            response: HttpServletResponse
    ) {
        val userConnectSecret = usersService.getOrCreateUserConnectSecret(userId)
        val appLink: String = scaSdkService.createConnectAppLink(userConnectSecret)
        QrTools.encodeTextAsQrPngImage(appLink, 256, 256)?.let { image ->
            response.contentType = "image/png"
            val outputStream = response.outputStream
            outputStream.write(image)
            outputStream.flush()
            outputStream.close()
        }
    }

    @GetMapping(SCA_ACTION_QR_PATH)
    fun getActionQRImage(
            @RequestParam(ScaSdkConstants.KEY_ACTION_UUID) actionId: String?,
            response: HttpServletResponse
    ) {
        val appLink: String = scaSdkService.createAuthenticateActionAppLink(actionId)
        QrTools.encodeTextAsQrPngImage(appLink, 256, 256)?.let { image ->
            response.contentType = "image/png"
            val outputStream = response.outputStream
            outputStream.write(image)
            outputStream.flush()
            outputStream.close()
        }
    }

    @GetMapping(SCA_LOGIN_STATUS_PATH)
    @ResponseBody
    fun getScaLoginStatus(
            @RequestParam(ScaSdkConstants.KEY_ACTION_UUID) actionUUID: String?,
            response: HttpServletResponse
    ): Map<String, String> {
        val action = scaSdkService.getActionByUUID(actionUUID) ?: throw NotFound.ActionNotFound()
        return if (action.status == ActionStatus.AUTHENTICATED) {
            val userId = action.userId?.toLongOrNull() ?: return mapOf("status" to action.status.toString().toLowerCase())
            clearActionCookie(COOKIE_AUTHENTICATION_ACTION, response)

            mapOf(
                    "status" to action.status.toString().toLowerCase(),
                    "redirect" to UserDashboardController.createDashboardLink(userId)
            )
        } else {
            mapOf("status" to action.status.toString().toLowerCase())
        }
    }

    @GetMapping(SCA_PAYMENT_STATUS_PATH)
    @ResponseBody
    fun getScaPaymentStatus(
            @RequestParam("payment_uuid") paymentUUID: String,
            response: HttpServletResponse
    ): Map<String, Any> {
        val payment: PaymentOrder? = paymentsService.getPaymentByUUID(paymentUUID)
        val userName = payment?.userId?.let { usersService.findUser(it) }?.name ?: "Unknown"

        return mapOf(
                "status" to (payment?.status ?: "No payment"),
                "user_name" to userName,
                "show_auth" to (payment?.isAuthenticated()?.not() ?: true)
        )
    }
}
