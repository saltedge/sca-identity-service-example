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

import com.saltedge.authenticator.identity.AUTHENTICATOR_API_BASE_PATH
import com.saltedge.authenticator.identity.HEADER_KEY_ACCESS_TOKEN
import com.saltedge.authenticator.identity.controller.admin.createUserEnrollSuccessUrl
import com.saltedge.authenticator.identity.controller.admin.createUserEnrollUrl
import com.saltedge.authenticator.identity.error.ConnectionNotFoundException
import com.saltedge.authenticator.identity.model.Connection
import com.saltedge.authenticator.identity.model.ConnectionsRepository
import com.saltedge.authenticator.identity.model.UsersRepository
import com.saltedge.authenticator.identity.model.mapping.*
import com.saltedge.authenticator.identity.tools.validateRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

const val CONNECTIONS_REQUEST_PATH: String = "$AUTHENTICATOR_API_BASE_PATH/connections"

@RestController
@RequestMapping(CONNECTIONS_REQUEST_PATH)
class ConnectionsController {
    @Autowired
    private var usersRepository: UsersRepository? = null
    @Autowired
    private var connectionsRepository: ConnectionsRepository? = null

    @PostMapping
    fun createConnection(
        request: HttpServletRequest,
        @RequestBody newConnectionRequest: CreateConnectionRequest
    ): ResponseEntity<CreateConnectionResponse> {
        val repository = connectionsRepository ?: return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)

        newConnectionRequest.data?.let { data ->
            val user = data.connectQuery?.toLongOrNull()?.let { userId ->
                usersRepository?.findById(userId)?.let { if (it.isPresent) it.get() else null }
            }
            val connection = Connection(requestData = data, user = user)
            repository.save(connection)

            return ResponseEntity.ok(CreateConnectionResponse(CreateConnectionResponseData(
                connectionId = "${connection.id}",
                authorizeUrl = createConnectionResponseUrl(request, connection)
            )))
        } ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
    }

    @DeleteMapping
    fun revokeConnection(
        request: HttpServletRequest,
        @RequestHeader(HEADER_KEY_ACCESS_TOKEN) accessToken: String?
    ): ResponseEntity<RevokeConnectionResponse> {
        val connection = request.validateRequest(connectionsRepository = connectionsRepository) ?: throw ConnectionNotFoundException()
        connectionsRepository?.save(connection.apply { revoked = true })
        return ResponseEntity.ok(RevokeConnectionResponse(RevokeConnectionData(success = true, accessToken = accessToken ?: "")))
    }

    private fun createConnectionResponseUrl(request: HttpServletRequest, connection: Connection): String {
        return if (connection.user == null) {
            createUserEnrollUrl(request, connection.connectToken)
        } else {
            createUserEnrollSuccessUrl(connection)
        }
    }
}
