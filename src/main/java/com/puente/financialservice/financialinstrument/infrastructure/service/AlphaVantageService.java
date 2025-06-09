package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.client.AlphaVantageApiClient;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import com.puente.financialservice.financialinstrument.infrastructure.mapper.FinancialInstrumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public Optional<FinancialInstrument> getInstrumentData(String symbol) {
        logger.info("🔍 Getting instrument data for symbol: {}", symbol);
        
        // First try to get from cache
        Optional<FinancialInstrument> cached = cacheService.getFromCache(symbol);
        if (cached.isPresent()) {
            FinancialInstrument instrument = cached.get();
            boolean hasRealData = instrument.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0;
            
            if (hasRealData) {
                logger.info("✅ Found REAL data in cache for {}: price={}", 
                    symbol, instrument.getCurrentPrice());
                return cached;
            } else {
                logger.info("⚠️ Found cached data for {} but price is 0, will try API", symbol);
            }
        } else {
            logger.info("🔍 No cache entry found for {}, will try API", symbol);
        }
        
        // If not in cache or cached data is empty, try to fetch from API
        Optional<FinancialInstrument> fetchedInstrument = apiClient.fetchInstrumentData(symbol);
        
        if (fetchedInstrument.isPresent()) {
            FinancialInstrument fetched = fetchedInstrument.get();
            boolean hasRealData = fetched.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0;
            
            if (hasRealData) {
                logger.info("✅ Successfully fetched REAL data from API for {}: price={}", 
                    symbol, fetched.getCurrentPrice());
                cacheService.putInCache(symbol, fetched);
                return fetchedInstrument;
            } else {
                logger.warn("⚠️ API returned data for {} but price is 0 (possible API issue)", symbol);
                cacheService.putInCache(symbol, fetched);
                return fetchedInstrument;
            }
        } else {
            logger.error("❌ Failed to fetch data from API for symbol: {}", symbol);
            
            // As fallback, create empty instrument so we have something to return
            FinancialInstrument emptyInstrument = mapper.createEmptyInstrument(symbol);
            logger.info("🔄 Creating empty instrument as fallback for {}", symbol);
            cacheService.putInCache(symbol, emptyInstrument);
            return Optional.of(emptyInstrument);
        }
    }

    public List<FinancialInstrument> getAllPredefinedInstruments() {
        return cacheService.getCachedInstruments(PredefinedSymbols.SYMBOLS, mapper);
    }

    public void clearCache() {
        cacheService.clearCache();
    }
} 