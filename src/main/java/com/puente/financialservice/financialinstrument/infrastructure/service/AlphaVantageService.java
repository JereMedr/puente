package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.client.AlphaVantageApiClient;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import com.puente.financialservice.financialinstrument.infrastructure.mapper.FinancialInstrumentMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlphaVantageService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageService.class);
    
    private final AlphaVantageApiClient apiClient;
    private final FinancialInstrumentCacheService cacheService;
    private final FinancialInstrumentMapper mapper;

    public AlphaVantageService(
            AlphaVantageApiClient apiClient,
            FinancialInstrumentCacheService cacheService,
            FinancialInstrumentMapper mapper) {
        this.apiClient = apiClient;
        this.cacheService = cacheService;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing AlphaVantageService - Cache size: {}", cacheService.getCacheSize());
    }

    public Optional<FinancialInstrument> getInstrumentData(String symbol) {
        // First try to get from cache
        Optional<FinancialInstrument> cachedInstrument = cacheService.getFromCache(symbol);
        if (cachedInstrument.isPresent()) {
            logger.debug("Returning cached data for symbol: {}", symbol);
            return cachedInstrument;
        }

        // If not in cache, fetch from API
        Optional<FinancialInstrument> fetchedInstrument = apiClient.fetchInstrumentData(symbol);
        fetchedInstrument.ifPresent(instrument -> cacheService.putInCache(symbol, instrument));
        
        return fetchedInstrument;
    }

    public List<FinancialInstrument> getAllPredefinedInstruments() {
        return cacheService.getCachedInstruments(PredefinedSymbols.SYMBOLS, mapper);
    }

    public void clearCache() {
        cacheService.clearCache();
    }
} 