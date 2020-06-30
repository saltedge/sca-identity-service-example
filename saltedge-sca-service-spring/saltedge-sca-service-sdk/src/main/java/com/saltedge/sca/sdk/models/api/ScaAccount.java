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

import javax.validation.constraints.NotEmpty;

import static com.saltedge.sca.sdk.ScaSdkConstants.*;

/**
 * Bank Account with account name and one of account identifiers (account number or sort code or iban)
 */
@Validated
public class ScaAccount {
    /**
     * name of the account that was shared with third party provider
     */
    @JsonProperty(KEY_NAME)
    @NotEmpty
    public String name;

    /**
     * identifier of the account (for the accounts in GBP currency)
     */
    @JsonProperty(KEY_ACCOUNT_NUMBER)
    public String accountNumber;

    /**
     * identifier of the account (for the accounts in GBP currency)
     */
    @JsonProperty(KEY_SORT_CODE)
    public String sortCode;

    /**
     * identifier of the account (for the accounts in EUR or other European currency)
     */
    @JsonProperty(KEY_IBAN)
    public String iban;

    public ScaAccount() {
    }

    public ScaAccount(@NotEmpty String name, String accountNumber, String sortCode, String iban) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
        this.iban = iban;
    }

    public String getName() {
        return name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }

    public String getIban() {
        return iban;
    }
}
