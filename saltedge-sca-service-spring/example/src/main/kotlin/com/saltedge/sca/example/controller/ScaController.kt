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

import com.saltedge.sca.example.services.UsersService
import com.saltedge.sca.sdk.ScaSdkConstants
import com.saltedge.sca.sdk.errors.NotFound
import com.saltedge.sca.sdk.services.ScaSdkService
import com.saltedge.sca.sdk.tools.QrTools
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletResponse

const val SCA_CONNECT_QR_PATH = "/sca/connect/qr"
const val SCA_ACTION_QR_PATH = "/sca/action/qr"
const val SCA_ACTIONS_STATUS_PATH = "/sca/actions/status"
const val SCA_ACTIONS_EXECUTE_PATH = "/sca/actions/execute"

@Controller
class ScaController {
    private val log = LoggerFactory.getLogger(ScaController::class.java)
    @Autowired
    private lateinit var scaSdkService: ScaSdkService
    @Autowired
    private lateinit var usersService: UsersService

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
        val appLink: String = scaSdkService.createActionAppLink(actionId)
        QrTools.encodeTextAsQrPngImage(appLink, 256, 256)?.let { image ->
            response.contentType = "image/png"
            val outputStream = response.outputStream
            outputStream.write(image)
            outputStream.flush()
            outputStream.close()
        }
    }

    @GetMapping(SCA_ACTIONS_STATUS_PATH)
    @ResponseBody
    fun getActionStatus(@RequestParam(ScaSdkConstants.KEY_ACTION_UUID) actionUUID: String?): Map<String, String> {
        val status = scaSdkService.getActionStatus(actionUUID) ?: throw NotFound.ActionNotFound()
        return mapOf("status" to status.toString().toLowerCase())
    }

    @GetMapping(SCA_ACTIONS_EXECUTE_PATH)
    fun executeAction(@RequestParam(ScaSdkConstants.KEY_ACTION_UUID) actionUUID: String?): ModelAndView {
        val action = scaSdkService.getActionByUUID(actionUUID) ?: throw NotFound.ActionNotFound()
        val userId = action.userId?.toLongOrNull() ?: return ModelAndView("redirect:$SIGN_IN_PATH")
        return if (action.isAuthenticated) {
            UserDashboardController.redirectToDashboard(userId)
        } else {
            ModelAndView("redirect:$SIGN_IN_PATH")
        }
    }
}
