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
package com.saltedge.sca.sdk.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saltedge.sca.sdk.ScaSdkConstants;
import com.saltedge.sca.sdk.errors.BadRequest;
import com.saltedge.sca.sdk.errors.Unauthorized;
import com.saltedge.sca.sdk.models.api.requests.AuthenticatedRequestAbs;
import com.saltedge.sca.sdk.models.api.requests.EmptyAuthenticatedRequest;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionEntity;
import com.saltedge.sca.sdk.models.persistent.ClientConnectionsRepository;
import com.saltedge.sca.sdk.tools.DateTools;
import com.saltedge.sca.sdk.tools.JsonTools;
import com.saltedge.sca.sdk.tools.SignTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Verify Access-Token and Signature.
 *
 * Search ClientConnection entity by Access-Token string and checks if entity is expired
 * Verify RSA Signature
 */
@Component
public class RequestResolver implements HandlerMethodArgumentResolver {
    private static Logger log = LoggerFactory.getLogger(RequestResolver.class);
    private ObjectMapper mapper = JsonTools.createDefaultMapper();
    @Autowired
    ClientConnectionsRepository repository;
    @Autowired
    private Environment environment;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        Class<?> type = parameter.getParameterType();
        return type.equals(ClientConnectionEntity.class)
                || type.getSuperclass().equals(AuthenticatedRequestAbs.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest httpServletRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (httpServletRequest == null) throw new BadRequest.WrongRequestFormat();

        String requestUrl = getRequestURL(httpServletRequest);
        String expiresAt = resolveExpiresAt(httpServletRequest);
        String requestBody = getRequestBody(httpServletRequest);
        String signature = getRequestSignature(httpServletRequest);
        ClientConnectionEntity connection = resolveAuthenticatorConnection(httpServletRequest);

        boolean verifySuccess = SignTools.verify(
                signature,
                httpServletRequest.getMethod(),
                requestUrl,
                expiresAt,
                requestBody,
                connection.getPublicKey()
        );
        if (!verifySuccess) throw new BadRequest.InvalidSignature();

        if (parameter.getParameterType() == ClientConnectionEntity.class) {
            return connection;
        } else if (parameter.getParameterType() == EmptyAuthenticatedRequest.class) {
            return new EmptyAuthenticatedRequest(connection);
        } else {
            Object result = mapper.readValue(requestBody, parameter.getParameterType());
            if (result instanceof AuthenticatedRequestAbs) {
                ((AuthenticatedRequestAbs) result).setConnection(connection);
            }
            return result;
        }
    }

    private String getRequestURL(HttpServletRequest httpServletRequest) {
        String result = httpServletRequest.getRequestURL().toString();
        if (Arrays.asList(environment.getActiveProfiles()).contains("dev")) {
            return result.contains("//localhost") ? result : result.replace("http://", "https://");
        } else {
            return result;
        }
    }

    private String getRequestSignature(HttpServletRequest httpServletRequest) {
        String result = httpServletRequest.getHeader(ScaSdkConstants.HEADER_KEY_SIGNATURE);
        if (StringUtils.isEmpty(result)) throw new BadRequest.SignatureMissing();
        return result;
    }

    private String getRequestBody(HttpServletRequest httpServletRequest) {
        try {
            return httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ClientConnectionEntity resolveAuthenticatorConnection(HttpServletRequest webRequest) {
        String accessToken = webRequest.getHeader(ScaSdkConstants.HEADER_KEY_ACCESS_TOKEN);
        if (StringUtils.isEmpty(accessToken)) throw new BadRequest.AccessTokenMissing();

        ClientConnectionEntity connection = repository.findByAccessTokenAndRevokedFalse(accessToken);
        if (connection == null) throw new Unauthorized.ConnectionNotFound();
        else if (StringUtils.isEmpty(connection.getUserId())) throw new Unauthorized.UserNotFound();
        else return connection;
    }

    private String resolveExpiresAt(HttpServletRequest webRequest) {
        String expiresAtHeader = webRequest.getHeader(ScaSdkConstants.HEADER_KEY_EXPIRES_AT);
        Integer expiresAt = null;
        try {
            if (expiresAtHeader != null) expiresAt = Integer.valueOf(expiresAtHeader);
        } catch (Exception e) {
            log.error("Invalid ExpiresAt header:", e);
        }
        if (expiresAt == null || expiresAt <= DateTools.nowUtcSeconds()) throw new BadRequest.SignatureExpired();
        return expiresAtHeader;
    }
}
