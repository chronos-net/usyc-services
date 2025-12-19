package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CatConceptoPago;
import com.io.usyc.Dto.*;
import com.io.usyc.Repository.CatConceptoPagoRepository;
import com.io.usyc.Service.ConceptoPagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ConceptoPagoServiceImpl implements ConceptoPagoService {

    private final CatConceptoPagoRepository conceptoRepo;

    public ConceptoPagoServiceImpl(CatConceptoPagoRepository conceptoRepo) {
        this.conceptoRepo = conceptoRepo;
    }

    @Override
    public ConceptoRes crear(ConceptoCreateReq req) {
        validarTexto(req.codigo(), "codigo");
        validarTexto(req.nombre(), "nombre");
        validarTipoMonto(req.tipoMonto());

        String codigo = req.codigo().trim().toUpperCase();

        conceptoRepo.findByCodigoIgnoreCase(codigo).ifPresent(x -> {
            throw new IllegalArgumentException("Ya existe un concepto con código: " + codigo);
        });

        CatConceptoPago c = new CatConceptoPago();
        c.setCodigo(codigo);
        c.setNombre(req.nombre().trim());
        c.setDescripcion(req.descripcion() != null ? req.descripcion().trim() : null);
        c.setTipoMonto(req.tipoMonto().trim().toUpperCase());
        c.setActivo(req.activo() == null ? Boolean.TRUE : req.activo());

        return toRes(conceptoRepo.save(c));
    }

    @Override
    public ConceptoRes actualizar(Long conceptoId, ConceptoUpdateReq req) {
        if (conceptoId == null) throw new IllegalArgumentException("El campo 'conceptoId' es obligatorio.");

        CatConceptoPago c = conceptoRepo.findById(conceptoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe concepto con id: " + conceptoId));

        if (req.nombre() != null) {
            validarTexto(req.nombre(), "nombre");
            c.setNombre(req.nombre().trim());
        }
        if (req.descripcion() != null) {
            c.setDescripcion(req.descripcion().trim());
        }
        if (req.tipoMonto() != null) {
            validarTipoMonto(req.tipoMonto());
            c.setTipoMonto(req.tipoMonto().trim().toUpperCase());
        }
        if (req.activo() != null) {
            c.setActivo(req.activo());
        }

        return toRes(conceptoRepo.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public ConceptoRes obtener(Long conceptoId) {
        if (conceptoId == null) throw new IllegalArgumentException("El campo 'conceptoId' es obligatorio.");
        return conceptoRepo.findById(conceptoId).map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe concepto con id: " + conceptoId));
    }

    @Override
    @Transactional(readOnly = true)
    public ConceptoRes obtenerPorCodigo(String codigo) {
        validarTexto(codigo, "codigo");
        return conceptoRepo.findByCodigoIgnoreCase(codigo.trim().toUpperCase()).map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe concepto con código: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConceptoRes> listar(Boolean soloActivos) {
        boolean activos = soloActivos != null && soloActivos;
        return conceptoRepo.findAll().stream()
                .filter(c -> !activos || Boolean.TRUE.equals(c.getActivo()))
                .map(this::toRes)
                .toList();
    }

    @Override
    public void activar(Long conceptoId) {
        CatConceptoPago c = conceptoRepo.findById(conceptoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe concepto con id: " + conceptoId));
        c.setActivo(true);
        conceptoRepo.save(c);
    }

    @Override
    public void desactivar(Long conceptoId) {
        CatConceptoPago c = conceptoRepo.findById(conceptoId)
                .orElseThrow(() -> new IllegalArgumentException("No existe concepto con id: " + conceptoId));
        c.setActivo(false);
        conceptoRepo.save(c);
    }

    private ConceptoRes toRes(CatConceptoPago c) {
        return new ConceptoRes(
                c.getId(),
                c.getCodigo(),
                c.getNombre(),
                c.getDescripcion(),
                c.getTipoMonto(),
                c.getActivo()
        );
    }

    private static void validarTipoMonto(String v) {
        validarTexto(v, "tipoMonto");
        String t = v.trim().toUpperCase();
        if (!t.equals("CARRERA_INSCRIPCION") && !t.equals("CARRERA_MENSUALIDAD") && !t.equals("MONTO_MANUAL")) {
            throw new IllegalArgumentException("tipoMonto inválido. Usa: CARRERA_INSCRIPCION, CARRERA_MENSUALIDAD o MONTO_MANUAL.");
        }
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}