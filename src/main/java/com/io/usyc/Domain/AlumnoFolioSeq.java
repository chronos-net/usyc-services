package com.io.usyc.Domain;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "alumno_folio_seq")
@Data
public class AlumnoFolioSeq {

    @EmbeddedId
    private AlumnoFolioSeqId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrera_id", referencedColumnName = "carrera_id", insertable = false, updatable = false)
    private CatCarrera carrera;

    @Column(name = "ultimo_seq", nullable = false)
    private Integer ultimoSeq = 0;

    // getters/setters
}

