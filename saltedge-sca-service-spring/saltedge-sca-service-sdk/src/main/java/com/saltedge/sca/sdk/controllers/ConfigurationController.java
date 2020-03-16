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
package com.saltedge.sca.sdk.controllers;

import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.models.api.ConfigurationData;
import com.saltedge.sca.sdk.models.api.responces.ConfigurationResponse;
import com.saltedge.sca.sdk.provider.ServiceProvider;
import com.saltedge.sca.sdk.tools.EnvironmentTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller designated for serving Service Provider's SCA configuration
 */
@RestController
@RequestMapping(ConfigurationController.CONFIGURATION_REQUEST_PATH)
public class ConfigurationController {
    public final static String CONFIGURATION_REQUEST_PATH = ScaSdkConstants.AUTHENTICATOR_API_BASE_PATH + "/configuration";
    @Autowired
    Environment env;
    @Autowired
    ServiceProvider providerApi;

    @GetMapping
    public ResponseEntity<ConfigurationResponse> getConfiguration() {
        String identityServiceUrl = EnvironmentTools.getScaServiceUrl(env);
        System.err.println("CONFIGURATION_REQUEST: Service url:"+identityServiceUrl);
        String providerCode = providerApi.getProviderCode();
        String providerName = providerApi.getProviderName();
        String providerLogoUrl = providerApi.getProviderLogoUrl();
        String providerSupportEmail = providerApi.getProviderSupportEmail();
        return ResponseEntity.ok(new ConfigurationResponse(new ConfigurationData(
                identityServiceUrl, providerCode, providerName, providerLogoUrl, providerSupportEmail
        )));
    }


}
