package com.saltedge.authenticator.identity.controller.api

import com.saltedge.authenticator.identity.controller.api.CONFIGURATION_REQUEST_PATH
import com.saltedge.authenticator.identity.controller.api.ConfigurationController
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest(ConfigurationController::class)
class ConfigurationControllerIntegrationTests {
	@Autowired
	private val mvc: MockMvc? = null

	@Test
	fun getConfigurationTest() {
		mvc!!.perform(get(CONFIGURATION_REQUEST_PATH))
				.andExpect(status().isOk)
				.andExpect(jsonPath("$.data.connect_url", `is`("https://localhost")))
				.andExpect(jsonPath("$.data.code", `is`("spring-demobank")))
				.andExpect(jsonPath("$.data.name", `is`("Spring Demobank")))
				.andExpect(jsonPath("$.data.support_email", `is`("support@spring-demobank.com")))
				.andExpect(jsonPath("$.data.version", `is`("1")))
	}
}
