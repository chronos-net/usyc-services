package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cat_plantel")
@Data
public class CatPlantel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plantel_id")
    private Integer id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address", length = 250)
    private String address;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
