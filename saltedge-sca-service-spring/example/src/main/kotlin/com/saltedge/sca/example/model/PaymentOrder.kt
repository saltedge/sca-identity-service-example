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

import com.saltedge.sca.sdk.models.AuthenticateAction
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
class PaymentOrder() {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public var id: Long = 0

    @Column
    @CreationTimestamp
    private val createdAt: LocalDateTime? = null

    @Column
    @UpdateTimestamp
    private val updatedAt: LocalDateTime? = null

    @Column(nullable = false, length = 4096, unique = true)
    var uuid: String = ""

    @Column(nullable = false, length = 64)
    var amount: String = ""

    @Column(nullable = false, length = 3)
    var currency: String = ""

    @Column(nullable = false, length = 4096)
    var payeeName: String = ""

    @Column(nullable = false, length = 4096)
    var payeeAddress: String = ""

    @Column
    var userId: Long? = null

    @Column(nullable = false, length = 256)
    var status: String = "waiting_confirmation"

    fun isClosed(): Boolean = status == "closed_success" || status == "closed_deny" || status == "closed_error"

    fun isWaitingConfirmation(): Boolean = status == "waiting_confirmation"

    fun isAuthenticated(): Boolean = userId != null
}
