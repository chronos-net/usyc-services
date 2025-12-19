package com.io.usyc.Domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class AlumnoFolioSeqId implements Serializable {

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "carrera_id", nullable = false, length = 2)
    private String carreraId;

    public AlumnoFolioSeqId() {}

    public AlumnoFolioSeqId(Integer anio, String carreraId) {
        this.anio = anio;
        this.carreraId = carreraId;
    }

    // equals/hashCode
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlumnoFolioSeqId that)) return false;
        return Objects.equals(anio, that.anio) && Objects.equals(carreraId, that.carreraId);
    }

    @Override public int hashCode() {
        return Objects.hash(anio, carreraId);
    }

    // getters/setters
}

