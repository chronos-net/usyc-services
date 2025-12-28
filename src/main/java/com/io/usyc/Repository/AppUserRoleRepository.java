package com.io.usyc.Repository;

import com.io.usyc.Domain.AppUserRole;
import com.io.usyc.Domain.AppUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRoleRepository extends JpaRepository<AppUserRole, AppUserRoleId> {
}
