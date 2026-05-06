package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.Alumno;
import com.io.usyc.Dto.AlumnoListItemRes;
import com.io.usyc.Repository.AlumnoRepository;
import com.io.usyc.Service.AlumnoCatalogoService;
import com.io.usyc.Service.SecurityUserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<AlumnoListItemRes> listar(Pageable pageable, SecurityUserDetails principal) {

        Page<Alumno> page;
        // ADMIN ve todos los alumnos; usuarios operativos solo ven su plantel asignado.
        if (tieneRolAdmin(principal)) {
            page = alumnoRepo.findAll(pageable);
        } else {
            var plantel = principal.getUser().getPlantel();
            if (plantel == null) {
                throw new IllegalStateException(
                        "El usuario no tiene plantel asignado. Contacta al administrador.");
            }
            page = alumnoRepo.findByPlantel_Id(plantel.getId(), pageable);
        }

        return page.map(this::toListItem);
    }

    private static boolean tieneRolAdmin(SecurityUserDetails principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> ROL_ADMIN_AUTHORITY.equals(a.getAuthority()));
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
