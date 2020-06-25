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
package com.saltedge.sca.example.tools

import freemarker.template.Configuration
import freemarker.template.Template
import java.io.StringWriter
import java.io.Writer
import java.time.LocalDateTime

object AuthorizationTemplate {

    private val aispTemplate = loadTemplate("authorization_example_aisp.ftl")
    private val pispTemplate = loadTemplate("authorization_example_pisp.ftl")

    fun createHTMLDescriptionForAisp(
            tppName: String,
            serviceName: String
    ): String {
        return try {
            val out: Writer = StringWriter()
            aispTemplate.process(mapOf("tpp_name" to tppName, "service_name" to serviceName), out)
            out.toString()
        } catch(e: Exception) {
            e.printStackTrace()
            "$tppName is requesting your authorization to access account information data from $serviceName</p>"
        }
    }

    fun createHTMLDescriptionForPisp(
            amountString: String,
            fromAccount: String,
            payeeName: String,
            payeeAccount: String,
            paymentDescription: String
    ): String {
        return try {
            val dataMap = mapOf(
                    "amount" to amountString,
                    "payee_name" to payeeName,
                    "from_account" to fromAccount,
                    "to_account" to payeeAccount,
                    "payment_description" to paymentDescription,
                    "date" to LocalDateTime.now().toString()
            )
            val out: Writer = StringWriter()
            pispTemplate.process(dataMap, out)
            out.toString()
        } catch(e: Exception) {
            e.printStackTrace()
            paymentDescription
        }
    }

    private fun loadTemplate(templateFileName: String): Template {
        val configuration = Configuration(Configuration.VERSION_2_3_29)
        configuration.setClassForTemplateLoading(this.javaClass, "/templates/");
        return configuration.getTemplate(templateFileName)
    }
}
