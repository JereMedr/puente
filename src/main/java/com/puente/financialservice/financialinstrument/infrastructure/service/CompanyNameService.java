package com.puente.financialservice.financialinstrument.infrastructure.service;

import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class CompanyNameService {
    
    private static final Map<String, String> COMPANY_NAMES = Map.ofEntries(
        Map.entry("AAPL", "Apple Inc"),
        Map.entry("MSFT", "Microsoft Corporation"),
        Map.entry("GOOGL", "Alphabet Inc"),
        Map.entry("AMZN", "Amazon.com Inc"),
        Map.entry("META", "Meta Platforms Inc"),
        Map.entry("TSLA", "Tesla Inc"),
        Map.entry("JPM", "JPMorgan Chase & Co"),
        Map.entry("V", "Visa Inc"),
        Map.entry("PG", "Procter & Gamble Co"),
        Map.entry("JNJ", "Johnson & Johnson"),
        Map.entry("WMT", "Walmart Inc"),
        Map.entry("BAC", "Bank of America Corp"),
        Map.entry("KO", "Coca-Cola Co"),
        Map.entry("DIS", "Walt Disney Co"),
        Map.entry("NFLX", "Netflix Inc"),
        Map.entry("INTC", "Intel Corporation"),
        Map.entry("VZ", "Verizon Communications Inc"),
        Map.entry("T", "AT&T Inc"),
        Map.entry("PFE", "Pfizer Inc"),
        Map.entry("MRK", "Merck & Co Inc")
    );

    public String getCompanyName(String symbol) {
        return COMPANY_NAMES.getOrDefault(symbol, symbol);
    }
} 