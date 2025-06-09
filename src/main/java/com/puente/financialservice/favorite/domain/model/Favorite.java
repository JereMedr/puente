package com.puente.financialservice.favorite.domain.model;

import com.puente.financialservice.user.domain.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    @EmbeddedId
    private FavoriteId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "symbol", insertable = false, updatable = false)
    private String symbol;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Constructor conveniente para crear favoritos
    public Favorite(User user, String symbol) {
        this.id = new FavoriteId(user.getId(), symbol);
        this.user = user;
        this.symbol = symbol;
    }
} 