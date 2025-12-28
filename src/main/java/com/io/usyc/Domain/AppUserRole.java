package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_user_role")
@Getter @Setter
@NoArgsConstructor
public class AppUserRole {

    @EmbeddedId
    private AppUserRoleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roleId")
    @JoinColumn(name = "role_id", nullable = false)
    private AppRole role;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    public AppUserRole(AppUser user, AppRole role) {
        this.user = user;
        this.role = role;
        this.id = new AppUserRoleId(user.getUserId(), role.getRoleId());
        this.assignedAt = LocalDateTime.now();
    }
}

