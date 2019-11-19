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

import com.saltedge.authenticator.identity.controller.api.CONFIGURATION_REQUEST_PATH
import com.saltedge.authenticator.identity.tools.getQRCodeImage
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/admin/connect")
class ConnectController {
    private val deepLinkPrefix = "authenticator://saltedge.com/connect"

    @GetMapping
    fun showConnect(
        request: HttpServletRequest,
        @RequestParam("user_id") userId: Long?
    ): ModelAndView {
        val imageSrc = userId?.let { "connect/qr?user_id=$userId"  } ?: "connect/qr"
        return ModelAndView("connect", mapOf(
            "link" to createDeepLink(request, userId),
            "src" to imageSrc
        ))
    }

    @GetMapping("/qr")
    fun getQRCodeImage(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestParam("user_id") userId: Long?
    ) {
        getQRCodeImage(text = createDeepLink(request, userId), width = 512, height = 512)?.let { image ->
            response.contentType = "image/png"
            val outputStream = response.outputStream
            outputStream.write(image)
            outputStream.flush()
            outputStream.close()
        }
    }

    private fun createDeepLink(request: HttpServletRequest, userId: Long?): String {
        val connectQuery = userId?.let { "&connect_query=$userId" } ?: ""
        return "${deepLinkPrefix}?configuration=https://${request.serverName}$CONFIGURATION_REQUEST_PATH$connectQuery"
    }
}
