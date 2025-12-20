package com.io.usyc.Repository;

import com.io.usyc.Domain.CatTipoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatTipoPagoRepository extends JpaRepository<CatTipoPago, Integer> {
    Optional<CatTipoPago> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<CatTipoPago> findAllByActiveTrueOrderByNameAsc();
    List<CatTipoPago> findAllByOrderByNameAsc();
}