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
import com.saltedge.sca.example.model.PaymentOrderEntity
import com.saltedge.sca.example.model.PaymentOrdersRepository
import com.saltedge.sca.sdk.provider.ScaSDKCallbackService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentsService {
    @Autowired
    private lateinit var paymentsRepository: PaymentOrdersRepository
    @Autowired
    private lateinit var scaSdkService: ScaSDKCallbackService

    fun getOrCreateDemoPaymentOrder(savedPaymentUUID: String, createNew: Boolean): PaymentOrderEntity {
        var payment: PaymentOrderEntity? = getPaymentByUUID(savedPaymentUUID)
        if (payment == null || payment.isClosed() || createNew) {
            payment = createNewDemoPaymentOrder()
        }
        return payment
    }

    fun createAuthenticateActionAppLink(paymentUUID: String): String = scaSdkService.createAuthenticateActionAppLink(paymentUUID)

    fun getPaymentByUUID(paymentUUID: String): PaymentOrderEntity? {
        return paymentsRepository.findFirstByUuid(paymentUUID)
    }

    private fun createNewDemoPaymentOrder(): PaymentOrderEntity {
        val amount = (0 until 200).random().toDouble()
        val payment = PaymentOrderEntity().apply {
            this.uuid = UUID.randomUUID().toString()
            this.amount = "%.2f".format(amount)
            this.currency = "EUR"
            this.payeeName = "Salt Edge Inc."
            this.payeeAddress = "40 King Street West, Suite 2100, Toronto, Ontario M5H3C2, Canada"
            this.fromAccount = "DE89 3704 0044 0532 0130 00"
            this.payeeAccount = "RO49 AAAA 1B31 0075 9384 0000"
        }
        paymentsRepository.save(payment)
        scaSdkService.createAction(SCA_ACTION_PAYMENT, payment.uuid, null)
        return payment
    }

    private fun requireSCAConfirmation(payment: PaymentOrderEntity): Boolean {
        return (payment.amount.toDoubleOrNull() ?: return false) > 50.0
    }
}
