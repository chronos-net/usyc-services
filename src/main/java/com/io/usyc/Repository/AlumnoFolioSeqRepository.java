package com.io.usyc.Repository;

import com.io.usyc.Domain.AlumnoFolioSeq;
import com.io.usyc.Domain.AlumnoFolioSeqId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AlumnoFolioSeqRepository extends JpaRepository<AlumnoFolioSeq, AlumnoFolioSeqId> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from AlumnoFolioSeq s where s.id.anio = :anio and s.id.carreraId = :carreraId")
    Optional<AlumnoFolioSeq> findForUpdate(@Param("anio") Integer anio, @Param("carreraId") String carreraId);
}
