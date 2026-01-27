package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CarreraConceptoConfig;
import com.io.usyc.Domain.CatCarrera;
import com.io.usyc.Domain.CatConceptoPago;
import com.io.usyc.Domain.CatEscolaridad;
import com.io.usyc.Dto.*;
import com.io.usyc.Mapper.CarreraMapper;
import com.io.usyc.Repository.CarreraConceptoConfigRepository;
import com.io.usyc.Repository.CatCarreraRepository;
import com.io.usyc.Repository.CatConceptoPagoRepository;
import com.io.usyc.Repository.CatEscolaridadRepository;
import com.io.usyc.Service.CarreraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CarreraServiceImpl implements CarreraService {

    private final CatCarreraRepository carreraRepo;
    private final CatEscolaridadRepository escolaridadRepo;
    @Autowired private CatConceptoPagoRepository conceptoRepo;
    @Autowired private CarreraConceptoConfigRepository carreraConceptoRepo;
    @Autowired private CarreraMapper carreraMapper;

    public CarreraServiceImpl(CatCarreraRepository carreraRepo, CatEscolaridadRepository escolaridadRepo) {
        this.carreraRepo = carreraRepo;
        this.escolaridadRepo = escolaridadRepo;
    }
    @Override
    @Transactional
    public CarreraRes crear(CarreraCreateReq req) {

        validarTexto(req.carreraId(), "carreraId");
        validarTexto(req.nombre(), "nombre");

        if (req.escolaridadId() == null) {
            throw new IllegalArgumentException("El campo 'escolaridadId' es obligatorio.");
        }

        String carreraId = normalizarCarreraId(req.carreraId());

        if (carreraRepo.existsById(carreraId)) {
            throw new IllegalArgumentException("Ya existe una carrera con id: " + carreraId);
        }

        int anios = normalizarAnios(req.duracionAnios());
        int meses = normalizarMeses(req.duracionMeses());
        validarDuracionNoCero(anios, meses);

        List<CarreraConceptoConfigReq> conceptosReq = (req.conceptos() == null) ? List.of() : req.conceptos();
        if (conceptosReq.isEmpty()) {
            throw new IllegalArgumentException("El campo 'conceptos' es obligatorio y no puede venir vacío.");
        }

        validarConceptos(conceptosReq);

        CatEscolaridad esc = escolaridadRepo.findById(req.escolaridadId())
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + req.escolaridadId()));

        CatCarrera carrera = new CatCarrera();
        carrera.setId(carreraId);
        carrera.setEscolaridad(esc);
        carrera.setNombre(req.nombre().trim());
        carrera.setDuracionAnios(anios);
        carrera.setDuracionMeses(meses);
        carrera.setActivo(req.activo() == null ? Boolean.TRUE : req.activo());

        // Guardar carrera primero (para FK)
        CatCarrera saved = carreraRepo.save(carrera);

        // Guardar configs
        for (CarreraConceptoConfigReq cc : conceptosReq) {
            CatConceptoPago concepto = conceptoRepo.findById(cc.conceptoId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe conceptoPago con id: " + cc.conceptoId()));

            CarreraConceptoConfig cfg = new CarreraConceptoConfig();
            cfg.setCarrera(saved);
            cfg.setConcepto(concepto);
            cfg.setMonto(cc.monto());
            cfg.setCantidad(cc.cantidad());
            cfg.setActivo(cc.activo() == null ? Boolean.TRUE : cc.activo());

            carreraConceptoRepo.save(cfg);
        }

        List<CarreraConceptoConfig> configs = carreraConceptoRepo.findByCarrera_Id(saved.getId());
        return CarreraMapper.toRes(saved, configs);
    }
    private void validarConceptos(List<CarreraConceptoConfigReq> conceptosReq) {
        Set<Long> ids = new HashSet<>();
        for (CarreraConceptoConfigReq c : conceptosReq) {

            if (c.conceptoId() == null) {
                throw new IllegalArgumentException("Cada concepto debe incluir 'conceptoId'.");
            }
            if (!ids.add(c.conceptoId())) {
                throw new IllegalArgumentException("Concepto repetido en la lista: conceptoId=" + c.conceptoId());
            }

            validarMonto(c.monto(), "monto");
            if (c.cantidad() == null || c.cantidad() <= 0) {
                throw new IllegalArgumentException("La 'cantidad' debe ser mayor a 0 para conceptoId=" + c.conceptoId());
            }
        }
    }    @Override
    @Transactional
    public CarreraRes actualizar(String carreraId, CarreraUpdateReq req) {

        String id = normalizarCarreraId(carreraId);

        CatCarrera c = carreraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + id));

        // 1) Escolaridad
        if (req.escolaridadId() != null) {
            CatEscolaridad esc = escolaridadRepo.findById(req.escolaridadId())
                    .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + req.escolaridadId()));
            c.setEscolaridad(esc);
        }

        // 2) Nombre
        if (req.nombre() != null) {
            validarTexto(req.nombre(), "nombre");
            c.setNombre(req.nombre().trim());
        }

        // 3) Duración (si viene uno, validamos ambos con valores finales)
        if (req.duracionAnios() != null || req.duracionMeses() != null) {
            int aniosFinal = normalizarAnios(req.duracionAnios() != null ? req.duracionAnios() : c.getDuracionAnios());
            int mesesFinal = normalizarMeses(req.duracionMeses() != null ? req.duracionMeses() : c.getDuracionMeses());
            validarDuracionNoCero(aniosFinal, mesesFinal);

            c.setDuracionAnios(aniosFinal);
            c.setDuracionMeses(mesesFinal);
        }

        // 4) Activo
        if (req.activo() != null) {
            c.setActivo(req.activo());
        }

        // Guardar carrera
        CatCarrera saved = carreraRepo.save(c);

        // 5) Conceptos (opcional)
        if (req.conceptos() != null) {
            if (req.conceptos().isEmpty()) {
                throw new IllegalArgumentException("Si envías 'conceptos', no puede venir vacío.");
            }

            validarConceptos(req.conceptos()); // mismo validador que en crear()

            // Traer actuales y mapear por conceptoId
            List<CarreraConceptoConfig> actuales = carreraConceptoRepo.findByCarrera_Id(saved.getId());
            java.util.Map<Long, CarreraConceptoConfig> porConceptoId = new java.util.HashMap<>();
            for (CarreraConceptoConfig cfg : actuales) {
                porConceptoId.put(cfg.getConcepto().getId(), cfg);
            }

            // Upsert de los que vienen
            for (CarreraConceptoConfigReq cc : req.conceptos()) {

                CatConceptoPago concepto = conceptoRepo.findById(cc.conceptoId())
                        .orElseThrow(() -> new IllegalArgumentException("No existe conceptoPago con id: " + cc.conceptoId()));

                CarreraConceptoConfig existente = porConceptoId.get(concepto.getId());

                if (existente == null) {
                    CarreraConceptoConfig nuevo = new CarreraConceptoConfig();
                    nuevo.setCarrera(saved);
                    nuevo.setConcepto(concepto);
                    nuevo.setMonto(cc.monto());
                    nuevo.setCantidad(cc.cantidad());
                    nuevo.setActivo(cc.activo() == null ? Boolean.TRUE : cc.activo());
                    carreraConceptoRepo.save(nuevo);
                } else {
                    existente.setMonto(cc.monto());
                    existente.setCantidad(cc.cantidad());
                    if (cc.activo() != null) existente.setActivo(cc.activo());
                    carreraConceptoRepo.save(existente);
                }
            }

            // ✅ OPCIONAL (si quieres “sincronizar”): desactivar los que NO vinieron
        /*
        java.util.Set<Long> enviados = req.conceptos().stream()
                .map(CarreraConceptoConfigReq::conceptoId)
                .collect(java.util.stream.Collectors.toSet());

        for (CarreraConceptoConfig cfg : actuales) {
            if (!enviados.contains(cfg.getConcepto().getId())) {
                cfg.setActivo(false);
                carreraConceptoRepo.save(cfg);
            }
        }
        */
        }

        // Respuesta con conceptos + total proyectado
        List<CarreraConceptoConfig> configsFinal = carreraConceptoRepo.findByCarrera_Id(saved.getId());
        return CarreraMapper.toRes(saved, configsFinal);
    }



    @Override
    @Transactional(readOnly = true)
    public CarreraRes obtener(String carreraId) {

        String id = normalizarCarreraId(carreraId);

        CatCarrera c = carreraRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + id));

        List<CarreraConceptoConfig> configs = carreraConceptoRepo.findByCarrera_Id(c.getId());

        // usa tu mapper nuevo
        return CarreraMapper.toRes(c, configs);
    }


    @Override
    @Transactional(readOnly = true)
    public List<CarreraRes> listar(Boolean soloActivos) {

        boolean activos = soloActivos != null && soloActivos;

        List<CatCarrera> carreras = activos
                ? carreraRepo.findByActivoTrue()
                : carreraRepo.findAll();

        if (carreras.isEmpty()) return List.of();

        List<String> ids = carreras.stream().map(CatCarrera::getId).toList();

        // Traer TODAS las configs de una sola vez
        List<CarreraConceptoConfig> configs = carreraConceptoRepo.findByCarrera_IdIn(ids);

        // Agrupar por carreraId
        var porCarrera = configs.stream()
                .collect(java.util.stream.Collectors.groupingBy(x -> x.getCarrera().getId()));

        return carreras.stream()
                .map(c -> CarreraMapper.toRes(c, porCarrera.getOrDefault(c.getId(), List.of())))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public List<CarreraRes> listarPorEscolaridad(Long escolaridadId, Boolean soloActivos) {

        if (escolaridadId == null) {
            throw new IllegalArgumentException("El campo 'escolaridadId' es obligatorio.");
        }

        boolean activos = soloActivos != null && soloActivos;

        List<CatCarrera> carreras = activos
                ? carreraRepo.findByEscolaridad_IdAndActivoTrue(escolaridadId)
                : carreraRepo.findByEscolaridad_Id(escolaridadId);

        if (carreras.isEmpty()) return List.of();

        List<String> ids = carreras.stream().map(CatCarrera::getId).toList();

        List<CarreraConceptoConfig> configs = carreraConceptoRepo.findByCarrera_IdIn(ids);

        var porCarrera = configs.stream()
                .collect(java.util.stream.Collectors.groupingBy(x -> x.getCarrera().getId()));

        return carreras.stream()
                .map(c -> CarreraMapper.toRes(c, porCarrera.getOrDefault(c.getId(), List.of())))
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

    private CarreraRes toRes(CatCarrera c, List<CarreraConceptoConfig> configs) {
        var esc = c.getEscolaridad();

        java.math.BigDecimal total = configs.stream()
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .map(x -> x.getMonto().multiply(java.math.BigDecimal.valueOf(x.getCantidad())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        var conceptos = configs.stream()
                .map(x -> new CarreraConceptoConfigRes(
                        x.getConcepto().getId(),
                        x.getConcepto().getCodigo(),
                        x.getConcepto().getNombre(),
                        x.getMonto(),
                        x.getCantidad(),
                        x.getActivo()
                ))
                .toList();

        return new CarreraRes(
                c.getId(),
                esc != null ? esc.getId() : null,
                esc != null ? esc.getNombre() : null,
                c.getNombre(),
                c.getDuracionAnios(),
                c.getDuracionMeses(),
                c.getActivo(),
                total,
                conceptos
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

    private static int normalizarAnios(Integer anios) {
        if (anios == null) return 0;
        if (anios < 0) throw new IllegalArgumentException("La duración en años no puede ser negativa.");
        return anios;
    }

    private static int normalizarMeses(Integer meses) {
        if (meses == null) return 0;
        if (meses < 0 || meses > 11) throw new IllegalArgumentException("La duración en meses debe estar entre 0 y 11.");
        return meses;
    }

    private static void validarDuracionNoCero(int anios, int meses) {
        if (anios == 0 && meses == 0) {
            throw new IllegalArgumentException("La duración no puede ser 0 años y 0 meses.");
        }
    }
}
