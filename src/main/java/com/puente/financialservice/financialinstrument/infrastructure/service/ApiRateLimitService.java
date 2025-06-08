package com.puente.financialservice.financialinstrument.infrastructure.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApiRateLimitService {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiRateLimitService.class);
    
    private final int apiCallLimit;
    private final long apiCallInterval;
    private long lastApiCallTime = 0;
    private int apiCallsThisMinute = 0;

    public ApiRateLimitService(
            @Value("${app.alpha-vantage.rate-limit.calls-per-minute:5}") int apiCallLimit,
            @Value("${app.alpha-vantage.rate-limit.interval-ms:60000}") long apiCallInterval) {
        this.apiCallLimit = apiCallLimit;
        this.apiCallInterval = apiCallInterval;
    }

    public void checkApiLimits() throws InterruptedException {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastApiCallTime >= apiCallInterval) {
            resetCallCounter(currentTime);
        }
        
        if (apiCallsThisMinute >= apiCallLimit) {
            waitForNextInterval(currentTime);
        }
        
        apiCallsThisMinute++;
    }

    private void resetCallCounter(long currentTime) {
        apiCallsThisMinute = 0;
        lastApiCallTime = currentTime;
    }

    private void waitForNextInterval(long currentTime) throws InterruptedException {
        long waitTime = apiCallInterval - (currentTime - lastApiCallTime);
        logger.warn("API call limit reached. Waiting {} ms before next call", waitTime);
        
        Thread.sleep(waitTime);
        resetCallCounter(System.currentTimeMillis());
    }
} 