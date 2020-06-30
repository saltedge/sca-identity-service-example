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

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.saltedge.sca.sdk.models.api.ScaAccount
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.persistence.AttributeConverter

class AccountsConverter : AttributeConverter<List<ScaAccount>, String> {
    private val objectMapper = ObjectMapper()
    override fun convertToDatabaseColumn(attribute: List<ScaAccount>): String? {
        return try {
            objectMapper.writeValueAsString(attribute)
        } catch (e: JsonProcessingException) {
            log.error("JSON writing error", e)
            null
        }
    }

    override fun convertToEntityAttribute(dbData: String): List<ScaAccount>? {
        return try {
            val typeRef: TypeReference<List<ScaAccount>> = object : TypeReference<List<ScaAccount>>() {}
            objectMapper.readValue(dbData, typeRef)
        } catch (e: IOException) {
            log.error("JSON reading error", e)
            null
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AccountsConverter::class.java)
    }
}