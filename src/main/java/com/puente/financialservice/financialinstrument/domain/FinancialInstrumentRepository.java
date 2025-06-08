package com.puente.financialservice.financialinstrument.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialInstrumentRepository extends JpaRepository<FinancialInstrument, String> {
} 