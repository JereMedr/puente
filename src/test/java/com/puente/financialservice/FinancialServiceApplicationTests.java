package com.puente.financialservice;

import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentExternalService;
import com.puente.financialservice.user.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class FinancialServiceApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	@MockBean
	private FinancialInstrumentExternalService financialInstrumentExternalService;

	@MockBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
		// Verifica que el contexto de Spring se carga correctamente
	}

	@Test
	void healthCheck() {
		webTestClient.get()
			.uri("/actuator/health")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.status").isEqualTo("UP");
	}

}
