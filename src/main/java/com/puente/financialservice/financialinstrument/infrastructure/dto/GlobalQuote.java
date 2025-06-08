package com.puente.financialservice.financialinstrument.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GlobalQuote {
    @JsonProperty("01. symbol")
    private String symbol;
    
    @JsonProperty("05. price")
    private String price;
    
    @JsonProperty("08. previous close")
    private String previousClose;
    
    @JsonProperty("09. change")
    private String change;
    
    @JsonProperty("10. change percent")
    private String changePercent;
} 