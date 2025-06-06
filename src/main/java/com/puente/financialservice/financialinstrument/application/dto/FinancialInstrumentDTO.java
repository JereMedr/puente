package com.puente.financialservice.financialinstrument.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInstrumentDTO {
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private LocalDateTime lastUpdated;
} 