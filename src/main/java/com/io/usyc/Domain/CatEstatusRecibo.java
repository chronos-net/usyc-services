package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cat_estatus_recibo")
@Data
public class CatEstatusRecibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "estatus_id")
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo; // EMITIDO, PAGADO, CANCELADO

    @Column(name = "nombre", nullable = false, length = 80)
    private String nombre;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "estatus", fetch = FetchType.LAZY)
    private List<Recibo> recibos = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (creadoEn == null) creadoEn = LocalDateTime.now();
    }

    // getters/setters
}
