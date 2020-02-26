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
package com.saltedge.sca.sdk.tools;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class DateToolsTests {
	@Test
	public void convertDateToIso8601Test() {
		LocalDateTime ldt = LocalDateTime.of(2000, 1, 1, 9, 0).atOffset(ZoneOffset.ofHours(2)).toLocalDateTime();

		assertThat(DateTools.convertDateToIso8601(ldt)).isEqualTo("2000-01-01T07:00Z");
	}

	@Test
	public void dateIsExpiredTest() {
		LocalDateTime ldt = LocalDateTime.of(2000, 1, 1, 9, 0);

		assertThat(DateTools.dateIsExpired(ldt)).isTrue();

		ldt = LocalDateTime.now().plusSeconds(1);

		assertThat(DateTools.dateIsExpired(ldt)).isFalse();

		assertThat(DateTools.dateIsExpired(null)).isFalse();
	}

	@Test
	public void nowUtcSecondsTest() {
		assertThat(DateTools.nowUtcSeconds()).isCloseTo(Math.toIntExact((new Date().getTime() / 1000L)), Assertions.within(1));
	}
}
