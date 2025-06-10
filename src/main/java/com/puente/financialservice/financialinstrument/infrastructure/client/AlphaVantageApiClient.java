package com.puente.financialservice.financialinstrument.infrastructure.client;

import com.puente.financialservice.financialinstrument.domain.model.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.dto.AlphaVantageResponse;
import com.puente.financialservice.financialinstrument.infrastructure.mapper.FinancialInstrumentMapper;
import com.puente.financialservice.financialinstrument.infrastructure.service.ApiRateLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class AlphaVantageApiClient {
    
    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageApiClient.class);
    
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;
    private final ApiRateLimitService rateLimitService;
    private final FinancialInstrumentMapper mapper;

    public AlphaVantageApiClient(
            RestTemplate restTemplate,
            @Value("${app.alpha-vantage.base-url}") String baseUrl,
            @Value("${app.alpha-vantage.api-key:demo}") String apiKey,
            ApiRateLimitService rateLimitService,
            FinancialInstrumentMapper mapper) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.rateLimitService = rateLimitService;
        this.mapper = mapper;
        
        // Detailed logging for debugging configuration issues
        logger.info("🔧 AlphaVantageApiClient Configuration Debug:");
        logger.info("🌐 Base URL: {}", baseUrl);
        
        // Log API key configuration (mask for security)
        String maskedApiKey = apiKey != null && apiKey.length() > 4 ? 
            apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4) : 
            "***INVALID***";
        logger.info("🔑 API Key loaded: {}", maskedApiKey);
        
        // Check if we're getting the default value
        if ("demo".equals(apiKey)) {
            logger.error("❌ CONFIGURATION PROBLEM: API key is 'demo' - property 'app.alpha-vantage.api-key' not found or not loaded!");
            logger.error("🔍 This means the application.properties value is not being read correctly.");
        } else if (apiKey == null || apiKey.trim().isEmpty()) {
            logger.error("❌ CONFIGURATION PROBLEM: API key is null or empty!");
        } else {
            logger.info("✅ API key loaded successfully from properties");
        }
    }

    @Cacheable(value = "financialInstruments", key = "#symbol")
    public Optional<FinancialInstrument> fetchInstrumentData(String symbol) {
        try {
            rateLimitService.checkApiLimits();
            
            String url = buildApiUrl(symbol);
            logger.info("🌐 Making API call to URL: {}", url);
            
            // First, get the raw response as String to see what we're getting
            String rawResponse = restTemplate.getForObject(url, String.class);
            logger.info("📦 Raw API Response for {}: {}", symbol, rawResponse);
            
            // Now try to parse it as our DTO
            AlphaVantageResponse response = restTemplate.getForObject(url, AlphaVantageResponse.class);
            logger.info("🔍 Parsed response object: {}", response != null ? "NOT NULL" : "NULL");
            
            if (response != null && response.getGlobalQuote() != null) {
                logger.info("✅ GlobalQuote found: {}", response.getGlobalQuote());
                logger.info("📊 GlobalQuote details - Symbol: {}, Price: {}, Change: {}", 
                    response.getGlobalQuote().getSymbol(),
                    response.getGlobalQuote().getPrice(),
                    response.getGlobalQuote().getChange());
                    
                FinancialInstrument instrument = mapper.mapFromGlobalQuote(symbol, response.getGlobalQuote());
                logger.info("💰 Successfully mapped instrument for {}: price={}", symbol, instrument.getCurrentPrice());
                return Optional.of(instrument);
            } else {
                if (response == null) {
                    logger.error("❌ Response object is NULL for symbol: {}", symbol);
                } else {
                    logger.error("❌ GlobalQuote is NULL in response for symbol: {}", symbol);
                }
                return Optional.empty();
            }
            
        } catch (HttpClientErrorException e) {
            logger.error("🔴 HTTP Error for {}: status={}, body={}", symbol, e.getStatusCode(), e.getResponseBodyAsString());
            handleHttpError(symbol, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("⏸️ Rate limit wait interrupted for symbol {}", symbol);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("💥 Unexpected error for {}: {}", symbol, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private String buildApiUrl(String symbol) {
        String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                baseUrl, symbol, apiKey);
        
        // Debug logging to see what API key is actually being used
        String maskedApiKey = apiKey != null && apiKey.length() > 4 ? 
            apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4) : 
            "***INVALID***";
        logger.debug("🔧 Building URL with API key: {}", maskedApiKey);
        
        return url;
    }

    private boolean isValidResponse(AlphaVantageResponse response) {
        return response != null && response.getGlobalQuote() != null;
    }

    private void handleHttpError(String symbol, HttpClientErrorException e) {
        if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
            logger.error("API rate limit exceeded for symbol {}: {}", symbol, e.getMessage());
        } else {
            logger.error("HTTP error fetching data for symbol {}: {}", symbol, e.getMessage());
        }
    }
} 