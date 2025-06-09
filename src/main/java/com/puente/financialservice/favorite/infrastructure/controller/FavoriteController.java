package com.puente.financialservice.favorite.infrastructure.controller;

import com.puente.financialservice.favorite.application.service.FavoriteService;
import com.puente.financialservice.favorite.domain.model.Favorite;
import com.puente.financialservice.favorite.infrastructure.dto.FavoriteDTO;
import com.puente.financialservice.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/favorites")
@Tag(name = "Favorites", description = "Endpoints for managing favorite financial instruments. Limited to predefined symbols with rate limits of 25 API calls per day.")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{symbol}")
    @Operation(
        summary = "Add a financial instrument to favorites",
        description = "Adds a financial instrument to the user's favorites list."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Instrument added to favorites successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FavoriteDTO.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid symbol or already in favorites",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT token is missing or invalid",
        content = @Content
    )
    public ResponseEntity<FavoriteDTO> addFavorite(
            @AuthenticationPrincipal User user,
            @Parameter(
                description = "Stock symbol to add to favorites (e.g., AAPL, MSFT, GOOGL). Must be from predefined list.",
                example = "AAPL",
                required = true
            )
            @PathVariable String symbol) {
        Favorite favorite = favoriteService.addFavorite(user, symbol);
        return ResponseEntity.ok(toDTO(favorite));
    }

    @DeleteMapping("/{symbol}")
    @Operation(
        summary = "Remove a financial instrument from favorites",
        description = "Removes the specified financial instrument from the user's favorites list."
    )
    @ApiResponse(
        responseCode = "200",
        description = "Instrument removed from favorites successfully"
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT token is missing or invalid",
        content = @Content
    )
    public ResponseEntity<Void> removeFavorite(
            @AuthenticationPrincipal User user,
            @Parameter(
                description = "Stock symbol to remove from favorites",
                example = "AAPL",
                required = true
            )
            @PathVariable String symbol) {
        favoriteService.removeFavorite(user, symbol);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(
        summary = "Get user's favorite financial instruments",
        description = "Retrieves the list of financial instruments that the user has marked as favorites."
    )
    @ApiResponse(
        responseCode = "200",
        description = "List of favorite instruments retrieved successfully",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = FavoriteDTO.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Unauthorized - JWT token is missing or invalid",
        content = @Content
    )
    public ResponseEntity<List<FavoriteDTO>> getFavorites(@AuthenticationPrincipal User user) {
        List<Favorite> favorites = favoriteService.getUserFavorites(user);
        List<FavoriteDTO> favoriteDTOs = favorites.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(favoriteDTOs);
    }

    private FavoriteDTO toDTO(Favorite favorite) {
        return new FavoriteDTO(
                favorite.getSymbol(),
                favorite.getCreatedAt()
        );
    }
}

// Error response schema for Swagger documentation
@Schema(description = "Error response")
class ErrorResponse {
    @Schema(description = "Error message", example = "Invalid financial instrument symbol - not in predefined list")
    private String error;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
} 