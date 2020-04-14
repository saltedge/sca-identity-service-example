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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer {
    @Autowired
    private val usersRepository: UsersRepository? = null
    private var user: UserEntity? = null

    @EventListener
    fun seed(event: ContextRefreshedEvent?) {
        user = seedUsers()
    }

    private fun seedUsers(): UserEntity {
        return if (usersRepository!!.count() == 0L) {
            log.info("DatabaseInitializer: Users Seeded")
            usersRepository.save(UserEntity(
                    "username",
                    "secret"
            ))
        } else {
            log.info("DatabaseInitializer: Users Seeding Not Required")
            usersRepository.findAll()[0]
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DatabaseInitializer::class.java)
    }
}