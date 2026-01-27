package com.io.usyc.Repository;

import com.io.usyc.Domain.CarreraConceptoConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CarreraConceptoConfigRepository extends JpaRepository<CarreraConceptoConfig, Long> {
    List<CarreraConceptoConfig> findByCarrera_Id(String carreraId);
    boolean existsByCarrera_IdAndConcepto_Id(String carreraId, Long conceptoId);

    List<CarreraConceptoConfig> findByCarrera_IdIn(List<String> carreraIds);

}
