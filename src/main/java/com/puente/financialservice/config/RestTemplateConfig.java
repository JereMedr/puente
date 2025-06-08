package com.puente.financialservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;

@Configuration
public class RestTemplateConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);
    
    @Value("${app.alpha-vantage.api-key:NOT_LOADED}")
    private String alphaVantageApiKey;
    
    @Value("${app.alpha-vantage.base-url:NOT_LOADED}")
    private String alphaVantageBaseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @PostConstruct
    public void verifyConfiguration() {
        logger.info("🔧 Configuration Verification at Startup:");
        logger.info("🌐 Alpha Vantage Base URL: {}", alphaVantageBaseUrl);
        
        // Mask API key for security logging
        String maskedKey = alphaVantageApiKey != null && alphaVantageApiKey.length() > 4 && !"NOT_LOADED".equals(alphaVantageApiKey) ?
            alphaVantageApiKey.substring(0, 4) + "***" + alphaVantageApiKey.substring(alphaVantageApiKey.length() - 4) :
            alphaVantageApiKey;
        logger.info("🔑 Alpha Vantage API Key: {}", maskedKey);
        
        if ("NOT_LOADED".equals(alphaVantageApiKey)) {
            logger.error("❌ CRITICAL: Alpha Vantage API key not loaded from properties!");
            logger.error("🔍 Check if application.properties is in the correct location and accessible.");
        }
        
        if ("NOT_LOADED".equals(alphaVantageBaseUrl)) {
            logger.error("❌ CRITICAL: Alpha Vantage base URL not loaded from properties!");
        }
    }
} 