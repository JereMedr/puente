package com.puente.financialservice.config;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentExternalService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.cache.annotation.Cacheable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AlphaVantageFinancialInstrumentAdapter implements FinancialInstrumentExternalService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String baseUrl;

    public AlphaVantageFinancialInstrumentAdapter(
            RestTemplate restTemplate,
            @Value("${app.alpha-vantage.api-key}") String apiKey,
            @Value("${app.alpha-vantage.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    @Override
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
            // En un entorno de producción, deberíamos manejar esto de manera más robusta
            return Optional.empty();
        }
    }

    @Override
    public void updateInstrumentData(FinancialInstrument instrument) {
        // En una implementación real, podríamos actualizar datos en Alpha Vantage
        // Pero para este ejemplo, solo implementamos la lectura
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