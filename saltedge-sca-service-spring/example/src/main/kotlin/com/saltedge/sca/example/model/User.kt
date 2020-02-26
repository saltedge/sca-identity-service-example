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
package com.saltedge.sca.example.model

import com.saltedge.sca.sdk.tools.CodeBuilder
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
class User() {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public var id: Long = 0

    @Column
    @CreationTimestamp
    private val createdAt: LocalDateTime? = null

    @Column
    @UpdateTimestamp
    private val updatedAt: LocalDateTime? = null

    @Column(nullable = false, length = 64, unique = true)
    public var name: String = ""
    @Column(nullable = false, length = 64)
    var password: String = ""

    @Column(length = 4096)
    var authSessionSecret: String? = null
    private var authSessionSecretExpiresAt: Long? = null

    constructor(name: String, password: String) : this() {
        this.name = name
        this.password = password
    }

    fun authSessionSecretIsExpired(): Boolean {
        return (authSessionSecretExpiresAt ?: return true) <= Date().time
    }

    fun createAuthSessionSecret() {
        this.authSessionSecret = CodeBuilder.generateRandomString()
        this.authSessionSecretExpiresAt = Date().time + 5 * 60 * 1000
    }
}
