package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "alumno")
@Data
public class Alumno {

    @Id
    @Column(name = "alumno_id", length = 9)
    private String id; // "202501001"

    @Column(name = "nombre_completo", nullable = false, length = 180)
    private String nombreCompleto;

    @Column(name = "matricula", unique = true, length = 60)
    private String matricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escolaridad_id", nullable = false)
    private CatEscolaridad escolaridad;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", nullable = false)
    private CatCarrera carrera;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    @Column(name = "fecha_termino")
    private LocalDate fechaTermino;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plantel_id", nullable = false)
    private CatPlantel plantel;


    @PrePersist
    void prePersist() {
        var now = LocalDateTime.now();
        if (creadoEn == null) creadoEn = now;
        if (actualizadoEn == null) actualizadoEn = now;
    }

    @PreUpdate
    void preUpdate() {
        actualizadoEn = LocalDateTime.now();
    }

    // getters/setters
}
