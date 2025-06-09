package com.puente.financialservice.favorite.infrastructure.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a favorite financial instrument for a user")
public class FavoriteDTO {
    @Schema(
        description = "The stock symbol of the favorite instrument",
        example = "AAPL",
        required = true
    )
    private String symbol;

    @Schema(
        description = "The timestamp when the instrument was added to favorites",
        example = "2024-03-19T10:30:00",
        required = true
    )
    private LocalDateTime createdAt;
} 