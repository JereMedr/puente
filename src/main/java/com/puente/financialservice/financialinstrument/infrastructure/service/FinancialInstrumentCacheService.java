package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.mapper.FinancialInstrumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FinancialInstrumentCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentCacheService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final ConcurrentHashMap<String, FinancialInstrument> instrumentCache = new ConcurrentHashMap<>();
    private LocalDateTime lastUpdateTime = LocalDateTime.now();

    public Optional<FinancialInstrument> getFromCache(String symbol) {
        FinancialInstrument cached = instrumentCache.get(symbol);
        return Optional.ofNullable(cached);
    }

    public void putInCache(String symbol, FinancialInstrument instrument) {
        instrumentCache.put(symbol, instrument);
    }

    public List<FinancialInstrument> getCachedInstruments(List<String> symbols, FinancialInstrumentMapper mapper) {
        logger.info("Getting cached instruments. Last update: {}", lastUpdateTime.format(formatter));
        
        return symbols.stream()
                .map(symbol -> instrumentCache.getOrDefault(symbol, mapper.createEmptyInstrument(symbol)))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = "financialInstruments", allEntries = true)
    public void clearCache() {
        instrumentCache.clear();
        logger.info("Cache cleared at {}", LocalDateTime.now().format(formatter));
    }

    public void updateLastUpdateTime() {
        this.lastUpdateTime = LocalDateTime.now();
    }

    public LocalDateTime getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getCacheSize() {
        return instrumentCache.size();
    }
} 