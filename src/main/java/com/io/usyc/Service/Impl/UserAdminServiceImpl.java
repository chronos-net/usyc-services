package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.*;
import com.io.usyc.Dto.PasswordChangeReq;
import com.io.usyc.Dto.UserCreateReq;
import com.io.usyc.Dto.UserCreateRes;
import com.io.usyc.Exception.BadRequestException;
import com.io.usyc.Exception.NotFoundException;
import com.io.usyc.Repository.*;
import com.io.usyc.Service.UserAdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final AppUserRepository userRepo;
    private final AppRoleRepository roleRepo;
    private final AppUserRoleRepository userRoleRepo;
    private final CatPlantelRepository plantelRepo;
    private final PasswordEncoder passwordEncoder;

    public UserAdminServiceImpl(AppUserRepository userRepo,
                                AppRoleRepository roleRepo,
                                AppUserRoleRepository userRoleRepo,
                                CatPlantelRepository plantelRepo,
                                PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.userRoleRepo = userRoleRepo;
        this.plantelRepo = plantelRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserCreateRes createUser(UserCreateReq req) {
        validateCreateReq(req);

        if (req.username() != null && userRepo.existsByUsername(req.username())) {
            throw new BadRequestException("El username ya está registrado.");
        }

        CatPlantel plantel = plantelRepo.findById(req.plantelId())
                .orElseThrow(() -> new NotFoundException("Plantel no encontrado: " + req.plantelId()));

        AppUser user = new AppUser();
        user.setEmail(nullIfBlank(req.email()));
        user.setUsername(requiredTrim(req.username(), "username"));
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setFullName(nullIfBlank(req.fullName()));
        user.setActive(true);
        user.setPlantel(plantel);

        // guardar user primero para tener userId (identity)
        user = userRepo.save(user);

        // roles
        Set<AppUserRole> links = new HashSet<>();
        for (Long roleId : req.roleIds()) {
            AppRole role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new NotFoundException("Role no encontrado: " + roleId));
            links.add(new AppUserRole(user, role));
        }

        // Asignar al entity (orphanRemoval/cascade) o guardar por repo; ambas valen.
        user.getUserRoles().clear();
        user.getUserRoles().addAll(links);

        // flush opcional si quieres validar constraints al momento
        user = userRepo.save(user);

        return new UserCreateRes(
                user.getUserId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.isActive(),
                user.getAlumnoId(),
                user.getPlantel() != null ? user.getPlantel().getId() : null,
                user.getUserRoles().stream()
                        .map(ur -> ur.getRole().getCode())
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public void changePassword(Long userId, PasswordChangeReq req) {
        if (userId == null) throw new BadRequestException("userId es requerido.");
        if (req == null) throw new BadRequestException("Body es requerido.");
        if (isBlank(req.currentPassword())) throw new BadRequestException("currentPassword es requerido.");
        if (isBlank(req.newPassword())) throw new BadRequestException("newPassword es requerido.");
        if (req.newPassword().length() < 8) throw new BadRequestException("La nueva contraseña debe tener al menos 8 caracteres.");
        if (req.newPassword().equals(req.currentPassword())) throw new BadRequestException("La nueva contraseña no puede ser igual a la actual.");

        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + userId));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual no es válida.");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepo.save(user);
    }

    private void validateCreateReq(UserCreateReq req) {
        if (req == null) throw new BadRequestException("Body es requerido.");
        if (isBlank(req.username())) throw new BadRequestException("username es requerido.");
        if (isBlank(req.password())) throw new BadRequestException("password es requerido.");
        if (req.password().length() < 8) throw new BadRequestException("password debe tener al menos 8 caracteres.");
        if (req.plantelId() == null) throw new BadRequestException("plantelId es requerido.");
        if (req.roleIds() == null || req.roleIds().isEmpty()) throw new BadRequestException("roleIds es requerido y debe traer al menos 1 rol.");
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nullIfBlank(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String requiredTrim(String s, String field) {
        if (isBlank(s)) throw new BadRequestException(field + " es requerido.");
        return s.trim();
    }
}

