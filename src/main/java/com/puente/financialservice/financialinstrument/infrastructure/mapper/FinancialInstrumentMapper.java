package com.puente.financialservice.financialinstrument.infrastructure.mapper;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.dto.GlobalQuote;
import com.puente.financialservice.financialinstrument.infrastructure.service.CompanyNameService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class FinancialInstrumentMapper {
    
    private final CompanyNameService companyNameService;

    public FinancialInstrumentMapper(CompanyNameService companyNameService) {
        this.companyNameService = companyNameService;
    }

    public FinancialInstrument mapFromGlobalQuote(String symbol, GlobalQuote quote) {
        if (quote == null) {
            throw new IllegalArgumentException("GlobalQuote cannot be null");
        }

        try {
            return new FinancialInstrument(
                symbol,
                companyNameService.getCompanyName(symbol),
                parseToDecimal(quote.getPrice()),
                parseToDecimal(quote.getPreviousClose()),
                parseToDecimal(quote.getChange()),
                parseToDecimal(quote.getChangePercent().replace("%", "")),
                LocalDateTime.now()
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parsing numeric values for symbol " + symbol, e);
        }
    }

    public FinancialInstrument createEmptyInstrument(String symbol) {
        return new FinancialInstrument(
            symbol,
            companyNameService.getCompanyName(symbol),
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            LocalDateTime.now()
        );
    }

    private BigDecimal parseToDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }
} 