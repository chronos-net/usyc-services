package com.io.usyc.Repository;

import com.io.usyc.Domain.CatEscolaridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CatEscolaridadRepository extends JpaRepository<CatEscolaridad, Long> {
    Optional<CatEscolaridad> findByCodigoIgnoreCase(String codigo);
}
