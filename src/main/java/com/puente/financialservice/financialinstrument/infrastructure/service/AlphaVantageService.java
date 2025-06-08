package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import jakarta.annotation.PostConstruct;

@Service
public class AlphaVantageService {
    private static final Logger logger = LoggerFactory.getLogger(AlphaVantageService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;
    private static final String DEMO_API_KEY = "demo";
    private static final int API_CALL_LIMIT = 5; // Límite de llamadas por minuto para la API demo
    private static final long API_CALL_INTERVAL = 60000; // 1 minuto en milisegundos
    private long lastApiCallTime = 0;
    private int apiCallsThisMinute = 0;
    private LocalDateTime lastUpdateTime;
    
    // Cache en memoria para datos de instrumentos
    private final ConcurrentHashMap<String, FinancialInstrument> instrumentCache = new ConcurrentHashMap<>();

    public AlphaVantageService(
            RestTemplate restTemplate,
            @Value("${app.alpha-vantage.api-key}") String apiKey,
            @Value("${app.alpha-vantage.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.lastUpdateTime = LocalDateTime.now();
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing AlphaVantageService - Starting first data load");
        updateAllInstruments();
    }

    @Cacheable(value = "financialInstruments", key = "#symbol")
    public Optional<FinancialInstrument> getInstrumentData(String symbol) {
        // Primero intentar obtener del caché en memoria
        FinancialInstrument cached = instrumentCache.get(symbol);
        if (cached != null) {
            return Optional.of(cached);
        }

        try {
            checkApiLimits();
            
            // Usar el endpoint demo para pruebas
            String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    baseUrl, symbol, DEMO_API_KEY);
            
            logger.info("Fetching data for symbol: {}", symbol);
            AlphaVantageResponse response = restTemplate.getForObject(url, AlphaVantageResponse.class);
            
            if (response != null && response.getGlobalQuote() != null) {
                FinancialInstrument instrument = mapToFinancialInstrument(symbol, response.getGlobalQuote());
                // Actualizar caché en memoria
                instrumentCache.put(symbol, instrument);
                logger.info("Successfully fetched data for symbol: {}", symbol);
                return Optional.of(instrument);
            }
            
            logger.warn("No data found for symbol: {}", symbol);
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                logger.error("API rate limit exceeded for symbol {}: {}", symbol, e.getMessage());
            } else {
                logger.error("HTTP error fetching data for symbol {}: {}", symbol, e.getMessage());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error fetching data for symbol {}: {}", symbol, e.getMessage());
            return Optional.empty();
        }
    }

    public List<FinancialInstrument> getAllPredefinedInstruments() {
        logger.info("Getting all predefined instruments from cache. Last update: {}", 
            lastUpdateTime.format(formatter));
        return PredefinedSymbols.SYMBOLS.stream()
                .map(symbol -> instrumentCache.getOrDefault(symbol, 
                    new FinancialInstrument(symbol, getCompanyName(symbol), 
                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 
                        BigDecimal.ZERO, LocalDateTime.now())))
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 300000) // 5 minutos
    public void updateAllInstruments() {
        LocalDateTime startTime = LocalDateTime.now();
        logger.info("Starting scheduled update of all instruments at {}", startTime.format(formatter));
        
        new Thread(() -> {
            int updatedCount = 0;
            for (String symbol : PredefinedSymbols.SYMBOLS) {
                try {
                    Optional<FinancialInstrument> instrument = getInstrumentData(symbol);
                    if (instrument.isPresent()) {
                        instrumentCache.put(symbol, instrument.get());
                        updatedCount++;
                        logger.info("Updated data for symbol: {} - Price: {}", 
                            symbol, instrument.get().getCurrentPrice());
                    }
                } catch (Exception e) {
                    logger.error("Error updating symbol {}: {}", symbol, e.getMessage());
                }
            }
            lastUpdateTime = LocalDateTime.now();
            logger.info("Completed scheduled update. Updated {} instruments. Next update in 5 minutes at {}", 
                updatedCount, lastUpdateTime.plusMinutes(5).format(formatter));
        }).start();
    }

    @CacheEvict(value = "financialInstruments", allEntries = true)
    public void clearCache() {
        instrumentCache.clear();
        logger.info("Cache cleared at {}", LocalDateTime.now().format(formatter));
    }

    private void checkApiLimits() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastApiCallTime >= API_CALL_INTERVAL) {
            // Reset contador si ha pasado un minuto
            apiCallsThisMinute = 0;
            lastApiCallTime = currentTime;
        }
        
        if (apiCallsThisMinute >= API_CALL_LIMIT) {
            long waitTime = API_CALL_INTERVAL - (currentTime - lastApiCallTime);
            logger.warn("API call limit reached. Waiting {} ms before next call", waitTime);
            try {
                Thread.sleep(waitTime);
                apiCallsThisMinute = 0;
                lastApiCallTime = System.currentTimeMillis();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("API call limit wait interrupted", e);
            }
        }
        
        apiCallsThisMinute++;
    }

    private FinancialInstrument mapToFinancialInstrument(String symbol, GlobalQuote quote) {
        try {
            LocalDateTime now = LocalDateTime.now();
            return new FinancialInstrument(
                symbol,
                getCompanyName(symbol),
                new BigDecimal(quote.getPrice()),
                new BigDecimal(quote.getPreviousClose()),
                new BigDecimal(quote.getChange()),
                new BigDecimal(quote.getChangePercent().replace("%", "")),
                now
            );
        } catch (NumberFormatException e) {
            logger.error("Error parsing numeric values for symbol {}: {}", symbol, e.getMessage());
            throw new RuntimeException("Error parsing instrument data", e);
        }
    }

    private String getCompanyName(String symbol) {
        return switch (symbol) {
            case "AAPL" -> "Apple Inc";
            case "MSFT" -> "Microsoft Corporation";
            case "GOOGL" -> "Alphabet Inc";
            case "AMZN" -> "Amazon.com Inc";
            case "META" -> "Meta Platforms Inc";
            case "TSLA" -> "Tesla Inc";
            case "JPM" -> "JPMorgan Chase & Co";
            case "V" -> "Visa Inc";
            case "PG" -> "Procter & Gamble Co";
            case "JNJ" -> "Johnson & Johnson";
            case "WMT" -> "Walmart Inc";
            case "BAC" -> "Bank of America Corp";
            case "KO" -> "Coca-Cola Co";
            case "DIS" -> "Walt Disney Co";
            case "NFLX" -> "Netflix Inc";
            case "INTC" -> "Intel Corporation";
            case "VZ" -> "Verizon Communications Inc";
            case "T" -> "AT&T Inc";
            case "PFE" -> "Pfizer Inc";
            case "MRK" -> "Merck & Co Inc";
            default -> symbol;
        };
    }

    // Clases internas para mapear la respuesta de Alpha Vantage
    private static class AlphaVantageResponse {
        private GlobalQuote globalQuote;

        public GlobalQuote getGlobalQuote() {
            return globalQuote;
        }

        public void setGlobalQuote(GlobalQuote globalQuote) {
            this.globalQuote = globalQuote;
        }
    }

    private static class GlobalQuote {
        private String symbol;
        private String price;
        private String previousClose;
        private String change;
        private String changePercent;

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getPreviousClose() {
            return previousClose;
        }

        public void setPreviousClose(String previousClose) {
            this.previousClose = previousClose;
        }

        public String getChange() {
            return change;
        }

        public void setChange(String change) {
            this.change = change;
        }

        public String getChangePercent() {
            return changePercent;
        }

        public void setChangePercent(String changePercent) {
            this.changePercent = changePercent;
        }
    }
} 