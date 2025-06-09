package com.puente.financialservice.financialinstrument.infrastructure.controller;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.service.AlphaVantageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/instruments")
@Tag(name = "Financial Instruments", description = "API for managing financial instruments")
public class FinancialInstrumentController {

    private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentController.class);
    private final AlphaVantageService alphaVantageService;

    public FinancialInstrumentController(AlphaVantageService alphaVantageService) {
        this.alphaVantageService = alphaVantageService;
    }

    @GetMapping
    @Operation(summary = "Get all predefined financial instruments")
    public ResponseEntity<List<FinancialInstrument>> getAllInstruments() {
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/instruments - Get all predefined financial instruments");
        logger.info("📋 Fetching all predefined financial instruments");
        List<FinancialInstrument> instruments = alphaVantageService.getAllPredefinedInstruments();
        logger.info("✅ ENDPOINT RESPONSE: GET /api/v1/instruments - Returning {} instruments", instruments.size());
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/{symbol}")
    @Operation(
        summary = "Get financial instrument by symbol", 
        description = "Retrieves detailed information about a specific financial instrument using its stock symbol (e.g., AAPL, MSFT, GOOGL). Note: Limited by Alpha Vantage API rate limits (5 calls/minute for free tier)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instrument found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid symbol format or not in predefined list"),
        @ApiResponse(responseCode = "404", description = "Instrument not found"),
        @ApiResponse(responseCode = "429", description = "API rate limit exceeded"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FinancialInstrument> getInstrumentBySymbol(
            @Parameter(
                description = "Stock symbol (e.g., AAPL, MSFT, GOOGL)", 
                example = "AAPL",
                required = true
            )
            @PathVariable String symbol) {
        
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/instruments/{} - Get instrument by symbol", symbol);
        logger.info("🎯 Fetching instrument data for symbol: {}", symbol);
        
        // Validate symbol format
        if (symbol == null || symbol.trim().isEmpty()) {
            logger.warn("❌ ENDPOINT ERROR: GET /api/v1/instruments/{} - Invalid symbol: empty or null", symbol);
            return ResponseEntity.badRequest().build();
        }
        
        // Normalize symbol (uppercase, trim)
        String normalizedSymbol = symbol.trim().toUpperCase();
        logger.debug("🔄 Normalized symbol from '{}' to '{}'", symbol, normalizedSymbol);
        
        try {
            // Verificar si el símbolo está en la lista predefinida
            boolean isInPredefinedList = com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols.SYMBOLS
                .contains(normalizedSymbol);
            if (!isInPredefinedList) {
                logger.warn("❌ ENDPOINT ERROR: GET /api/v1/instruments/{} - Symbol not in predefined list", normalizedSymbol);
                return ResponseEntity.badRequest().build();
            }

            Optional<FinancialInstrument> instrument = alphaVantageService.getInstrumentData(normalizedSymbol);
            
            if (instrument.isPresent()) {
                FinancialInstrument found = instrument.get();
                boolean hasRealData = found.getCurrentPrice().compareTo(java.math.BigDecimal.ZERO) > 0;
                
                logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/instruments/{} - Found instrument: name='{}', price={}, change={}, hasRealData={}", 
                    normalizedSymbol, found.getName(), found.getCurrentPrice(), found.getChange(), hasRealData);
                return ResponseEntity.ok(found);
            } else {
                logger.warn("❌ ENDPOINT NOT_FOUND: GET /api/v1/instruments/{} - No instrument found", normalizedSymbol);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("💥 ENDPOINT ERROR: GET /api/v1/instruments/{} - Exception: {}", normalizedSymbol, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 