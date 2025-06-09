package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.client.AlphaVantageApiClient;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import com.puente.financialservice.financialinstrument.infrastructure.mapper.FinancialInstrumentMapper;
import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    public void init() {
        logger.info("Initializing AlphaVantageService - Cache size: {}", cacheService.getCacheSize());
    }

    public Optional<FinancialInstrument> getInstrumentData(String symbol) {
        logger.info("🎯 Getting instrument data for symbol: {}", symbol);
        
        // First try to get from cache
        Optional<FinancialInstrument> cachedInstrument = cacheService.getFromCache(symbol);
        if (cachedInstrument.isPresent()) {
            FinancialInstrument cached = cachedInstrument.get();
            boolean hasRealData = cached.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0;
            
            if (hasRealData) {
                logger.info("📥 Found valid cached data for symbol: {} - price: {}", 
                    symbol, cached.getCurrentPrice());
                return cachedInstrument;
            } else {
                logger.warn("📥 Found cached data for {} but it's dummy data (price=0), will try API", symbol);
                // Don't return dummy data, try API instead
            }
        }

        logger.info("🚀 No valid cached data found for {}, fetching from API...", symbol);
        
        // If not in cache or dummy data, fetch from API
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

    public java.time.LocalDateTime getLastUpdateTime() {
        return cacheService.getLastUpdateTime();
    }

    public int getCacheSize() {
        return cacheService.getCacheSize();
    }

    public Optional<FinancialInstrument> getCachedInstrument(String symbol) {
        return cacheService.getFromCache(symbol);
    }
} 