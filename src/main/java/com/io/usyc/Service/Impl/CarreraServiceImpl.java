package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CatCarrera;
import com.io.usyc.Domain.CatEscolaridad;
import com.io.usyc.Dto.CarreraCreateReq;
import com.io.usyc.Dto.CarreraRes;
import com.io.usyc.Dto.CarreraUpdateReq;
import com.io.usyc.Repository.CatCarreraRepository;
import com.io.usyc.Repository.CatEscolaridadRepository;
import com.io.usyc.Service.CarreraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CarreraServiceImpl implements CarreraService {

    private final CatCarreraRepository carreraRepo;
    private final CatEscolaridadRepository escolaridadRepo;

    public CarreraServiceImpl(CatCarreraRepository carreraRepo, CatEscolaridadRepository escolaridadRepo) {
        this.carreraRepo = carreraRepo;
        this.escolaridadRepo = escolaridadRepo;
    }

    @Override
    public CarreraRes crear(CarreraCreateReq req) {
        validarCarreraId(req.carreraId());
        validarTexto(req.nombre(), "nombre");
        validarMonto(req.montoMensual(), "montoMensual");
        validarMonto(req.montoInscripcion(), "montoInscripcion");
        if (req.escolaridadId() == null) throw new IllegalArgumentException("El campo 'escolaridadId' es obligatorio.");

        String carreraId = normalizarCarreraId(req.carreraId());

        if (carreraRepo.existsById(carreraId)) {
            throw new IllegalArgumentException("Ya existe una carrera con id: " + carreraId);
        }

        CatEscolaridad esc = escolaridadRepo.findById(req.escolaridadId())
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + req.escolaridadId()));

        CatCarrera c = new CatCarrera();
        c.setId(carreraId);
        c.setEscolaridad(esc);
        c.setNombre(req.nombre().trim());
        c.setMontoMensual(req.montoMensual());
        c.setMontoInscripcion(req.montoInscripcion());
        c.setActivo(req.activo() == null ? Boolean.TRUE : req.activo());

        return toRes(carreraRepo.save(c));
    }

    @Override
    public CarreraRes actualizar(String carreraId, CarreraUpdateReq req) {
        String id = normalizarCarreraId(carreraId);

        CatCarrera c = carreraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + id));

        if (req.escolaridadId() != null) {
            CatEscolaridad esc = escolaridadRepo.findById(req.escolaridadId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + req.escolaridadId()));
            c.setEscolaridad(esc);
        }

        if (req.nombre() != null) {
            validarTexto(req.nombre(), "nombre");
            c.setNombre(req.nombre().trim());
        }
        if (req.montoMensual() != null) {
            validarMonto(req.montoMensual(), "montoMensual");
            c.setMontoMensual(req.montoMensual());
        }
        if (req.montoInscripcion() != null) {
            validarMonto(req.montoInscripcion(), "montoInscripcion");
            c.setMontoInscripcion(req.montoInscripcion());
        }
        if (req.activo() != null) {
            c.setActivo(req.activo());
        }

        return toRes(carreraRepo.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public CarreraRes obtener(String carreraId) {
        String id = normalizarCarreraId(carreraId);
        return carreraRepo.findById(id)
                .map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraRes> listar(Boolean soloActivos) {
        boolean activos = soloActivos != null && soloActivos;
        return carreraRepo.findAll().stream()
                .filter(c -> !activos || Boolean.TRUE.equals(c.getActivo()))
                .map(this::toRes)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraRes> listarPorEscolaridad(Long escolaridadId, Boolean soloActivos) {
        if (escolaridadId == null) throw new IllegalArgumentException("El campo 'escolaridadId' es obligatorio.");
        boolean activos = soloActivos != null && soloActivos;

        return carreraRepo.findAll().stream()
                .filter(c -> c.getEscolaridad() != null && escolaridadId.equals(c.getEscolaridad().getId()))
                .filter(c -> !activos || Boolean.TRUE.equals(c.getActivo()))
                .map(this::toRes)
                .toList();
    }

    @Override
    public void activar(String carreraId) {
        String id = normalizarCarreraId(carreraId);
        CatCarrera c = carreraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + id));
        c.setActivo(true);
        carreraRepo.save(c);
    }

    @Override
    public void desactivar(String carreraId) {
        String id = normalizarCarreraId(carreraId);
        CatCarrera c = carreraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + id));
        c.setActivo(false);
        carreraRepo.save(c);
    }

    private CarreraRes toRes(CatCarrera c) {
        var esc = c.getEscolaridad();
        return new CarreraRes(
                c.getId(),
                esc != null ? esc.getId() : null,
                esc != null ? esc.getNombre() : null,
                c.getNombre(),
                c.getMontoMensual(),
                c.getMontoInscripcion(),
                c.getActivo()
        );
    }

    private static void validarCarreraId(String v) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("El campo 'carreraId' es obligatorio.");
        String s = v.trim();
        if (!s.matches("\\d{1,2}")) throw new IllegalArgumentException("El 'carreraId' debe ser numérico de 1 o 2 dígitos (ej: 01, 10).");
    }

    private static String normalizarCarreraId(String v) {
        validarCarreraId(v);
        return String.format("%02d", Integer.parseInt(v.trim()));
    }

    private static void validarMonto(BigDecimal v, String campo) {
        if (v == null) throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        if (v.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El campo '" + campo + "' no puede ser negativo.");
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
    }
}

