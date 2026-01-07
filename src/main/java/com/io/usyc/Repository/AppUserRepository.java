package com.io.usyc.Repository;

import com.io.usyc.Domain.AppUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("""
  select distinct u
  from AppUser u
  left join fetch u.userRoles ur
  left join fetch ur.role r
  left join fetch u.plantel p
  where u.username = :username
""")
    Optional<AppUser> findByUsernameWithRoles(@Param("username") String username);

    boolean existsByEmail(String email);
    boolean existsByAlumnoId(String alumnoId);

    Optional<AppUser> findByEmail(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role", "plantel"})
    List<AppUser> findAll(org.springframework.data.jpa.domain.Specification<AppUser> spec);

}

