package com.puente.financialservice.financialinstrument.domain.port;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import java.util.Optional;

public interface FinancialInstrumentExternalService {
    Optional<FinancialInstrument> getInstrumentData(String symbol);
    void updateInstrumentData(FinancialInstrument instrument);
} 