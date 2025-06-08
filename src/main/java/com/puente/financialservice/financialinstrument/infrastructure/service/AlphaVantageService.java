package com.puente.financialservice.financialinstrument.infrastructure.service;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlphaVantageService {
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public AlphaVantageService(
            RestTemplate restTemplate,
            @Value("${app.alpha-vantage.api-key}") String apiKey,
            @Value("${app.alpha-vantage.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Cacheable(value = "financialInstruments", key = "#symbol")
    public Optional<FinancialInstrument> getInstrumentData(String symbol) {
        try {
            String url = String.format("%s/query?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    baseUrl, symbol, apiKey);
            
            AlphaVantageResponse response = restTemplate.getForObject(url, AlphaVantageResponse.class);
            
            if (response != null && response.getGlobalQuote() != null) {
                return Optional.of(mapToFinancialInstrument(symbol, response.getGlobalQuote()));
            }
            
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<FinancialInstrument> getAllPredefinedInstruments() {
        return PredefinedSymbols.SYMBOLS.stream()
                .map(this::getInstrumentData)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 300000) // 5 minutos
    public void updateAllInstruments() {
        PredefinedSymbols.SYMBOLS.forEach(this::getInstrumentData);
    }

    private FinancialInstrument mapToFinancialInstrument(String symbol, GlobalQuote quote) {
        return new FinancialInstrument(
            symbol,
            symbol, // En una implementación real, obtendríamos el nombre de otro endpoint
            new BigDecimal(quote.getPrice()),
            new BigDecimal(quote.getPreviousClose()),
            new BigDecimal(quote.getChange()),
            new BigDecimal(quote.getChangePercent().replace("%", "")),
            LocalDateTime.now()
        );
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