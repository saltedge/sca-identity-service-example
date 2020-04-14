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

import com.saltedge.sca.sdk.tools.DateTools
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import javax.persistence.*

@Entity
class ConsentEntity() {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    var id: Long = 0

    @Column
    @CreationTimestamp
    var createdAt: Instant? = null

    @Column
    @UpdateTimestamp
    var updatedAt: Instant? = null

    @Column(nullable = false, length = 1024)
    var title: String = ""

    @Column(nullable = false, length = 4096)
    var description: String = ""

    @Column
    var expiresAt: Instant? = null

    @Column
    var revoked = false

    @ManyToOne var user: UserEntity? = null

    constructor(
            title: String,
            description: String,
            expiresAt: Instant,
            user: UserEntity
    ) : this() {
        this.title = title
        this.description = description
        this.expiresAt = expiresAt
        this.user = user
    }

    fun isExpired(): Boolean {
        return DateTools.dateIsExpired(expiresAt)
    }
}