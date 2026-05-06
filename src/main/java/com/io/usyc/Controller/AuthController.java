package com.io.usyc.Controller;

import com.io.usyc.Config.JwtUtil;
import com.io.usyc.Domain.AppUser;
import com.io.usyc.Dto.AuthLoginReq;
import com.io.usyc.Dto.AuthLoginRes;
import com.io.usyc.Dto.AuthUserRes;
import com.io.usyc.Repository.AppUserRepository;
import com.io.usyc.Service.SecurityUserDetails;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final AppUserRepository userRepo;

    public AuthController(AuthenticationManager authManager, JwtUtil jwtUtil, AppUserRepository userRepo) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    public record LoginReq(@NotBlank String username, @NotBlank String password) {}

    @PostMapping("/login")
    public ResponseEntity<AuthLoginRes> login(@RequestBody LoginReq req, HttpServletResponse response) {

        String username = req.username().trim().toLowerCase();

        // 1) valida que exista + trae roles
        AppUser user = userRepo.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Usuario sin password configurado");
        }

        // 2) autentica credenciales
        authManager.authenticate(new UsernamePasswordAuthenticationToken(username, req.password()));

        // Último acceso: persiste para listados admin (GET /api/admin/users).
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);

        // 3) genera JWT con roles
        AuthUserRes userRes = toUserRes(user);
        String token = jwtUtil.generateToken(username, userRes.roles());

        // 4) cookie (LOCAL: secure=false, sameSite=Lax)
        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .secure(jwtUtil.isCookieSecure())
                .path("/")
                .maxAge(jwtUtil.getExpirationSeconds())
                .sameSite(jwtUtil.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new AuthLoginRes("LOGIN_OK", userRes));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserRes> me(@AuthenticationPrincipal SecurityUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        // Ya traes al user dentro del principal
        AppUser user = principal.getUser();

        // opcional: si quieres garantizar roles por JOIN FETCH
        user = userRepo.findByUsernameWithRoles(user.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        return ResponseEntity.ok(toUserRes(user));
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .secure(jwtUtil.isCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite(jwtUtil.getSameSite())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
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
