package com.puente.financialservice.financialinstrument.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class PredefinedSymbols {
    public static final List<String> SYMBOLS = Arrays.asList(
 "AAPL", // Apple Inc
        "MSFT", // Microsoft
        "GOOGL", // Alphabet Inc
        "AMZN", // Amazon
        "META", // Meta Platforms
        "TSLA", // Tesla
        "JPM", // JPMorgan Chase
        "V", // Visa
        "PG", // Procter & Gamble
        "JNJ", // Johnson & Johnson
        "WMT", // Walmart
        "BAC", // Bank of America
        "KO", // Coca-Cola
        "DIS", // Walt Disney
        "NFLX", // Netflix
        "INTC", // Intel
        "VZ", // Verizon
        "T", // AT&T
        "PFE", // Pfizer
        "MRK"  // Merck & Co
    );
} 