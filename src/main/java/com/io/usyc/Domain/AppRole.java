package com.io.usyc.Domain;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_role")
@Data
public class AppRole {
    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "code", nullable = false, unique = true)
    private String code;
}
