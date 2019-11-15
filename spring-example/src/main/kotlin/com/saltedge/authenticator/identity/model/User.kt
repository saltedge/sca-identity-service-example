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

import java.util.*
import javax.persistence.*

@Entity
class User() {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	public var id: Long? = null
	private var createdAt: Long
	private var updatedAt: Long

	@Column(nullable = false, length = 64, unique = true)
	var name: String = ""
	@Column(nullable = false, length = 64)
	var password: String = ""

	@Column(length = 4096)
	private val authSessionToken: String? = null
	private val authSessionTokenExpiresAt: Long? = null

	constructor(name: String, password: String) : this() {
		this.name = name
		this.password = password
	}

	init {
		val now = Date()
		createdAt = now.time
		updatedAt = createdAt
	}
}
