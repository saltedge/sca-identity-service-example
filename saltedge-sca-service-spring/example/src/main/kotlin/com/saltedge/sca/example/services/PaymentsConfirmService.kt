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

import com.saltedge.sca.example.model.PaymentOrderEntity
import com.saltedge.sca.example.model.PaymentOrdersRepository
import com.saltedge.sca.example.tools.AuthorizationTemplate
import com.saltedge.sca.sdk.models.AuthorizationContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PaymentsConfirmService {
    @Autowired
    private lateinit var paymentsRepository: PaymentOrdersRepository

    fun authenticateDemoPaymentOrder(paymentUUID: String, userId: Long): AuthorizationContent? {
        val payment: PaymentOrderEntity = paymentsRepository.findFirstByUuid(paymentUUID) ?: return null
        payment.userId = userId
        paymentsRepository.save(payment)
        val description = AuthorizationTemplate.createHTMLDescriptionForPisp(
                "${payment.amount} ${payment.currency}",
                payment.fromAccount,
                payment.payeeName,
                payment.payeeAccount,
                "Confirm payment for ${payment.payeeName} ${payment.payeeAddress}}"
        )
        return AuthorizationContent(paymentUUID, "Payment confirmation", description)
    }

    fun authorizePaymentOrder(paymentUUID: String, confirmed: Boolean) {
        val payment: PaymentOrderEntity = paymentsRepository.findFirstByUuid(paymentUUID) ?: return
        payment.status = if (confirmed) "closed_success" else "closed_deny"
        paymentsRepository.save(payment)
    }
}
