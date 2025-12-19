package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.*;
import com.io.usyc.Dto.AlumnoCreateReq;
import com.io.usyc.Dto.AlumnoRes;
import com.io.usyc.Repository.AlumnoFolioSeqRepository;
import com.io.usyc.Repository.AlumnoRepository;
import com.io.usyc.Repository.CatCarreraRepository;
import com.io.usyc.Repository.CatEscolaridadRepository;
import com.io.usyc.Service.AlumnoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepo;
    private final AlumnoFolioSeqRepository seqRepo;
    private final CatEscolaridadRepository escolaridadRepo;
    private final CatCarreraRepository carreraRepo;

    public AlumnoServiceImpl(
            AlumnoRepository alumnoRepo,
            AlumnoFolioSeqRepository seqRepo,
            CatEscolaridadRepository escolaridadRepo,
            CatCarreraRepository carreraRepo
    ) {
        this.alumnoRepo = alumnoRepo;
        this.seqRepo = seqRepo;
        this.escolaridadRepo = escolaridadRepo;
        this.carreraRepo = carreraRepo;
    }

    @Override
    public AlumnoRes crear(AlumnoCreateReq req) {
        validarTexto(req.nombreCompleto(), "nombreCompleto");
        if (req.escolaridadId() == null) throw new IllegalArgumentException("El campo 'escolaridadId' es obligatorio.");
        validarCarreraId(req.carreraId());

        // Normaliza carreraId a 2 dígitos
        String carreraId = normalizarCarreraId(req.carreraId());

        // Valida catálogos existentes
        CatEscolaridad escolaridad = escolaridadRepo.findById(req.escolaridadId())
                .orElseThrow(() -> new IllegalArgumentException("No existe escolaridad con id: " + req.escolaridadId()));

        CatCarrera carrera = carreraRepo.findById(carreraId)
                .orElseThrow(() -> new IllegalArgumentException("No existe carrera con id: " + carreraId));

        // Regla: la carrera debe pertenecer a la escolaridad seleccionada
        if (!carrera.getEscolaridad().getId().equals(escolaridad.getId())) {
            throw new IllegalArgumentException("La carrera '" + carreraId + "' no pertenece a la escolaridad seleccionada.");
        }

        // Matricula opcional pero si viene, que no se repita
        if (req.matricula() != null && !req.matricula().trim().isEmpty()) {
            String matricula = req.matricula().trim().toUpperCase();
            if (alumnoRepo.existsByMatricula(matricula)) {
                throw new IllegalArgumentException("Ya existe un alumno con matrícula: " + matricula);
            }
        }

        // Genera alumno_id: AAAA + CC + NNN (con lock)
        int anio = LocalDate.now().getYear();
        AlumnoFolioSeq seq = obtenerOCrearSeqBloqueado(anio, carreraId);
        int nuevoSeq = seq.getUltimoSeq() + 1;
        seq.setUltimoSeq(nuevoSeq);
        seqRepo.save(seq);

        String alumnoId = String.valueOf(anio) + carreraId + String.format("%03d", nuevoSeq);

        Alumno a = new Alumno();
        a.setId(alumnoId);
        a.setNombreCompleto(req.nombreCompleto().trim().toUpperCase());
        a.setMatricula(req.matricula() == null ? null : req.matricula().trim().toUpperCase());
        a.setEscolaridad(escolaridad);
        a.setCarrera(carrera);
        a.setFechaIngreso(req.fechaIngreso() == null ? LocalDate.now() : req.fechaIngreso());
        a.setActivo(true);

        // timestamps
        a.setCreadoEn(LocalDateTime.now());
        a.setActualizadoEn(LocalDateTime.now());

        Alumno saved = alumnoRepo.save(a);
        return toRes(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AlumnoRes obtener(String alumnoId) {
        validarTexto(alumnoId, "alumnoId");
        Alumno a = alumnoRepo.findById(alumnoId.trim())
                .orElseThrow(() -> new IllegalArgumentException("No existe alumno con id: " + alumnoId));
        return toRes(a);
    }

    private AlumnoFolioSeq obtenerOCrearSeqBloqueado(int anio, String carreraId) {
        // Intento 1: buscar con lock
        var opt = seqRepo.findForUpdate(anio, carreraId);
        if (opt.isPresent()) return opt.get();

        // Si no existe, lo creamos
        AlumnoFolioSeq nuevo = new AlumnoFolioSeq();
        nuevo.setId(new AlumnoFolioSeqId(anio, carreraId));
        nuevo.setUltimoSeq(0);
        seqRepo.save(nuevo);

        // Y ahora sí lo volvemos a pedir con lock (para evitar carreras en paralelo)
        return seqRepo.findForUpdate(anio, carreraId)
                .orElseThrow(() -> new IllegalStateException("No se pudo inicializar el consecutivo para año/carrera."));
    }

    private AlumnoRes toRes(Alumno a) {
        return new AlumnoRes(
                a.getId(),
                a.getNombreCompleto(),
                a.getMatricula(),
                a.getEscolaridad().getId(),
                a.getEscolaridad().getNombre(),
                a.getCarrera().getId(),
                a.getCarrera().getNombre(),
                a.getFechaIngreso(),
                a.getActivo()
        );
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
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
}

