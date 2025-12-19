package com.io.usyc.Repository;

import com.io.usyc.Domain.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlumnoRepository extends JpaRepository<Alumno, String> {
    boolean existsByMatricula(String matricula);
}
