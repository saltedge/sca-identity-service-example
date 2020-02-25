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

import com.saltedge.sca.example.services.UserAuthService
import com.saltedge.sca.example.tools.COOKIE_AUTHENTICATION_ACTION
import com.saltedge.sca.example.tools.saveAuthenticationActionCookie
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_ACTION_UUID
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_SECRET
import com.saltedge.sca.sdk.services.ScaSdkService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletResponse

const val SIGN_IN_PATH = "/users/sign_in"
const val SIGN_UP_PATH = "/users/register"
const val SIGN_IN_SCA_PATH = "/users/sign_in_sca"
const val SIGN_IN_CONNECT_SCA_PATH = "/users/connect_sca"

@Controller
class UserSignInController {
    private val log = LoggerFactory.getLogger(UserSignInController::class.java)
    private val signInQrTemplate = "users_sign_in_qr"
    private val signUpTemplate = "users_register"
    @Autowired
    private lateinit var userAuthService: UserAuthService
    @Autowired
    private lateinit var scaSdkService: ScaSdkService

    @GetMapping(SIGN_IN_PATH)
    fun showSignInWithQr(
            @CookieValue(value = COOKIE_AUTHENTICATION_ACTION, defaultValue = "") savedActionUUID: String,
            response: HttpServletResponse
    ): ModelAndView {
        val actionUUID: String = userAuthService.getOrCreateAuthenticateAction(savedActionUUID)
        if (savedActionUUID != actionUUID) saveAuthenticationActionCookie(actionUUID, response)
        val appLink: String = userAuthService.createActionAppLink(actionUUID)
        val existUsers = userAuthService.hasUsers()
        return ModelAndView(signInQrTemplate)
                .addObject("show_sca_options", existUsers)
                .apply { if (!existUsers) addObject("error", "No registered users") }
                .addObject(KEY_ACTION_UUID, actionUUID)
                .addObject("authenticator_link", appLink)
                .addObject("qr_img_src", "$SCA_ACTION_QR_PATH?$KEY_ACTION_UUID=$actionUUID")
    }

    @PostMapping(SIGN_IN_PATH)
    fun onSubmitSignInCredentials(
            @RequestParam username: String,
            @RequestParam password: String
    ): ModelAndView {
        val userId: Long? = userAuthService.findUserId(username = username, password = password)
        return when {
            userId != null -> {
                UserDashboardController.redirectToDashboard(userId)
            }
            else -> {
                ModelAndView(signInQrTemplate).addObject("error", "Invalid credentials")
            }
        }
    }

    @GetMapping(SIGN_UP_PATH)
    fun showSignUp(): ModelAndView = ModelAndView(signUpTemplate)

    @PostMapping(SIGN_UP_PATH)
    fun onSubmitSignUpCredentials(
            @RequestParam(required = true) username: String,
            @RequestParam(required = true) password: String,
            response: HttpServletResponse
    ): ModelAndView {
        var error: String? = null
        val userId = userAuthService.createNewUser(username = username, password = password, errorCallback = {
            error = it
        })
        return when {
            userId != null -> {
                UserDashboardController.redirectToDashboard(userId)
            }
            error != null -> ModelAndView(signUpTemplate).addObject("error", error)
            else -> ModelAndView("redirect:$SIGN_UP_PATH")
        }
    }

    @GetMapping(SIGN_IN_SCA_PATH)
    fun showSignSca(@RequestParam(KEY_SECRET) authenticationSecret: String): ModelAndView {
        return ModelAndView("users_sign_in_sca").addObject(KEY_SECRET, authenticationSecret)
    }

    @PostMapping(SIGN_IN_SCA_PATH)
    fun onSubmitSignInScaCredentials(
            @RequestParam username: String,
            @RequestParam password: String,
            @RequestParam(KEY_SECRET) authenticationSecret: String
    ): ModelAndView {
        val redirectUrl = userAuthService.authenticateScaClientAndGetRedirectUrl(username = username, password = password, secret = authenticationSecret)
        return ModelAndView("redirect:$redirectUrl")
    }

    @GetMapping(SIGN_IN_CONNECT_SCA_PATH)
    fun showAuthenticatorConnect(): ModelAndView {
        return ModelAndView("connect_sca")
                .addObject("authenticator_link", scaSdkService.createConnectAppLink())
                .addObject("qr_img_src", SCA_CONNECT_QR_PATH)
    }
}
