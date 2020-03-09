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

import com.saltedge.sca.example.controller.SCA_ACTION_PAYMENT
import com.saltedge.sca.example.model.PaymentOrder
import com.saltedge.sca.example.model.PaymentOrdersRepository
import com.saltedge.sca.example.model.UsersRepository
import com.saltedge.sca.sdk.services.ScaSdkService
import com.saltedge.sca.sdk.tools.CodeBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentsService {
    @Autowired
    private lateinit var usersRepository: UsersRepository
    @Autowired
    private lateinit var paymentsRepository: PaymentOrdersRepository
    @Autowired
    private lateinit var scaSdkService: ScaSdkService

    fun getOrCreatePaymentOrder(savedPaymentUUID: String, createNew: Boolean): PaymentOrder {
        var payment: PaymentOrder? = getPaymentByUUID(savedPaymentUUID)
        if (payment == null || payment.isClosed() || createNew) {
            payment = createNewPaymentOrder()
        }
        return payment
    }

    fun createAuthenticateActionAppLink(paymentUUID: String): String = scaSdkService.createAuthenticateActionAppLink(paymentUUID)

    fun getPaymentByUUID(paymentUUID: String): PaymentOrder? {
        return paymentsRepository.findFirstByUuid(paymentUUID)
    }

    fun onAuthenticatePaymentOrder(paymentUUID: String, userId: Long): Long? {
        val payment: PaymentOrder = paymentsRepository.findFirstByUuid(paymentUUID) ?: return null
        payment.userId = userId
        return if (requireSCAConfirmation(payment)) {
            paymentsRepository.save(payment)
            val authorization = scaSdkService.createAuthorization(
                    userId.toString(),
                    payment.uuid,
                    "Payment confirmation",
                    "Confirm payment for \n${payment.payeeName}\n${payment.payeeAddress}\nAmount: ${payment.amount} ${payment.currency}"
            )
            authorization.id
        } else {
            payment.status = "closed"
            paymentsRepository.save(payment)
            null
        }
    }

    fun onAuthorizePaymentOrder(paymentUUID: String, confirmed: Boolean) {
        val payment: PaymentOrder = paymentsRepository.findFirstByUuid(paymentUUID) ?: return
        payment.status = if (confirmed) "closed_success" else "closed_deny"
        paymentsRepository.save(payment)
    }

    private fun createNewPaymentOrder(): PaymentOrder {
        val amount = (0 until 200).random().toDouble()
        val payment = PaymentOrder().apply {
            this.uuid = UUID.randomUUID().toString()
            this.amount = "%.2f".format(amount)
            this.currency = "EUR"
            this.payeeName = "Salt Edge Inc."
            this.payeeAddress = "40 King Street West, Suite 2100, Toronto, Ontario M5H3C2, Canada"
        }
        paymentsRepository.save(payment)
        scaSdkService.createAction(SCA_ACTION_PAYMENT, payment.uuid, null)
        return payment
    }

    private fun requireSCAConfirmation(payment: PaymentOrder): Boolean {
        return (payment.amount.toDoubleOrNull() ?: return false) > 50.0
    }
}
