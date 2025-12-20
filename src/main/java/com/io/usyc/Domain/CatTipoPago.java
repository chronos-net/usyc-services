package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cat_tipo_pago")
@Data
public class CatTipoPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tipo_pago_id")
    private Integer id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
