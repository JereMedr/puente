package com.puente.financialservice.financialinstrument.application.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentExternalService;
import com.puente.financialservice.financialinstrument.application.dto.FinancialInstrumentDTO;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
public class FinancialInstrumentService {
    private final ConcurrentHashMap<String, FinancialInstrument> instrumentCache;
    private final FinancialInstrumentExternalService externalService;

    public FinancialInstrumentService(FinancialInstrumentExternalService externalService) {
        this.instrumentCache = new ConcurrentHashMap<>();
        this.externalService = externalService;
    }

    public List<FinancialInstrumentDTO> getAllInstruments() {
        return instrumentCache.values().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "financialInstruments", key = "#symbol")
    public FinancialInstrumentDTO getInstrumentBySymbol(String symbol) {
        return Optional.ofNullable(instrumentCache.get(symbol))
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));
    }

    @CachePut(value = "financialInstruments", key = "#symbol")
    public FinancialInstrumentDTO updateInstrumentData(String symbol) {
        final FinancialInstrument instrument = Optional.ofNullable(instrumentCache.get(symbol))
                .orElseGet(() -> {
                    FinancialInstrument newInstrument = new FinancialInstrument();
                    newInstrument.setSymbol(symbol);
                    return newInstrument;
                });

        externalService.getInstrumentData(symbol)
                .ifPresent(updatedData -> {
                    // Actualizar información básica
                    instrument.setName(updatedData.getName());
                    instrument.setType(updatedData.getType());
                    instrument.setRegion(updatedData.getRegion());
                    instrument.setMarketOpen(updatedData.getMarketOpen());
                    instrument.setMarketClose(updatedData.getMarketClose());
                    instrument.setTimezone(updatedData.getTimezone());
                    instrument.setCurrency(updatedData.getCurrency());
                    instrument.setMatchScore(updatedData.getMatchScore());
                    
                    // Actualizar información de precios
                    instrument.setCurrentPrice(updatedData.getCurrentPrice());
                    instrument.setPreviousClose(updatedData.getPreviousClose());
                    instrument.setChange(updatedData.getChange());
                    instrument.setChangePercent(updatedData.getChangePercent());
                    instrument.setLastUpdated(LocalDateTime.now());
                    
                    instrumentCache.put(symbol, instrument);
                });

        return mapToDTO(instrument);
    }

    public void syncAllInstruments() {
        instrumentCache.keySet().forEach(this::updateInstrumentData);
    }

    private FinancialInstrumentDTO mapToDTO(FinancialInstrument instrument) {
        return new FinancialInstrumentDTO(
            instrument.getSymbol(),
            instrument.getName(),
            instrument.getType(),
            instrument.getRegion(),
            instrument.getMarketOpen(),
            instrument.getMarketClose(),
            instrument.getTimezone(),
            instrument.getCurrency(),
            instrument.getMatchScore(),
            instrument.getCurrentPrice(),
            instrument.getPreviousClose(),
            instrument.getChange(),
            instrument.getChangePercent(),
            instrument.getLastUpdated()
        );
    }
} 