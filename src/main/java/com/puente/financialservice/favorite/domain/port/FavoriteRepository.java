package com.puente.financialservice.favorite.domain.port;

import com.puente.financialservice.favorite.domain.model.Favorite;
import com.puente.financialservice.favorite.domain.model.FavoriteId;
import com.puente.financialservice.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository {
    Favorite save(Favorite favorite);
    void delete(Favorite favorite);
    Optional<Favorite> findById(FavoriteId id);
    List<Favorite> findAllByUser(User user);
    boolean existsByUserAndSymbol(User user, String symbol);
} 