package com.puente.financialservice.financialinstrument.infrastructure.exception;

/**
 * Excepción personalizada para manejar errores relacionados con Alpha Vantage API.
 */
public class AlphaVantageException extends RuntimeException {
    
    public AlphaVantageException(String message) {
        super(message);
    }
    
    public AlphaVantageException(String message, Throwable cause) {
        super(message, cause);
    }
} 