package com.io.usyc.Utils;

import com.io.usyc.Domain.AppUser;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class AppUserSpecs {

    public static Specification<AppUser> plantelId(Integer plantelId) {
        return (root, query, cb) -> plantelId == null ? cb.conjunction()
                : cb.equal(root.get("plantel").get("id"), plantelId);
    }

    public static Specification<AppUser> active(Boolean active) {
        return (root, query, cb) -> active == null ? cb.conjunction()
                : cb.equal(root.get("active"), active);
    }

    public static Specification<AppUser> roleCode(String roleCode) {
        return (root, query, cb) -> {
            if (roleCode == null || roleCode.trim().isEmpty()) return cb.conjunction();

            // join a userRoles -> role (distinct para evitar duplicados)
            query.distinct(true);
            var ur = root.join("userRoles", JoinType.LEFT);
            var role = ur.join("role", JoinType.LEFT);
            return cb.equal(role.get("code"), roleCode.trim());
        };
    }

    public static Specification<AppUser> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("alumnoId")), like)
            );
        };
    }
}
