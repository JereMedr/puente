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
            return FinancialInstrument.builder()
                .symbol(symbol)
                .name(companyNameService.getCompanyName(symbol))
                .type("EQUITY")
                .region("US")
                .marketOpen("09:30")
                .marketClose("16:00")
                .timezone("America/New_York")
                .currency("USD")
                .matchScore(1.0)
                .currentPrice(parseToDecimal(quote.getPrice()))
                .previousClose(parseToDecimal(quote.getPreviousClose()))
                .change(parseToDecimal(quote.getChange()))
                .changePercent(parseToDecimal(quote.getChangePercent().replace("%", "")))
                .lastUpdated(LocalDateTime.now())
                .build();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Error parsing numeric values for symbol " + symbol, e);
        }
    }

    public FinancialInstrument createEmptyInstrument(String symbol) {
        return FinancialInstrument.builder()
            .symbol(symbol)
            .name(companyNameService.getCompanyName(symbol))
            .type("EQUITY")
            .region("US")
            .marketOpen("09:30")
            .marketClose("16:00")
            .timezone("America/New_York")
            .currency("USD")
            .matchScore(1.0)
            .currentPrice(BigDecimal.ZERO)
            .previousClose(BigDecimal.ZERO)
            .change(BigDecimal.ZERO)
            .changePercent(BigDecimal.ZERO)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    private BigDecimal parseToDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }
} 