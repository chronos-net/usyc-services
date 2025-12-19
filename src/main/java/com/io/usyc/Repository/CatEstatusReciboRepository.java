package com.io.usyc.Repository;

import com.io.usyc.Domain.CatEstatusRecibo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CatEstatusReciboRepository extends JpaRepository<CatEstatusRecibo, Long> {
    java.util.Optional<CatEstatusRecibo> findByCodigo(String codigo);
}
