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
package com.saltedge.sca.sdk.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

import static com.saltedge.sca.sdk.ScaSdkConstants.KEY_BALANCE;
import static com.saltedge.sca.sdk.ScaSdkConstants.KEY_TRANSACTIONS;

/**
 * The specific information of shared with third party data type
 */
@Validated
public class ScaConsentSharedData {
    /**
     * Balance data of account shared with third party
     */
    @JsonProperty(KEY_BALANCE)
    @NotNull
    public boolean balance;

    /**
     * Transactions data of account shared with third party
     */
    @JsonProperty(KEY_TRANSACTIONS)
    @NotNull
    public boolean transactions;

    public ScaConsentSharedData() {
    }

    public ScaConsentSharedData(@NotNull boolean balance, @NotNull boolean transactions) {
        this.balance = balance;
        this.transactions = transactions;
    }

    public boolean isBalance() {
        return balance;
    }

    public boolean isTransactions() {
        return transactions;
    }
}
