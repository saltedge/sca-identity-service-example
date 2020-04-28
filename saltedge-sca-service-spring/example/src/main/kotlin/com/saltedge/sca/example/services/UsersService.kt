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

import com.saltedge.sca.example.model.UserEntity
import com.saltedge.sca.example.model.UsersRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UsersService {
    @Autowired
    lateinit var usersRepository: UsersRepository

    fun findUser(userId: Long): UserEntity? {
        return usersRepository.findFirstById(userId)
    }

    fun findUserIdByAuthSessionCode(sessionSecret: String?): String? {
        if (sessionSecret == null) return null
        val user = usersRepository.findFirstByAuthSessionSecret(authSessionSecret = sessionSecret) ?: return null
        if (user.authSessionSecretIsExpired()) return null
        return user.id.toString()
    }

    fun getOrCreateUserConnectSecret(userId: Long?): String? {
        return userId?.let { usersRepository.findFirstById(it) }?.let { getOrCreateUserConnectSecret(it) }
    }

    private fun getOrCreateUserConnectSecret(user: UserEntity): String? {
        return user.let {
            if (it.authSessionSecret == null || it.authSessionSecretIsExpired()) {
                it.createAuthSessionSecret()
                usersRepository.save(it)
            }
            it
        }.authSessionSecret
    }
}
