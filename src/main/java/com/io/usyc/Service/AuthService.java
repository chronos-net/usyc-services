package com.io.usyc.Service;

import com.io.usyc.Config.JwtUtil;
import com.io.usyc.Domain.AppRole;
import com.io.usyc.Domain.AppUser;
import com.io.usyc.Domain.AppUserRole;
import com.io.usyc.Domain.AppUserRoleId;
import com.io.usyc.Dto.AuthLoginReq;
import com.io.usyc.Dto.AuthLoginRes;
import com.io.usyc.Dto.AuthRegisterReq;
import com.io.usyc.Dto.AuthUserRes;
import com.io.usyc.Repository.AppRoleRepository;
import com.io.usyc.Repository.AppUserRepository;
import com.io.usyc.Repository.AppUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "LECTOR";
    private final AppUserRepository userRepo;
    private final AppRoleRepository roleRepo;
    private final AppUserRoleRepository userRoleRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;

    public AuthService(
            AppUserRepository userRepo,
            AppRoleRepository roleRepo,
            AppUserRoleRepository userRoleRepo,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager
    ) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.userRoleRepo = userRoleRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthUserRes register(AuthRegisterReq req) {
        String username = (req.username() == null) ? null : req.username().trim().toLowerCase();

        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required");
        if (req.password() == null || req.password().isBlank()) throw new IllegalArgumentException("password is required");

        if (userRepo.existsByUsername(username)) throw new IllegalStateException("Username already registered");

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setFullName(req.fullName());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepo.save(user);

        List<String> roleCodes = (req.roleCodes() == null || req.roleCodes().isEmpty())
                ? List.of(DEFAULT_ROLE)
                : req.roleCodes().stream().map(r -> r.trim().toUpperCase()).distinct().toList();

        for (String roleCode : roleCodes) {
            AppRole role = roleRepo.findByCode(roleCode)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleCode));

            AppUserRole ur = new AppUserRole();
            ur.setId(new AppUserRoleId(user.getUserId(), role.getRoleId()));
            ur.setUser(user);
            ur.setRole(role);
            ur.setAssignedAt(LocalDateTime.now());
            userRoleRepo.save(ur);
        }

        AppUser saved = userRepo.findByUsernameWithRoles(username).orElseThrow();
        return toUserRes(saved);
    }


    @Transactional
    public AuthLoginRes login(AuthLoginReq req) {

        String username = (req.username() == null) ? null : req.username().trim().toLowerCase();
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required");

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, req.password())
        );

        AppUser user = userRepo.findByUsernameWithRoles(username)
                .orElseThrow(() -> new IllegalStateException("User not found after auth: " + username));

        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        return new AuthLoginRes("LOGIN_OK", toUserRes(user));
    }
    private AuthUserRes toUserRes(AppUser user) {

        var roles = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getCode())
                .distinct()
                .toList();

        Integer plantelId = null;
        String plantelName = null;

        if (user.getPlantel() != null) {
            plantelId = user.getPlantel().getId();
            plantelName = user.getPlantel().getName();
        }

        return new AuthUserRes(
                user.getUserId(),
                user.getUsername(),
                user.getFullName(),
                user.isActive(),
                roles,
                plantelId,
                plantelName
        );
    }


}
