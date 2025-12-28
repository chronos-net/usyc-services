package com.io.usyc.Repository;

import com.io.usyc.Domain.AppUser;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

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



}

