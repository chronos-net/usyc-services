package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CatEscolaridad;
import com.io.usyc.Dto.EscolaridadCreateReq;
import com.io.usyc.Dto.EscolaridadRes;
import com.io.usyc.Dto.EscolaridadUpdateReq;
import com.io.usyc.Repository.CatEscolaridadRepository;
import com.io.usyc.Service.EscolaridadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EscolaridadServiceImpl implements EscolaridadService {

    private final CatEscolaridadRepository escolaridadRepo;

    public EscolaridadServiceImpl(CatEscolaridadRepository escolaridadRepo) {
        this.escolaridadRepo = escolaridadRepo;
    }

    @Override
    public EscolaridadRes crear(EscolaridadCreateReq req) {
        validarTexto(req.codigo(), "codigo");
        validarTexto(req.nombre(), "nombre");

        String codigo = req.codigo().trim().toUpperCase();

        escolaridadRepo.findAll().stream()
                .filter(e -> e.getCodigo().equalsIgnoreCase(codigo))
                .findAny()
                .ifPresent(e -> { throw new IllegalArgumentException("Ya existe una escolaridad con código: " + codigo); });

        CatEscolaridad e = new CatEscolaridad();
        e.setCodigo(codigo);
        e.setNombre(req.nombre().trim());
        e.setActivo(req.activo() == null ? Boolean.TRUE : req.activo());

        CatEscolaridad saved = escolaridadRepo.save(e);
        return toRes(saved);
    }

    @Override
    public EscolaridadRes actualizar(Long id, EscolaridadUpdateReq req) {
        CatEscolaridad e = escolaridadRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + id));

        if (req.nombre() != null) {
            validarTexto(req.nombre(), "nombre");
            e.setNombre(req.nombre().trim());
        }
        if (req.activo() != null) {
            e.setActivo(req.activo());
        }

        return toRes(escolaridadRepo.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public EscolaridadRes obtener(Long id) {
        return escolaridadRepo.findById(id)
                .map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public EscolaridadRes obtenerPorCodigo(String codigo) {
        validarTexto(codigo, "codigo");
        String c = codigo.trim().toUpperCase();

        return escolaridadRepo.findAll().stream()
                .filter(e -> e.getCodigo().equalsIgnoreCase(c))
                .findFirst()
                .map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con código: " + c));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscolaridadRes> listar(Boolean soloActivos) {
        boolean activos = soloActivos != null && soloActivos;
        return escolaridadRepo.findAll().stream()
                .filter(e -> !activos || Boolean.TRUE.equals(e.getActivo()))
                .map(this::toRes)
                .toList();
    }

    @Override
    public void activar(Long id) {
        CatEscolaridad e = escolaridadRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + id));
        e.setActivo(true);
        escolaridadRepo.save(e);
    }

    @Override
    public void desactivar(Long id) {
        CatEscolaridad e = escolaridadRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + id));
        e.setActivo(false);
        escolaridadRepo.save(e);
    }

    private EscolaridadRes toRes(CatEscolaridad e) {
        return new EscolaridadRes(e.getId(), e.getCodigo(), e.getNombre(), e.getActivo());
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}
