package com.io.usyc.Repository;

import com.io.usyc.Domain.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppRoleRepository extends JpaRepository<AppRole, Long> {
    Optional<AppRole> findByCode(String code);
}