package com.puente.financialservice.user.infrastructure.persistence;

import com.puente.financialservice.user.domain.model.User;
import com.puente.financialservice.user.domain.port.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
    // Los métodos básicos de CRUD son proporcionados por JpaRepository
    // Podemos agregar métodos personalizados aquí si es necesario
} 