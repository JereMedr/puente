package com.puente.financialservice.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        logger.info("🚀 Creating TaskExecutor for scheduler and async operations...");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("FinancialScheduler-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        logger.info("✅ TaskExecutor configured: core={}, max={}, queue={}, prefix='{}'",
            executor.getCorePoolSize(), executor.getMaxPoolSize(), 
            executor.getQueueCapacity(), executor.getThreadNamePrefix());
        
        return executor;
    }
} 