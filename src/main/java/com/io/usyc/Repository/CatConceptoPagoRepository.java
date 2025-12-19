package com.io.usyc.Repository;

import com.io.usyc.Domain.CatConceptoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatConceptoPagoRepository extends JpaRepository<CatConceptoPago, Long> {
    Optional<CatConceptoPago> findByCodigoIgnoreCase(String codigo);
}   