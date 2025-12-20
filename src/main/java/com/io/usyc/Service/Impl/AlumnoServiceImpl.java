package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.*;
import com.io.usyc.Dto.AlumnoCreateReq;
import com.io.usyc.Dto.AlumnoRes;
import com.io.usyc.Repository.*;
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
    private final CatPlantelRepository plantelRepo;

    public AlumnoServiceImpl(
            AlumnoRepository alumnoRepo,
            AlumnoFolioSeqRepository seqRepo,
            CatEscolaridadRepository escolaridadRepo,
            CatCarreraRepository carreraRepo,
            CatPlantelRepository plantelRepo
    ) {
        this.alumnoRepo = alumnoRepo;
        this.seqRepo = seqRepo;
        this.escolaridadRepo = escolaridadRepo;
        this.carreraRepo = carreraRepo;
        this.plantelRepo = plantelRepo;
    }

    @Override
    public AlumnoRes crear(AlumnoCreateReq req) {
        validarTexto(req.nombreCompleto(), "nombreCompleto");

        if (req.escolaridadId() == null)
            throw new IllegalArgumentException("El campo 'escolaridadId' es obligatorio.");

        validarCarreraId(req.carreraId());

        if (req.plantelId() == null)
            throw new IllegalArgumentException("El campo 'plantelId' es obligatorio.");

        // Normaliza carreraId a 2 dígitos
        String carreraId = normalizarCarreraId(req.carreraId());

        CatEscolaridad escolaridad = escolaridadRepo.findById(req.escolaridadId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe escolaridad con id: " + req.escolaridadId()
                ));

        CatCarrera carrera = carreraRepo.findById(carreraId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe carrera con id: " + carreraId
                ));

        if (!carrera.getEscolaridad().getId().equals(escolaridad.getId())) {
            throw new IllegalArgumentException(
                    "La carrera '" + carreraId + "' no pertenece a la escolaridad seleccionada."
            );
        }

        CatPlantel plantel = plantelRepo.findById(req.plantelId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe plantel con id: " + req.plantelId()
                ));

        if (!Boolean.TRUE.equals(plantel.getActive())) {
            throw new IllegalArgumentException("El plantel seleccionado está inactivo.");
        }

        if (req.matricula() != null && !req.matricula().trim().isEmpty()) {
            String matricula = req.matricula().trim().toUpperCase();
            if (alumnoRepo.existsByMatricula(matricula)) {
                throw new IllegalArgumentException("Ya existe un alumno con matrícula: " + matricula);
            }
        }

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
        a.setPlantel(plantel);
        a.setFechaIngreso(req.fechaIngreso() == null ? LocalDate.now() : req.fechaIngreso());
        a.setActivo(true);
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
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe alumno con id: " + alumnoId
                ));
        return toRes(a);
    }

    private AlumnoFolioSeq obtenerOCrearSeqBloqueado(int anio, String carreraId) {
        var opt = seqRepo.findForUpdate(anio, carreraId);
        if (opt.isPresent()) return opt.get();

        AlumnoFolioSeq nuevo = new AlumnoFolioSeq();
        nuevo.setId(new AlumnoFolioSeqId(anio, carreraId));
        nuevo.setUltimoSeq(0);
        seqRepo.save(nuevo);

        return seqRepo.findForUpdate(anio, carreraId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo inicializar el consecutivo para año/carrera."
                ));
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
                a.getPlantel().getId(),
                a.getPlantel().getName(),
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
        if (v == null || v.trim().isEmpty())
            throw new IllegalArgumentException("El campo 'carreraId' es obligatorio.");
        String s = v.trim();
        if (!s.matches("\\d{1,2}"))
            throw new IllegalArgumentException(
                    "El 'carreraId' debe ser numérico de 1 o 2 dígitos (ej: 01, 10)."
            );
    }

    private static String normalizarCarreraId(String v) {
        validarCarreraId(v);
        return String.format("%02d", Integer.parseInt(v.trim()));
    }
}
