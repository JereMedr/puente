package com.puente.financialservice.favorite.infrastructure.persistence;

import com.puente.financialservice.favorite.domain.model.Favorite;
import com.puente.financialservice.favorite.domain.model.FavoriteId;
import com.puente.financialservice.favorite.domain.port.FavoriteRepository;
import com.puente.financialservice.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaFavoriteRepository extends JpaRepository<Favorite, FavoriteId>, FavoriteRepository {
} 