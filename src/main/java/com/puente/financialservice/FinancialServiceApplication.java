package com.puente.financialservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.puente.financialservice")
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = "com.puente.financialservice")
public class FinancialServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinancialServiceApplication.class, args);
	}

}
