package com.puente.financialservice.financialinstrument.infrastructure.controller;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.service.AlphaVantageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/instruments")
@Tag(name = "Financial Instruments", description = "API for managing financial instruments")
public class FinancialInstrumentController {

    private final AlphaVantageService alphaVantageService;

    public FinancialInstrumentController(AlphaVantageService alphaVantageService) {
        this.alphaVantageService = alphaVantageService;
    }

    @GetMapping
    @Operation(summary = "Get all predefined financial instruments")
    public ResponseEntity<List<FinancialInstrument>> getAllInstruments() {
        return ResponseEntity.ok(alphaVantageService.getAllPredefinedInstruments());
    }

    @GetMapping("/{symbol}")
    @Operation(summary = "Get a specific financial instrument by symbol")
    public ResponseEntity<FinancialInstrument> getInstrumentBySymbol(@PathVariable String symbol) {
        return alphaVantageService.getInstrumentData(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{symbol}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force sync of a specific instrument", description = "Admin only - Forces an update of the instrument data from Alpha Vantage")
    public ResponseEntity<FinancialInstrument> syncInstrument(@PathVariable String symbol) {
        return alphaVantageService.getInstrumentData(symbol)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/sync-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force sync of all instruments", description = "Admin only - Forces an update of all instruments data from Alpha Vantage")
    public ResponseEntity<List<FinancialInstrument>> syncAllInstruments() {
        return ResponseEntity.ok(alphaVantageService.getAllPredefinedInstruments());
    }
} 