package com.io.usyc.Dto;
public record ConceptoCreateReq(
        String codigo,
        String nombre,
        String descripcion,
        String tipoMonto,  // CARRERA_INSCRIPCION | CARRERA_MENSUALIDAD | MONTO_MANUAL
        Boolean activo
) {}