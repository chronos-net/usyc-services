package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cat_carrera")
@Data
public class CatCarrera {

    @Id
    @Column(name = "carrera_id", length = 2)
    private String id; // "01".."99"

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "escolaridad_id", nullable = false)
    private CatEscolaridad escolaridad;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "duracion_anios", nullable = false)
    private Integer duracionAnios = 0;

    @Column(name = "duracion_meses", nullable = false)
    private Integer duracionMeses = 0;

    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarreraConceptoConfig> conceptosConfig = new ArrayList<>();

    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<Alumno> alumnos = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }
}
