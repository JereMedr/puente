package com.puente.financialservice.user.infrastructure.persistence;

import com.puente.financialservice.user.domain.model.User;
import com.puente.financialservice.user.domain.port.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, UserRepository {
    @Override
    Optional<User> findByEmail(String email);
    
    @Override
    boolean existsByEmail(String email);
    
    @Override
    List<User> findAll();
    
    @Override
    void deleteById(Long id);
} 