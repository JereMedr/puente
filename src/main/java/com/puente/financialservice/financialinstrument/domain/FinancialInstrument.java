package com.puente.financialservice.financialinstrument.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "financial_instruments")
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInstrument {
    @Id
    private String symbol;
    private String name;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal changePercent;
    private LocalDateTime lastUpdated;
} 