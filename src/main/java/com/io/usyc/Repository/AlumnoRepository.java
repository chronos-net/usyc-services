package com.io.usyc.Repository;

import com.io.usyc.Domain.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, String>, JpaSpecificationExecutor<Alumno> {
    boolean existsByMatricula(String matricula);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Page<Alumno> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Page<Alumno> findByPlantel_Id(Integer plantelId, Pageable pageable);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Page<Alumno> findAll(Specification<Alumno> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"escolaridad", "carrera", "plantel"})
    Optional<Alumno> findWithRefsById(String id);
}
