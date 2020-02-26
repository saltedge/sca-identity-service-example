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
package com.saltedge.sca.example

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

const val EXAMPLE_PACKAGE = "com.saltedge.sca.example"
const val SDK_PACKAGE = "com.saltedge.sca.sdk"

@SpringBootApplication(scanBasePackages = [EXAMPLE_PACKAGE, SDK_PACKAGE])
@EnableJpaRepositories(basePackages = [EXAMPLE_PACKAGE, SDK_PACKAGE])
@EntityScan(basePackages = [EXAMPLE_PACKAGE, SDK_PACKAGE])
open class ScaExampleApplication

fun main(args: Array<String>) {
    runApplication<ScaExampleApplication>(*args)
}
