package com.puente.financialservice.financialinstrument.infrastructure.persistence;

import com.puente.financialservice.financialinstrument.domain.FinancialInstrument;
import com.puente.financialservice.financialinstrument.domain.port.FinancialInstrumentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaFinancialInstrumentRepository extends JpaRepository<FinancialInstrument, String>, FinancialInstrumentRepository {
    // Los métodos básicos de CRUD son proporcionados por JpaRepository
    // Podemos agregar métodos personalizados aquí si es necesario
} 