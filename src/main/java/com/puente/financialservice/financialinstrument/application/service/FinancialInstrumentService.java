package com.puente.financialservice.financialinstrument.application.service;

import com.puente.financialservice.financialinstrument.domain.model.FinancialInstrument;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentRepository;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentExternalService;
import com.puente.financialservice.financialinstrument.application.dto.FinancialInstrumentDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialInstrumentService {
    private final FinancialInstrumentRepository repository;
    private final FinancialInstrumentExternalService externalService;

    public FinancialInstrumentService(
            FinancialInstrumentRepository repository,
            FinancialInstrumentExternalService externalService) {
        this.repository = repository;
        this.externalService = externalService;
    }

    public List<FinancialInstrumentDTO> getAllInstruments() {
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FinancialInstrumentDTO getInstrumentBySymbol(String symbol) {
        return repository.findBySymbol(symbol)
                .map(this::mapToDTO)
                .orElseThrow(() -> new RuntimeException("Instrument not found"));
    }

    private FinancialInstrumentDTO mapToDTO(FinancialInstrument instrument) {
        return new FinancialInstrumentDTO(
            instrument.getSymbol(),
            instrument.getName(),
            instrument.getCurrentPrice(),
            instrument.getPreviousClose(),
            instrument.getChange(),
            instrument.getChangePercent(),
            instrument.getLastUpdated()
        );
    }
} 