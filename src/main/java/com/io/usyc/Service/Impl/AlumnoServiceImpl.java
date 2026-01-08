package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.*;
import com.io.usyc.Dto.AlumnoCreateReq;
import com.io.usyc.Dto.AlumnoRes;
import com.io.usyc.Dto.AlumnoUpdateReq;
import com.io.usyc.Exception.BadRequestException;
import com.io.usyc.Exception.NotFoundException;
import com.io.usyc.Repository.*;
import com.io.usyc.Service.AlumnoService;
import com.io.usyc.Service.ReciboStgMigrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepo;
    private final AlumnoFolioSeqRepository seqRepo;
    private final CatEscolaridadRepository escolaridadRepo;
    private final CatCarreraRepository carreraRepo;
    private final CatPlantelRepository plantelRepo;
    private final ReciboStgMigrationService reciboStgMigrationService;

    public AlumnoServiceImpl(
            AlumnoRepository alumnoRepo,
            AlumnoFolioSeqRepository seqRepo,
            CatEscolaridadRepository escolaridadRepo,
            CatCarreraRepository carreraRepo,
            CatPlantelRepository plantelRepo,
            ReciboStgMigrationService reciboStgMigrationService
    ) {
        this.alumnoRepo = alumnoRepo;
        this.seqRepo = seqRepo;
        this.escolaridadRepo = escolaridadRepo;
        this.carreraRepo = carreraRepo;
        this.plantelRepo = plantelRepo;
        this.reciboStgMigrationService = reciboStgMigrationService;
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

        // ===== Generación de alumnoId AAAACCNNN =====
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

        Alumno saved = alumnoRepo.saveAndFlush(a); // ✅ fuerza INSERT inmediato en DB

        // ===== Bandera: migrar recibos previos desde recibo_stg =====
        Integer migratedCount = null;

        if (Boolean.TRUE.equals(req.pullPrevReceipts())) {

            // ✅ VALIDACIÓN EXTRA: si ya hay recibos migrados para este alumno => excepción
            if (reciboStgMigrationService.alreadyMigratedForAlumno(saved.getId())) {
                throw new IllegalStateException(
                        "Este alumno ya tiene recibos migrados previamente desde la base de datos anterior."
                );
            }

            String nombreLookup = (req.prevReceiptsNombre() != null && !req.prevReceiptsNombre().trim().isEmpty())
                    ? req.prevReceiptsNombre()
                    : req.nombreCompleto();

            // aquí ya no hacemos best-effort; si migración falla, sí debe fallar
            migratedCount = reciboStgMigrationService.migrateByNombreToAlumno(nombreLookup, saved.getId());

            log.info("Migración recibos previos OK. alumnoId={}, nombreLookup='{}', migrated={}",
                    saved.getId(), nombreLookup, migratedCount);
        }

        return toRes(saved, migratedCount);
    }


    @Override
    @Transactional(readOnly = true)
    public AlumnoRes obtener(String alumnoId) {
        validarTexto(alumnoId, "alumnoId");
        Alumno a = alumnoRepo.findById(alumnoId.trim())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe alumno con id: " + alumnoId
                ));
        return toRes(a, null);
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

    private AlumnoRes toRes(Alumno a, Integer recibosPreviosMigrados) {
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
                a.getActivo(),
                recibosPreviosMigrados
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





    @Override
    public AlumnoRes actualizar(String alumnoId, AlumnoUpdateReq req) {
        if (alumnoId == null || alumnoId.trim().isEmpty()) throw new BadRequestException("alumnoId es requerido.");
        if (req == null) throw new BadRequestException("Body es requerido.");

        Alumno a = alumnoRepo.findById(alumnoId)
                .orElseThrow(() -> new NotFoundException("Alumno no encontrado: " + alumnoId));

        // nombreCompleto
        if (req.nombreCompleto() != null) {
            String nc = requiredTrim(req.nombreCompleto(), "nombreCompleto");
            a.setNombreCompleto(nc);
        }

        // matricula (única)
        if (req.matricula() != null) {
            String m = nullIfBlank(req.matricula());
            if (m != null && !m.equals(a.getMatricula()) && alumnoRepo.existsByMatricula(m)) {
                throw new BadRequestException("La matrícula ya está registrada.");
            }
            a.setMatricula(m);
        }

        // escolaridad
        if (req.escolaridadId() != null) {
            CatEscolaridad e = escolaridadRepo.findById(req.escolaridadId())
                    .orElseThrow(() -> new NotFoundException("Escolaridad no encontrada: " + req.escolaridadId()));
            a.setEscolaridad(e);
        }

        // carrera
        if (req.carreraId() != null) {
            CatCarrera c = carreraRepo.findById(req.carreraId())
                    .orElseThrow(() -> new NotFoundException("Carrera no encontrada: " + req.carreraId()));
            a.setCarrera(c);
        }

        // fechas
        if (req.fechaIngreso() != null) a.setFechaIngreso(req.fechaIngreso());
        if (req.fechaTermino() != null) a.setFechaTermino(req.fechaTermino());

        // activo
        if (req.activo() != null) a.setActivo(req.activo());

        // plantel
        if (req.plantelId() != null) {
            CatPlantel p = plantelRepo.findById(req.plantelId())
                    .orElseThrow(() -> new NotFoundException("Plantel no encontrado: " + req.plantelId()));
            a.setPlantel(p);
        }

        // Save opcional (dirty checking también funciona)
        a = alumnoRepo.save(a);

        // Para respuesta con relaciones cargadas
        Alumno full = alumnoRepo.findWithRefsById(a.getId()).orElse(a);
        return toRes(full);
    }

    private AlumnoRes toRes(Alumno a) {
        var e = a.getEscolaridad();
        var c = a.getCarrera();
        var p = a.getPlantel();

        return new AlumnoRes(
                a.getId(),
                a.getNombreCompleto(),
                a.getMatricula(),
                e != null ? e.getId() : null,
                e != null ? e.getNombre() : null,
                c != null ? c.getId() : null,
                c != null ? c.getNombre() : null,
                p != null ? p.getId() : null,
                p != null ? p.getName() : null,
                a.getFechaIngreso(),
                a.getActivo(),null
        );
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String requiredTrim(String s, String field) {
        if (s == null || s.trim().isEmpty()) throw new BadRequestException(field + " es requerido.");
        return s.trim();
    }
}
