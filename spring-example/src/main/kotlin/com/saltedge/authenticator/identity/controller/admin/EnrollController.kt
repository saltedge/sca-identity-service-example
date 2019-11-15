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

import com.saltedge.authenticator.identity.model.Connection
import com.saltedge.authenticator.identity.model.ConnectionsRepository
import com.saltedge.authenticator.identity.model.UsersRepository
import com.saltedge.authenticator.identity.tools.generateRandomString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.util.UriComponentsBuilder
import javax.servlet.http.HttpServletRequest

private const val ENROLL_PATH = "/admin/enroll"

@Controller
@RequestMapping(ENROLL_PATH)
class EnrollController {
	@Autowired
	private var usersRepository: UsersRepository? = null
	@Autowired
	private var connectionsRepository: ConnectionsRepository? = null

	@GetMapping
	fun showSignIn(@RequestParam token: String? = null): ModelAndView {
		return ModelAndView("sign_in", mapOf("token" to token))
	}

	@PostMapping
	fun signInUser(@RequestParam name: String,
				   @RequestParam password: String,
				   @RequestParam token: String? = null): ModelAndView {
		val authToken = token ?: return ModelAndView("error")
		val repository = connectionsRepository ?: return ModelAndView("error")
		val connection = repository.findByConnectToken(authToken)
				?: return ModelAndView("error")
		val redirectString = if (name.isNotBlank() && password.isNotBlank()) {
			val user = usersRepository?.findByNameAndPassword(name = name, password = password)
					?: return ModelAndView("error")

			connection.user = user
			connection.accessToken = generateRandomString()
			connectionsRepository?.save(connection)
			createUserEnrollSuccessUrl(connection)
		} else {
			createRedirectUrl(
				returnUrl = connection.returnUrl,
				params = mapOf("error_class" to "AUTHENTICATION_ERROR", "error_message" to "AUTHENTICATION_ERROR_MESSAGE")
			)
		}
		return ModelAndView("redirect:$redirectString")
	}
}

fun createUserEnrollUrl(request: HttpServletRequest, sessionToken: String): String {
	val builder = UriComponentsBuilder.fromHttpUrl("https://${request.serverName}")
	builder.path(ENROLL_PATH)
	builder.queryParam("token", sessionToken)
	return builder.toUriString()
}

fun createUserEnrollSuccessUrl(connection: Connection): String {
	return createRedirectUrl(
		returnUrl = connection.returnUrl,
		params = mapOf("id" to connection.id.toString(), "access_token" to connection.accessToken)
	)
}

fun createRedirectUrl(returnUrl: String, params: Map<String, String>): String {
	val builder = UriComponentsBuilder.fromUriString(returnUrl)
	params.forEach { (key, value) -> builder.queryParam(key, value) }
	return builder.toUriString()
}
