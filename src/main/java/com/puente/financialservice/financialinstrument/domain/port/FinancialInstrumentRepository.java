package com.puente.financialservice.financialinstrument.domain.port;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import java.util.List;
import java.util.Optional;

public interface FinancialInstrumentRepository {
    FinancialInstrument save(FinancialInstrument instrument);
    Optional<FinancialInstrument> findBySymbol(String symbol);
    List<FinancialInstrument> findAll();
    void deleteBySymbol(String symbol);
} 