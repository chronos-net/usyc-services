package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @Column(name = "monto_mensual", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoMensual;

    @Column(name = "monto_inscripcion", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoInscripcion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "carrera", fetch = FetchType.LAZY)
    private List<Alumno> alumnos = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }

    // getters/setters
}
