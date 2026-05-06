package com.io.usyc.Repository;

import com.io.usyc.Domain.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, String> {
    boolean existsByMatricula(String matricula);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Page<Alumno> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Page<Alumno> findByPlantel_Id(Integer plantelId, Pageable pageable);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Optional<Alumno> findWithRefsById(String id);
}
