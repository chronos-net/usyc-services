package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.Alumno;
import com.io.usyc.Dto.AlumnoListItemRes;
import com.io.usyc.Repository.AlumnoRepository;
import com.io.usyc.Repository.AppUserRepository;
import com.io.usyc.Service.AlumnoCatalogoService;
import com.io.usyc.Service.SecurityUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AlumnoCatalogoServiceImpl implements AlumnoCatalogoService {

    private final AlumnoRepository alumnoRepo;
    @Autowired private AppUserRepository userRepo;

    public AlumnoCatalogoServiceImpl(AlumnoRepository alumnoRepo) {
        this.alumnoRepo = alumnoRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AlumnoListItemRes> listar(Pageable pageable, SecurityUserDetails principal) {

        Integer plantelId = principal.getUser().getPlantel() == null
                ? null
                : principal.getUser().getPlantel().getId();

        Page<Alumno> page = (plantelId == null)
                ? alumnoRepo.findAll(pageable)
                : alumnoRepo.findByPlantel_Id(plantelId, pageable);

        return page.map(this::toListItem);
    }



    private Integer currentUserPlantelIdOrNull() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No hay usuario autenticado.");
        }

        String username = auth.getName();

        var user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        // null => admin ve todo
        return user.getPlantel().getId();
    }


    private AlumnoListItemRes toListItem(Alumno a) {
        var esc = a.getEscolaridad();
        var car = a.getCarrera();

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
                car != null ? car.getNombre() : null
        );
    }
}
