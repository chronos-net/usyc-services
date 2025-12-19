package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recibo")
@Data
public class Recibo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recibo_id")
    private Long id;

    @Column(name = "folio", nullable = false, unique = true, length = 40)
    private String folio;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_pago")
    private LocalDate fechaPago;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alumno_id", nullable = false)
    private Alumno alumno;

    @Column(name = "concepto", nullable = false, length = 180)
    private String concepto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 10)
    private String moneda = "MXN";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estatus_id", nullable = false)
    private CatEstatusRecibo estatus;

    @Column(name = "cancelado_en")
    private LocalDateTime canceladoEn;

    @Column(name = "motivo_cancelacion", length = 300)
    private String motivoCancelacion;

    @Column(name = "qr_payload", nullable = false, unique = true, length = 300)
    private String qrPayload;

    @Column(name = "qr_hash", nullable = false, length = 128)
    private String qrHash;

    @Column(name = "comentario", length = 300)
    private String comentario;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

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

