package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.infrastructure.client.AlphaVantageApiClient;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FinancialInstrumentSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentSchedulerService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // Intelligent rotation to respect API limits
    private static final int SYMBOLS_PER_CYCLE = 4; // Update 4 symbols each 5 minutes
    private final AtomicInteger rotationIndex = new AtomicInteger(0);
    private final AtomicInteger totalCyclesExecuted = new AtomicInteger(0);
    private LocalDateTime firstExecutionTime = null;
    
    private final AlphaVantageApiClient apiClient;
    private final FinancialInstrumentCacheService cacheService;

    public FinancialInstrumentSchedulerService(
            AlphaVantageApiClient apiClient,
            FinancialInstrumentCacheService cacheService) {
        this.apiClient = apiClient;
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init() {
        logger.info("🏁 SCHEDULER INITIALIZED:");
        logger.info("📊 Configuration: {} symbols per cycle, 5-minute interval", SYMBOLS_PER_CYCLE);
        logger.info("🎯 Total symbols to rotate: {}", PredefinedSymbols.SYMBOLS.size());
        logger.info("⏰ Estimated full cycle duration: {} minutes", 
            (PredefinedSymbols.SYMBOLS.size() * 5) / SYMBOLS_PER_CYCLE);
        logger.info("🚀 First execution will start in ~5 minutes");
        logger.info("📝 Symbols list: {}", PredefinedSymbols.SYMBOLS);
    }

    @Scheduled(fixedRate = 300000) // 5 minutes = 300,000 ms
    @Async("taskExecutor")
    @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(
        value = "spring.task.scheduling.enabled", 
        havingValue = "true", 
        matchIfMissing = true
    )
    public void updateInstrumentsIntelligently() {
        LocalDateTime startTime = LocalDateTime.now();
        int cycleNumber = totalCyclesExecuted.incrementAndGet();
        
        if (firstExecutionTime == null) {
            firstExecutionTime = startTime;
            logger.info("🎉 FIRST SCHEDULER EXECUTION!");
        }
        
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.info("🚀 CYCLE #{} - INTELLIGENT UPDATE STARTED", cycleNumber);
        logger.info("⏰ Start time: {}", startTime.format(formatter));
        logger.info("🕐 Running since: {} minutes", 
            java.time.Duration.between(firstExecutionTime, startTime).toMinutes());
        
        // Calculate which symbols to update in this cycle
        int totalSymbols = PredefinedSymbols.SYMBOLS.size();
        int startIndex = rotationIndex.get();
        int endIndex = Math.min(startIndex + SYMBOLS_PER_CYCLE, totalSymbols);
        
        logger.info("📊 ROTATION STRATEGY:");
        logger.info("   🎯 Updating symbols {} to {} (out of {})", 
            startIndex, endIndex - 1, totalSymbols);
        logger.info("   🔄 Rotation index: {} -> {}", startIndex, 
            endIndex >= totalSymbols ? 0 : endIndex);
        logger.info("   💡 Daily API usage: ~{} calls (limit: 25)", 
            (24 * 60 / 5) * SYMBOLS_PER_CYCLE / 60);
        
        // Show which symbols will be updated
        java.util.List<String> symbolsToUpdate = PredefinedSymbols.SYMBOLS.subList(startIndex, endIndex);
        logger.info("   📋 Symbols this cycle: {}", symbolsToUpdate);
        
        AtomicInteger updatedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        logger.info("🔄 STARTING API CALLS:");
        
        // Update only the selected symbols for this cycle
        for (int i = startIndex; i < endIndex; i++) {
            String symbol = PredefinedSymbols.SYMBOLS.get(i);
            int symbolPosition = i - startIndex + 1;
            int symbolsInCycle = endIndex - startIndex;
            
            try {
                logger.info("   📡 [{}/{}] Fetching {} (global position: {}/{})", 
                    symbolPosition, symbolsInCycle, symbol, i + 1, totalSymbols);
                
                long apiCallStart = System.currentTimeMillis();
                
                apiClient.fetchInstrumentData(symbol)
                    .ifPresentOrElse(
                        instrument -> {
                            long apiCallDuration = System.currentTimeMillis() - apiCallStart;
                            cacheService.putInCache(symbol, instrument);
                            
                            boolean hasRealData = instrument.getCurrentPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
                            String dataType = hasRealData ? "REAL DATA" : "DUMMY DATA";
                            
                            logger.info("   ✅ [{}/{}] {} SUCCESS: {} - Price: {}, Change: {} ({}ms) - {}", 
                                symbolPosition, symbolsInCycle, symbol, 
                                instrument.getCurrentPrice(), instrument.getChange(), 
                                apiCallDuration, dataType);
                            updatedCount.incrementAndGet();
                        },
                        () -> {
                            long apiCallDuration = System.currentTimeMillis() - apiCallStart;
                            logger.warn("   ⚠️ [{}/{}] {} NO DATA received ({}ms)", 
                                symbolPosition, symbolsInCycle, symbol, apiCallDuration);
                            errorCount.incrementAndGet();
                        }
                    );
                    
                // Small delay between API calls to be respectful
                if (i < endIndex - 1) { // Don't wait after the last call
                    logger.debug("   ⏳ Waiting 1 second before next API call...");
                    Thread.sleep(1000);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("   🛑 [{}/{}] Thread interrupted while updating symbol: {}", 
                    symbolPosition, symbolsInCycle, symbol);
                break;
            } catch (Exception e) {
                logger.error("   ❌ [{}/{}] Error updating {}: {}", 
                    symbolPosition, symbolsInCycle, symbol, e.getMessage());
                errorCount.incrementAndGet();
            }
        }
        
        // Update rotation index for next cycle
        int nextIndex = endIndex >= totalSymbols ? 0 : endIndex;
        rotationIndex.set(nextIndex);
        
        // Calculate next symbols that will be updated
        int nextStartIndex = nextIndex;
        int nextEndIndex = Math.min(nextStartIndex + SYMBOLS_PER_CYCLE, totalSymbols);
        if (nextEndIndex == totalSymbols && nextStartIndex < totalSymbols) {
            // If we're at the end, show that next cycle will start from beginning
            nextEndIndex = totalSymbols;
        }
        
        cacheService.updateLastUpdateTime();
        
        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = java.time.Duration.between(startTime, endTime).getSeconds();
        
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        logger.info("✅ CYCLE #{} COMPLETED", cycleNumber);
        logger.info("⏱️  Duration: {} seconds", durationSeconds);
        logger.info("📈 Results: {} successful, {} errors", updatedCount.get(), errorCount.get());
        
        // Show cache statistics
        int totalCached = cacheService.getCacheSize();
        AtomicInteger symbolsWithRealData = new AtomicInteger(0);
        
        // Count symbols with real data
        for (String symbol : PredefinedSymbols.SYMBOLS) {
            cacheService.getFromCache(symbol).ifPresent(instrument -> {
                if (instrument.getCurrentPrice().compareTo(java.math.BigDecimal.ZERO) > 0) {
                    symbolsWithRealData.incrementAndGet();
                }
            });
        }
        
        logger.info("📊 CACHE STATUS:");
        logger.info("   🗃️  Total cached: {} / {} symbols", totalCached, PredefinedSymbols.SYMBOLS.size());
        logger.info("   💰 With real prices: {} / {} ({}%)", symbolsWithRealData.get(), totalCached,
            totalCached > 0 ? (symbolsWithRealData.get() * 100) / totalCached : 0);
        
        // Show next cycle information
        String nextSymbolsInfo = nextIndex == 0 ? 
            "0-" + (SYMBOLS_PER_CYCLE - 1) + " (🔄 RESTART CYCLE)" : 
            nextStartIndex + "-" + (nextEndIndex - 1);
            
        logger.info("🔄 NEXT CYCLE:");
        logger.info("   ⏰ Scheduled for: {}", endTime.plusMinutes(5).format(formatter));
        logger.info("   📋 Will update symbols: {}", nextSymbolsInfo);
        
        // Progress through full rotation
        double rotationProgress = ((double) (nextIndex == 0 ? totalSymbols : nextIndex) / totalSymbols) * 100;
        logger.info("   📊 Full rotation progress: {:.1f}%", rotationProgress);
        
        if (nextIndex == 0) {
            logger.info("🎯 FULL ROTATION COMPLETED! Starting over...");
        }
        
        logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
} 