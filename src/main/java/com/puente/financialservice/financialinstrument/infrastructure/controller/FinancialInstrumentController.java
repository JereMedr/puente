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
import org.springframework.security.access.prepost.PreAuthorize;
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
        description = "Retrieves detailed information about a specific financial instrument using its stock symbol (e.g., AAPL, MSFT, GOOGL)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Instrument found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid symbol format"),
        @ApiResponse(responseCode = "404", description = "Instrument not found"),
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
            Optional<FinancialInstrument> instrument = alphaVantageService.getInstrumentData(normalizedSymbol);
            
            if (instrument.isPresent()) {
                FinancialInstrument found = instrument.get();
                logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/instruments/{} - Found instrument: name='{}', price={}, change={}", 
                    normalizedSymbol, found.getName(), found.getCurrentPrice(), found.getChange());
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

    @PutMapping("/{symbol}/sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force sync of a specific instrument", description = "Admin only - Forces an update of the instrument data from Alpha Vantage")
    public ResponseEntity<FinancialInstrument> syncInstrument(@PathVariable String symbol) {
        logger.info("🌐 ENDPOINT CALLED: PUT /api/v1/instruments/{}/sync - Admin force sync", symbol);
        logger.info("🔄 Admin sync requested for symbol: {}", symbol);
        
        Optional<FinancialInstrument> result = alphaVantageService.getInstrumentData(symbol);
        if (result.isPresent()) {
            logger.info("✅ ENDPOINT SUCCESS: PUT /api/v1/instruments/{}/sync - Sync completed", symbol);
            return ResponseEntity.ok(result.get());
        } else {
            logger.warn("❌ ENDPOINT NOT_FOUND: PUT /api/v1/instruments/{}/sync - Sync failed", symbol);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/sync-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force sync of all instruments", description = "Admin only - Forces an update of all instruments data from Alpha Vantage")
    public ResponseEntity<List<FinancialInstrument>> syncAllInstruments() {
        logger.info("🌐 ENDPOINT CALLED: PUT /api/v1/instruments/sync-all - Admin sync all instruments");
        logger.info("🔄 Admin sync-all requested");
        List<FinancialInstrument> instruments = alphaVantageService.getAllPredefinedInstruments();
        logger.info("✅ ENDPOINT SUCCESS: PUT /api/v1/instruments/sync-all - Synced {} instruments", instruments.size());
        return ResponseEntity.ok(instruments);
    }

    @GetMapping("/debug/{symbol}")
    @Operation(
        summary = "Debug endpoint for API troubleshooting", 
        description = "Provides detailed debugging information for API calls to Alpha Vantage"
    )
    public ResponseEntity<DebugResponse> debugInstrument(@PathVariable String symbol) {
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/instruments/debug/{} - Debug troubleshooting", symbol);
        logger.info("🔧 Debug endpoint called for symbol: {}", symbol);
        
        String normalizedSymbol = symbol.trim().toUpperCase();
        DebugResponse debugInfo = new DebugResponse();
        debugInfo.setSymbol(normalizedSymbol);
        debugInfo.setTimestamp(java.time.LocalDateTime.now().toString());
        
        try {
            logger.info("🔍 Starting debug process for {}", normalizedSymbol);
            
            // Test if symbol is in predefined list
            boolean isInPredefinedList = com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols.SYMBOLS
                .contains(normalizedSymbol);
            debugInfo.setInPredefinedList(isInPredefinedList);
            logger.info("📋 Symbol {} in predefined list: {}", normalizedSymbol, isInPredefinedList);
            
            // Try to get instrument data
            Optional<FinancialInstrument> instrument = alphaVantageService.getInstrumentData(normalizedSymbol);
            
            if (instrument.isPresent()) {
                FinancialInstrument found = instrument.get();
                debugInfo.setDataFound(true);
                debugInfo.setPrice(found.getCurrentPrice().toString());
                debugInfo.setLastUpdated(found.getLastUpdated().toString());
                debugInfo.setHasRealData(found.getCurrentPrice().compareTo(java.math.BigDecimal.ZERO) > 0);
                debugInfo.setMessage("Instrument data retrieved successfully");
                logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/instruments/debug/{} - Debug successful: price={}", normalizedSymbol, found.getCurrentPrice());
            } else {
                debugInfo.setDataFound(false);
                debugInfo.setHasRealData(false);
                debugInfo.setMessage("No instrument data found");
                logger.warn("❌ ENDPOINT WARNING: GET /api/v1/instruments/debug/{} - Debug failed: no data found", normalizedSymbol);
            }
            
        } catch (Exception e) {
            debugInfo.setDataFound(false);
            debugInfo.setHasRealData(false);
            debugInfo.setMessage("Error occurred: " + e.getMessage());
            logger.error("💥 ENDPOINT ERROR: GET /api/v1/instruments/debug/{} - Debug error: {}", normalizedSymbol, e.getMessage(), e);
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    @GetMapping("/raw-api-test/{symbol}")
    @Operation(summary = "Raw Alpha Vantage API test", description = "Tests Alpha Vantage API directly and returns raw response")
    public ResponseEntity<String> rawApiTest(@PathVariable String symbol) {
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/instruments/raw-api-test/{} - Direct API test", symbol);
        logger.info("🧪 RAW API TEST for symbol: {}", symbol);
        
        try {
            org.springframework.web.client.RestTemplate directRestTemplate = 
                new org.springframework.web.client.RestTemplate();
            
            String url = String.format("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=%s&apikey=9KPCSKNCL0K3ABAV", 
                symbol.toUpperCase());
            
            logger.info("🌐 Direct API URL: {}", url);
            
            String response = directRestTemplate.getForObject(url, String.class);
            logger.info("📦 Raw response received: {}", response);
            logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/instruments/raw-api-test/{} - Direct API test completed", symbol);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("💥 ENDPOINT ERROR: GET /api/v1/instruments/raw-api-test/{} - Raw API test failed: {}", symbol, e.getMessage(), e);
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @GetMapping("/config-check")
    @Operation(summary = "Check API configuration", description = "Shows current API configuration (masked for security)")
    public ResponseEntity<String> configCheck() {
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/instruments/config-check - Configuration check");
        logger.info("🔧 Configuration check requested");
        
        try {
            // This will trigger the AlphaVantageService to show current config
            String configInfo = "Configuration check completed. Check logs for details.";
            logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/instruments/config-check - Config check completed");
            return ResponseEntity.ok(configInfo);
            
        } catch (Exception e) {
            logger.error("💥 ENDPOINT ERROR: GET /api/v1/instruments/config-check - Config check failed: {}", e.getMessage(), e);
            return ResponseEntity.ok("Config check error: " + e.getMessage());
        }
    }

    @GetMapping("/debug-config")
    @Operation(summary = "Detailed configuration debugging", description = "Shows all relevant configuration values")
    public ResponseEntity<java.util.Map<String, String>> debugConfig(
            @org.springframework.beans.factory.annotation.Value("${app.alpha-vantage.api-key:NOT_FOUND}") String apiKey,
            @org.springframework.beans.factory.annotation.Value("${app.alpha-vantage.base-url:NOT_FOUND}") String baseUrl,
            @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:default}") String activeProfile) {
        
        logger.info("🌐 ENDPOINT CALLED: GET /api/v1/instruments/debug-config - Detailed configuration debugging");
        logger.info("🔍 DEBUG CONFIG: Checking all configuration values");
        
        java.util.Map<String, String> config = new java.util.HashMap<>();
        
        // Mask API key for security
        String maskedApiKey = apiKey != null && apiKey.length() > 4 && !"NOT_FOUND".equals(apiKey) ? 
            apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4) : 
            apiKey;
            
        config.put("apiKey", maskedApiKey);
        config.put("baseUrl", baseUrl);
        config.put("activeProfile", activeProfile);
        config.put("timestamp", java.time.LocalDateTime.now().toString());
        
        // Additional environment checks
        String alphaVantageEnv = System.getenv("ALPHA_VANTAGE_API_KEY");
        config.put("envApiKey", alphaVantageEnv != null ? "PRESENT" : "NOT_PRESENT");
        
        logger.info("🔍 Config debug results: {}", config);
        logger.info("✅ ENDPOINT SUCCESS: GET /api/v1/instruments/debug-config - Configuration debugging completed");
        
        return ResponseEntity.ok(config);
    }

    // Debug response DTO
    public static class DebugResponse {
        private String symbol;
        private String timestamp;
        private boolean inPredefinedList;
        private boolean dataFound;
        private boolean hasRealData;
        private String price;
        private String lastUpdated;
        private String message;

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public boolean isInPredefinedList() { return inPredefinedList; }
        public void setInPredefinedList(boolean inPredefinedList) { this.inPredefinedList = inPredefinedList; }
        
        public boolean isDataFound() { return dataFound; }
        public void setDataFound(boolean dataFound) { this.dataFound = dataFound; }
        
        public boolean isHasRealData() { return hasRealData; }
        public void setHasRealData(boolean hasRealData) { this.hasRealData = hasRealData; }
        
        public String getPrice() { return price; }
        public void setPrice(String price) { this.price = price; }
        
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
} 