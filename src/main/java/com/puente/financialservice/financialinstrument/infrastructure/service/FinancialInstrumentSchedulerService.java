package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.infrastructure.client.AlphaVantageApiClient;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FinancialInstrumentSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentSchedulerService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final AlphaVantageApiClient apiClient;
    private final FinancialInstrumentCacheService cacheService;

    public FinancialInstrumentSchedulerService(
            AlphaVantageApiClient apiClient,
            FinancialInstrumentCacheService cacheService) {
        this.apiClient = apiClient;
        this.cacheService = cacheService;
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    @Async("taskExecutor")
    public void updateAllInstruments() {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("Starting scheduled update of all instruments at {}", startTime.format(formatter));
        
        int updatedCount = 0;
        for (String symbol : PredefinedSymbols.SYMBOLS) {
            try {
                apiClient.fetchInstrumentData(symbol)
                    .ifPresent(instrument -> {
                        cacheService.putInCache(symbol, instrument);
                        logger.info("Updated data for symbol: {} - Price: {}", 
                            symbol, instrument.getCurrentPrice());
                    });
                updatedCount++;
            } catch (Exception e) {
                logger.error("Error updating symbol {}: {}", symbol, e.getMessage());
            }
        }
        
        cacheService.updateLastUpdateTime();
        logger.info("Completed scheduled update. Updated {} instruments. Next update in 5 minutes at {}", 
            updatedCount, cacheService.getLastUpdateTime().plusMinutes(5).format(formatter));
    }
} 