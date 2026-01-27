package com.io.usyc.Repository;

import com.io.usyc.Domain.CatCarrera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CatCarreraRepository extends JpaRepository<CatCarrera, String> {

    List    <CatCarrera> findByActivoTrue();

    List<CatCarrera> findByEscolaridad_Id(Long escolaridadId);

    List<CatCarrera> findByEscolaridad_IdAndActivoTrue(Long escolaridadId);

}

