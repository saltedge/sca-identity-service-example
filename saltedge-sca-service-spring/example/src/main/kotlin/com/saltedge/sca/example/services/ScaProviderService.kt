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

import com.saltedge.sca.example.controller.SCA_ACTION_LOGIN
import com.saltedge.sca.example.controller.SCA_ACTION_PAYMENT
import com.saltedge.sca.example.controller.SIGN_IN_SCA_PATH
import com.saltedge.sca.example.model.ConsentsRepository
import com.saltedge.sca.example.tools.AuthorizationTemplate
import com.saltedge.sca.example.tools.getApplicationUrl
import com.saltedge.sca.sdk.ScaSdkConstants.KEY_SECRET
import com.saltedge.sca.sdk.models.*
import com.saltedge.sca.sdk.provider.ServiceProvider
import com.saltedge.sca.sdk.tools.CodeBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Provides required by SCA module information and receives Action and Authorization events.
 */
@Service
class ScaProviderService : ServiceProvider {
    private val log: Logger = LoggerFactory.getLogger(ScaProviderService::class.java)
    @Autowired
    lateinit var env: Environment
    @Autowired
    lateinit var usersService: UsersService
    @Autowired
    lateinit var paymentsConfirmService: PaymentsConfirmService
    @Autowired
    lateinit var consentsRepository: ConsentsRepository

    /**
     * Provides human readable name of Service Provider
     *
     * @return name
     */
    override fun getProviderName(): String {
        return "Spring Bank Example"
    }

    /**
     * Provides email of Service Provider for clients support
     *
     * @return email string
     */
    override fun getProviderSupportEmail(): String {
        return "support@spring-demobank.com"
    }

    /**
     * Provides code name of Service Provider
     *
     * @return name
     */
    override fun getProviderCode(): String {
        return "spring-bank"
    }

    /**
     * Provides logo image of Service Provider
     *
     * @return url string
     */
    override fun getProviderLogoUrl(): String {
        return "https://s3-media1.fl.yelpcdn.com/bphoto/9J0LUrYkKYuwcICwQztkxw/ls.jpg"
    }

    /**
     * Find User entity by authentication session secret code.
     * Authentication session secret code is created
     * when user already authenticated and want to connect Authenticator app
     *
     * @param sessionSecret code.
     * @return UserIdentity
     */
    override fun getUserIdByAuthenticationSessionSecret(sessionSecret: String?): UserIdentity? {
        val userId: String? = usersService.findUserIdByAuthSessionCode(sessionSecret)
        return if (userId.isNullOrBlank()) null
        else {
            UserIdentity(
                    userId,
                    CodeBuilder.generateRandomString(),
                    Instant.now().plus(30, ChronoUnit.DAYS)
            )
        }
    }

    /**
     * Provides URL of authentication page of Service Provider
     * for redirection in Authenticator app.
     *
     * @param enrollSessionSecret code related to Authenticator connection
     * @return url string
     */
    override fun getAuthorizationPageUrl(enrollSessionSecret: String?): String? {
        return try {
            val urlString = getApplicationUrl(env)!!
            UriComponentsBuilder.fromUriString(urlString)
                    .path(SIGN_IN_SCA_PATH)
                    .apply { if (!enrollSessionSecret.isNullOrBlank()) queryParam(KEY_SECRET, enrollSessionSecret) }
                    .build().toUriString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Notifies application about receiving new authenticated Action request.
     * It can be Sign-in to portal action or Payment action which requires authentication.
     *
     * @param action entity with uuid and userId
     * @return return authorization content (title, description) for creating Authorization object (SCA confirmation),
     *         if null then user will receive Instant action error.
     */
    override fun onAuthenticateAction(action: AuthenticateAction): AuthorizationContent? {
        if (action.isExpired) return null
        val userId = action.userId.toLongOrNull() ?: return null
        val user = usersService.findUser(userId) ?: return null
        return when (action.code) {
            SCA_ACTION_LOGIN -> {
                AuthorizationContent(
                        action.uuid,
                        "Access to Account Information",
                        AuthorizationTemplate.createHTMLDescriptionForAisp(user.name, "Salt Edge SCA Service Example (Spring)")
                )
            }
            SCA_ACTION_PAYMENT -> {
                paymentsConfirmService.authenticateDemoPaymentOrder(paymentUUID = action.uuid, userId = userId)
            }
            else -> null
        }
    }

    /**
     * Notifies application about confirmation or denying of SCA Authorization
     *
     * @param authorization entity with unique authorizationCode and isConfirmed fields
     */
    override fun onAuthorizationConfirmed(authorization: Authorization) {
        paymentsConfirmService.authorizePaymentOrder(
                paymentUUID = authorization.authorizationCode ?: return,
                confirmed = authorization.confirmed ?: return
        )
    }

    override fun getActiveConsents(userId: String?): List<Consent> {
        return consentsRepository.findByUserIdAndRevokedFalseAndExpiresAtGreaterThan(
                userId = userId?.toLongOrNull() ?: return emptyList(),
                currentDate = Instant.now()
        ).map {
            Consent(
                it.id.toString(),
                it.title,
                it.description,
                it.createdAt,
                it.expiresAt
            )
        }
    }

    override fun revokeConsent(userId: String?, consentId: String?): Boolean {
        var consent = consentsRepository.findFirstByIdAndUserId(
                id = consentId?.toLongOrNull() ?: return false,
                userId = userId?.toLongOrNull() ?: return false
        ) ?: return false
        consent.revoked = true
        consent = consentsRepository.save(consent)
        return consent.revoked
    }
}
