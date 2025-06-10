package com.puente.financialservice.config;

import com.puente.financialservice.financialinstrument.domain.model.FinancialInstrument;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentExternalService;
import com.puente.financialservice.financialinstrument.infrastructure.dto.AlphaVantageResponse;
import com.puente.financialservice.financialinstrument.infrastructure.dto.GlobalQuote;
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
        return FinancialInstrument.builder()
            .symbol(symbol)
            .name(symbol) // En una implementación real, obtendríamos el nombre de otro endpoint
            .type("EQUITY")
            .region("US")
            .marketOpen("09:30")
            .marketClose("16:00")
            .timezone("America/New_York")
            .currency("USD")
            .matchScore(1.0)
            .currentPrice(new BigDecimal(quote.getPrice()))
            .previousClose(new BigDecimal(quote.getPreviousClose()))
            .change(new BigDecimal(quote.getChange()))
            .changePercent(new BigDecimal(quote.getChangePercent().replace("%", "")))
            .lastUpdated(LocalDateTime.now())
            .build();
    }
} 