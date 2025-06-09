package com.puente.financialservice.favorite.application.service;

import com.puente.financialservice.favorite.domain.model.Favorite;
import com.puente.financialservice.favorite.domain.model.FavoriteId;
import com.puente.financialservice.favorite.domain.port.FavoriteRepository;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentRepository;
import com.puente.financialservice.financialinstrument.infrastructure.config.PredefinedSymbols;
import com.puente.financialservice.user.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FavoriteService {
    private static final Logger logger = LoggerFactory.getLogger(FavoriteService.class);
    private final FavoriteRepository favoriteRepository;
    private final FinancialInstrumentRepository financialInstrumentRepository;

    public FavoriteService(FavoriteRepository favoriteRepository,
                         FinancialInstrumentRepository financialInstrumentRepository) {
        this.favoriteRepository = favoriteRepository;
        this.financialInstrumentRepository = financialInstrumentRepository;
    }

    @Transactional
    public Favorite addFavorite(User user, String symbol) {
        String normalizedSymbol = symbol.toUpperCase().trim();
        
        if (favoriteRepository.existsByUserAndSymbol(user, normalizedSymbol)) {
            logger.warn("User {} attempted to add already favorited symbol: {}", user.getEmail(), normalizedSymbol);
            throw new IllegalStateException("Symbol is already in favorites");
        }

        // Verificar si el símbolo está en la lista de predefinidos
        if (!PredefinedSymbols.SYMBOLS.contains(normalizedSymbol)) {
            logger.error("User {} attempted to add invalid symbol: {}", user.getEmail(), normalizedSymbol);
            throw new IllegalArgumentException("Invalid financial instrument symbol - not in predefined list");
        }

        // Verificar si existe en la base de datos (opcional)
        boolean existsInDb = financialInstrumentRepository.findBySymbol(normalizedSymbol).isPresent();
        if (!existsInDb) {
            logger.info("Symbol {} not yet in database but is valid. Will be loaded by scheduler.", normalizedSymbol);
        }

        logger.info("Adding favorite {} for user {}", normalizedSymbol, user.getEmail());
        Favorite favorite = new Favorite(user, normalizedSymbol);
        return favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(User user, String symbol) {
        String normalizedSymbol = symbol.toUpperCase().trim();
        FavoriteId favoriteId = new FavoriteId(user.getId(), normalizedSymbol);
        favoriteRepository.findById(favoriteId)
                .ifPresent(favorite -> {
                    logger.info("Removing favorite {} for user {}", normalizedSymbol, user.getEmail());
                    favoriteRepository.delete(favorite);
                });
    }

    @Transactional(readOnly = true)
    public List<Favorite> getUserFavorites(User user) {
        logger.debug("Getting favorites for user {}", user.getEmail());
        return favoriteRepository.findAllByUser(user);
    }
} 