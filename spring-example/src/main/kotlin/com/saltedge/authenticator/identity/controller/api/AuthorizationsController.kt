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
package com.saltedge.authenticator.identity.controller.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.saltedge.authenticator.identity.AUTHENTICATOR_API_BASE_PATH
import com.saltedge.authenticator.identity.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.identity.HEADER_KEY_AUTHORIZATION_ID
import com.saltedge.authenticator.identity.error.AuthorizationNotFoundException
import com.saltedge.authenticator.identity.error.ConnectionNotFoundException
import com.saltedge.authenticator.identity.error.UserNotFoundException
import com.saltedge.authenticator.identity.model.Authorization
import com.saltedge.authenticator.identity.model.AuthorizationsRepository
import com.saltedge.authenticator.identity.model.ConnectionsRepository
import com.saltedge.authenticator.identity.model.mapping.*
import com.saltedge.authenticator.identity.tools.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.security.PublicKey
import java.util.*
import javax.servlet.http.HttpServletRequest

const val AUTHORIZATIONS_REQUEST_PATH: String = "$AUTHENTICATOR_API_BASE_PATH/authorizations"

@RestController
@RequestMapping(AUTHORIZATIONS_REQUEST_PATH)
class AuthorizationsController {
	@Autowired
	private var connectionsRepository: ConnectionsRepository? = null
	@Autowired
	private var authorizationsRepository: AuthorizationsRepository? = null

	@GetMapping
	fun getAuthorizations(
			request: HttpServletRequest
	): ResponseEntity<AuthorizationsResponse> {
		val connection = request.validateRequest(connectionsRepository = connectionsRepository) ?: throw ConnectionNotFoundException()

		val publicKey = connection.publicKey.toPublicKey() ?: throw ConnectionNotFoundException()
		val user = connection.user ?: throw UserNotFoundException()

		val authorizations = authorizationsRepository?.findByUserAndExpiresAtGreaterThanAndConfirmedIsNull(
				user = user,
				currentDate = Date().time
		) ?: emptyList()

		val result = authorizations.map { createEncryptedAuthorization(it, connection.id, publicKey) }
		return ResponseEntity.ok(AuthorizationsResponse(result))
	}

	@GetMapping("/{authorizationId}")
	fun getAuthorization(
			request: HttpServletRequest,
			@PathVariable(HEADER_KEY_AUTHORIZATION_ID) authorizationId: Long
	): ResponseEntity<AuthorizationResponse> {
		val connection = request.validateRequest(connectionsRepository = connectionsRepository) ?: throw ConnectionNotFoundException()

		val publicKey = connection.publicKey.toPublicKey() ?: throw ConnectionNotFoundException()
		val user = connection.user ?: throw UserNotFoundException()
		val authorization = authorizationsRepository?.findByIdAndUserAndExpiresAtGreaterThanAndConfirmedIsNull(
				id = authorizationId,
				user = user,
				currentDate = Date().time
		)?.get() ?: throw AuthorizationNotFoundException()

		return ResponseEntity.ok(AuthorizationResponse(createEncryptedAuthorization(authorization, connection.id, publicKey)))
	}

	@PutMapping("/{authorizationId}")
	fun updateAuthorization(
			request: HttpServletRequest,
			@PathVariable(HEADER_KEY_AUTHORIZATION_ID) authorizationId: Long
	): ResponseEntity<UpdateAuthorizationResponse> {
		val requestBody = extractRequestBody(request, UpdateAuthorizationRequest::class.java)
		val updateAuthorizationRequest = requestBody.second
		val connection = request.validateRequest(requestBody.first, connectionsRepository) ?: throw ConnectionNotFoundException()

		val user = connection.user ?: throw UserNotFoundException()
		val authorization = authorizationsRepository?.findByIdAndUserAndExpiresAtGreaterThanAndConfirmedIsNull(
				id = authorizationId,
				user = user,
				currentDate = Date().time
		)?.get() ?: throw AuthorizationNotFoundException()

		val confirmSuccess = authorization.authorizationCode == updateAuthorizationRequest?.data?.authorizationCode
		val shouldBeConfirmed = updateAuthorizationRequest?.data?.confirm
		if (confirmSuccess && shouldBeConfirmed != null) {
			authorization.confirmed = shouldBeConfirmed
			authorizationsRepository?.save(authorization)
		}
		return ResponseEntity.ok(UpdateAuthorizationResponse(UpdateAuthorizationData(confirmSuccess, authorizationId.toString())))
	}

	private fun createEncryptedAuthorization(authorization: Authorization, connectionId: Long, publicKey: PublicKey): EncryptedAuthorization {
			val authorizationHash = mapOf(
					"id" to "${authorization.id}",
					"connection_id" to "$connectionId",
					"title" to authorization.title,
					"description" to authorization.description,
					"authorization_code" to authorization.authorizationCode,
					"created_at" to authorization.createdAt.toIso8601(),
					"expires_at" to authorization.expiresAt.toIso8601()
			)

		val authorizationJson = ObjectMapper().writeValueAsString(authorizationHash)
		return CryptTools.encrypt(data = authorizationJson, publicKey = publicKey).apply {
			this.id = "${authorization.id}"
			this.connectionId = "$connectionId"
		}
	}
}
