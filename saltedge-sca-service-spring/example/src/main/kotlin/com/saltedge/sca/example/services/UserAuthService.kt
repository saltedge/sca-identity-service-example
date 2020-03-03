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
package com.saltedge.sca.example.services

import com.saltedge.sca.example.controller.SCA_ACTION_LOGIN
import com.saltedge.sca.example.model.User
import com.saltedge.sca.example.model.UsersRepository
import com.saltedge.sca.sdk.models.AuthenticateAction
import com.saltedge.sca.sdk.services.ScaSdkService
import com.saltedge.sca.sdk.tools.CodeBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserAuthService {
    @Autowired
    private lateinit var usersRepository: UsersRepository
    @Autowired
    private lateinit var scaSdkService: ScaSdkService

    fun getOrCreateAuthSession(savedSessionCode: String): String {
        var action = scaSdkService.getActionByUUID(savedSessionCode)
        if (action == null || action.isExpired || action.isAuthenticated) {
            action = createAuthenticateAction()
        }
        return action.uuid
    }

    fun createActionAppLink(actionUUID: String): String = scaSdkService.createAuthenticateActionAppLink(actionUUID)

    fun hasUsers(): Boolean = usersRepository.count() > 0

    fun findUserId(username: String, password: String): Long? {
        return usersRepository.findFirstByNameAndPassword(name = username, password = password)?.id
    }

    fun createNewUser(username: String, password: String, errorCallback: (String) -> Unit): Long? {
        val error = validateUsernameAndPassword(username, password)
        return if (error != null) {
            errorCallback(error)
            null
        } else {
            usersRepository.save(User(name = username, password = password)).id
        }
    }

    private fun validateUsernameAndPassword(username: String, password: String): String? {
        return when {
            username.isBlank() || password.isBlank() -> "Invalid credentials"
            usersRepository.findFirstByName(name = username) != null -> "Username exist"
            else -> null
        }
    }

    fun authenticateScaClientAndGetRedirectUrl(username: String, password: String, secret: String): String {
        val userId: Long? = findUserId(username = username, password = password)
        return if (userId != null) {
            scaSdkService.onUserAuthenticationSuccess(secret, userId.toString())
        } else {
            scaSdkService.onUserAuthenticationFail(secret, "Invalid Credentials")
        }
    }

    private fun createAuthenticateAction(): AuthenticateAction? {
        return scaSdkService.createAction(
                SCA_ACTION_LOGIN,
                UUID.randomUUID().toString(),
                null
        );
    }
}
