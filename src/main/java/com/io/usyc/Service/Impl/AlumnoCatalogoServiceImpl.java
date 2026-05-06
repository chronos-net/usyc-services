package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.Alumno;
import com.io.usyc.Dto.AlumnoListItemRes;
import com.io.usyc.Repository.AlumnoRepository;
import com.io.usyc.Service.AlumnoCatalogoService;
import com.io.usyc.Service.SecurityUserDetails;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class AlumnoCatalogoServiceImpl implements AlumnoCatalogoService {

    private static final String ROL_ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final AlumnoRepository alumnoRepo;

    public AlumnoCatalogoServiceImpl(AlumnoRepository alumnoRepo) {
        this.alumnoRepo = alumnoRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlumnoListItemRes> listar(
            Pageable pageable,
            SecurityUserDetails principal,
            String q,
            Integer escolaridadId,
            Integer plantelId,
            LocalDate fechaIngresoDesde,
            LocalDate fechaIngresoHasta
    ) {

        Integer effectivePlantelId = resolveEffectivePlantelId(principal, plantelId);

        Specification<Alumno> spec = Specification.allOf(
                specQ(q),
                specEscolaridadId(escolaridadId),
                specPlantelId(effectivePlantelId),
                specFechaIngresoDesde(fechaIngresoDesde),
                specFechaIngresoHasta(fechaIngresoHasta)
        );

        return alumnoRepo.findAll(spec, pageable).map(this::toListItem);
    }

    private static boolean tieneRolAdmin(SecurityUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> ROL_ADMIN_AUTHORITY.equals(a.getAuthority()));
    }

    private static Integer resolveEffectivePlantelId(SecurityUserDetails principal, Integer requestedPlantelId) {
        // ADMIN puede ver todo o filtrar por plantelId; usuarios operativos SIEMPRE quedan restringidos a su plantel.
        if (tieneRolAdmin(principal)) return requestedPlantelId;

        var plantel = principal.getUser().getPlantel();
        if (plantel == null) {
            throw new IllegalStateException("El usuario no tiene plantel asignado. Contacta al administrador.");
        }
        return plantel.getId();
    }

    private static Specification<Alumno> specQ(String q) {
        if (q == null || q.trim().isEmpty()) return null;

        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombreCompleto")), like),
                cb.like(cb.lower(root.get("matricula")), like)
        );
    }

    private static Specification<Alumno> specEscolaridadId(Integer escolaridadId) {
        if (escolaridadId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("escolaridad").get("id"), escolaridadId);
    }

    private static Specification<Alumno> specPlantelId(Integer plantelId) {
        if (plantelId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("plantel").get("id"), plantelId);
    }

    private static Specification<Alumno> specFechaIngresoDesde(LocalDate desde) {
        if (desde == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("fechaIngreso"), desde);
    }

    private static Specification<Alumno> specFechaIngresoHasta(LocalDate hasta) {
        if (hasta == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("fechaIngreso"), hasta);
    }


    private AlumnoListItemRes toListItem(Alumno a) {
        var esc = a.getEscolaridad();
        var car = a.getCarrera();
        var pl = a.getPlantel();

        return new AlumnoListItemRes(
                a.getId(),
                a.getNombreCompleto(),
                a.getMatricula(),
                a.getActivo(),
                a.getFechaIngreso(),
                a.getFechaTermino(),
                esc != null ? esc.getId() : null,
                esc != null ? esc.getNombre() : null,
                car != null ? car.getId() : null,
                car != null ? car.getNombre() : null,
                pl != null ? pl.getId() : null,
                pl != null ? pl.getName() : null,
                pl != null ? pl.getCode() : null
        );
    }
}
