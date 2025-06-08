package com.puente.financialservice.financialinstrument.infrastructure.client;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
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
    }

    @Cacheable(value = "financialInstruments", key = "#symbol")
    public Optional<FinancialInstrument> fetchInstrumentData(String symbol) {
        try {
            rateLimitService.checkApiLimits();
            
            String url = buildApiUrl(symbol);
            logger.info("Fetching data for symbol: {}", symbol);
            
            AlphaVantageResponse response = restTemplate.getForObject(url, AlphaVantageResponse.class);
            
            if (isValidResponse(response)) {
                FinancialInstrument instrument = mapper.mapFromGlobalQuote(symbol, response.getGlobalQuote());
                logger.info("Successfully fetched data for symbol: {}", symbol);
                return Optional.of(instrument);
            }
            
            logger.warn("No data found for symbol: {}", symbol);
            return Optional.empty();
            
        } catch (HttpClientErrorException e) {
            handleHttpError(symbol, e);
            return Optional.empty();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Rate limit wait interrupted for symbol {}", symbol);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error fetching data for symbol {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    private String buildApiUrl(String symbol) {
        return String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                baseUrl, symbol, apiKey);
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