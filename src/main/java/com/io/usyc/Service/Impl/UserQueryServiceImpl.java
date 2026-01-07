package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.AppUser;
import com.io.usyc.Dto.UserListItemRes;
import com.io.usyc.Repository.AppUserRepository;
import com.io.usyc.Service.UserQueryService;
import com.io.usyc.Utils.AppUserSpecs;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final AppUserRepository userRepo;

    public UserQueryServiceImpl(AppUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public List<UserListItemRes> listUsers(Integer plantelId, Boolean active, String roleCode, String q) {
        Specification<AppUser> spec = Specification
                .where(AppUserSpecs.plantelId(plantelId))
                .and(AppUserSpecs.active(active))
                .and(AppUserSpecs.roleCode(roleCode))
                .and(AppUserSpecs.search(q));

        return userRepo.findAll(spec).stream()
                .map(this::toListItem)
                .toList();
    }

    private UserListItemRes toListItem(AppUser u) {
        Set<String> roles = (u.getUserRoles() == null) ? Set.of() :
                u.getUserRoles().stream()
                        .map(ur -> ur.getRole() != null ? ur.getRole().getCode() : null)
                        .filter(r -> r != null && !r.isBlank())
                        .collect(Collectors.toSet());

        Integer plantelId = (u.getPlantel() != null) ? u.getPlantel().getId() : null;

        return new UserListItemRes(
                u.getUserId(),
                u.getEmail(),
                u.getUsername(),
                u.getFullName(),
                u.isActive(),
                u.getAlumnoId(),
                plantelId,
                roles,
                u.getLastLoginAt(),
                u.getCreatedAt()
        );
    }
}

