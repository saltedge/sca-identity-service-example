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

import com.saltedge.authenticator.identity.model.mapping.CreateConnectionRequestData
import com.saltedge.authenticator.identity.tools.generateRandomString
import java.util.*
import javax.persistence.*

@Entity
class Connection() {
	@Id
	@GeneratedValue(strategy= GenerationType.AUTO)
	var id: Long = 0
	private val createdAt: Long
	private val updatedAt: Long

	@Column(length = 4096)
	var publicKey: String = ""

	@Column(length = 4096)
	var pushToken: String = ""

	@Column(length = 32)
	var platform: String = ""

	@Column(length = 4096)
	var returnUrl: String = ""

	@Column(length = 4096)
	var connectToken: String = ""

	private var connectTokenExpiresAt: Calendar? = null

	@Column(length = 4096)
	var accessToken: String = ""

	var revoked: Boolean = false

	@ManyToOne
	var user: User? = null

	constructor(requestData: CreateConnectionRequestData, user: User? = null) : this() {
		this.publicKey = requestData.publicKey
		this.pushToken = requestData.pushToken
		this.platform = requestData.platform
		this.returnUrl = requestData.returnUrl
		if (user == null) createConnectToken()
		else this.accessToken = generateRandomString()
		this.user = user
	}

	init {
		val now = Date()
		createdAt = now.time
		updatedAt = createdAt
	}

	private fun createConnectToken() {
		this.connectToken = generateRandomString()
		this.connectTokenExpiresAt = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
	}
}
