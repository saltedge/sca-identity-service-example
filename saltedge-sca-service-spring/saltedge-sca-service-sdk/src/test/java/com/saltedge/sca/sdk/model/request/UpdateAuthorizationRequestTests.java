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
package com.saltedge.sca.sdk.model.request;

import com.saltedge.sca.sdk.ValidationTestAbs;
import com.saltedge.sca.sdk.models.api.requests.ScaUpdateAuthorizationRequest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateAuthorizationRequestTests extends ValidationTestAbs {
    @Test
    public void validateTest() {
        ScaUpdateAuthorizationRequest model = new ScaUpdateAuthorizationRequest();

        assertThat(validator.validate(model)).isNotEmpty();

        model.data = new ScaUpdateAuthorizationRequest.Data();

        assertThat(validator.validate(model)).isNotEmpty();

        model.data.confirmAuthorization = null;
        model.data.authorizationCode = null;

        assertThat(validator.validate(model)).isNotEmpty();

        model.data.confirmAuthorization = true;
        model.data.authorizationCode = null;

        assertThat(validator.validate(model)).isNotEmpty();

        model.data.confirmAuthorization = null;
        model.data.authorizationCode = "";

        assertThat(validator.validate(model)).isNotEmpty();

        model.data.confirmAuthorization = false;
        model.data.authorizationCode = "";

        assertThat(validator.validate(model)).isEmpty();
    }
}
