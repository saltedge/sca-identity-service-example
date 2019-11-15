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
package com.saltedge.authenticator.identity.model

import com.saltedge.authenticator.identity.tools.toIso8601
import java.util.*
import javax.persistence.*

@Entity
class Authorization() {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	var id: Long = 0
	var createdAt: Long = 0
	var updatedAt: Long = 0
	var expiresAt: Long = 0
	var confirmed: Boolean? = null

	@Column(length = 4096)
	var title: String = ""

	@Column(length = 4096)
	var description: String = ""

	@Column(length = 4096)
	var authorizationCode: String = ""

	@ManyToOne
	var user: User? = null

	internal constructor(title: String,
						 description: String,
						 authorizationCode: String,
						 user: User,
						 createdAt: Long = Date().time) : this() {
		this.title = title
		this.description = description
		this.authorizationCode = authorizationCode
		this.user = user
		this.createdAt = createdAt
		this.updatedAt = createdAt
		this.expiresAt = createdAt + 5 * 60 * 1000
	}

	fun isExpired(): Boolean {
		return expiresAt <= Date().time
	}

	fun getCreatedAtString(): String {
		return Date(createdAt).toIso8601()
	}

	fun getExpiresAtString(): String {
		return Date(expiresAt).toIso8601()
	}

	fun getStatus(): String {
		return when {
			confirmed == true -> "CONFIRMED"
			confirmed == false -> "DENIED"
			this.isExpired() -> "EXPIRED"
			else -> "WAITING CONFIRM"
		}
	}
}
