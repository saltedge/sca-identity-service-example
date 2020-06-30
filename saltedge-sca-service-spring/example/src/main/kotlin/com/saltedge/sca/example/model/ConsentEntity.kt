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

import com.saltedge.sca.sdk.models.api.ScaAccount
import com.saltedge.sca.sdk.tools.DateTools
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.Instant
import java.time.ZoneOffset
import javax.persistence.*

/**
 * Consent DB model
 * @see com.saltedge.sca.sdk.models.api.ScaConsent
 * @see com.saltedge.sca.sdk.models.api.ScaAccount
 */
@Entity
class ConsentEntity() {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    var id: Long = 0

    @CreationTimestamp
    @Column(updatable = false)
    val createdAt: Instant? = null

    @UpdateTimestamp
    @Column
    var updatedAt: Instant? = null

    @Column
    var expiresAt: Instant? = null

    @Column(nullable = false, length = 64)
    var consentType: String = "aisp"

    @Column(nullable = false, length = 1024)
    var tppName: String = ""

    @Column(nullable = false, length = 4096)
    @Convert(converter = AccountsConverter::class)
    var accounts: List<ScaAccount> = emptyList()

    @Column(nullable = false)
    var shareBalances: Boolean = true

    @Column(nullable = false)
    var shareTransactions: Boolean = true

    @Column(nullable = false, length = 64)
    var status: String = "open"

    @ManyToOne var user: UserEntity? = null

    constructor(
            tppName: String,
            accounts: List<ScaAccount>,
            expiresAt: Instant,
            user: UserEntity
    ) : this() {
        this.tppName = tppName
        this.accounts = accounts
        this.expiresAt = expiresAt
        this.user = user
    }

    fun isExpired(): Boolean {
        return DateTools.dateIsExpired(expiresAt)
    }

    fun expirationLocalDate(): String {
        return expiresAt?.atZone(ZoneOffset.systemDefault())?.toLocalDate()?.toString() ?: ""
    }

    fun isRevoked(): Boolean {
        return status == CONSENT_STATUS_REVOKED
    }

    fun revoke() {
        status = CONSENT_STATUS_REVOKED
    }
}