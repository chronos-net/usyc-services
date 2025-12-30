package com.io.usyc.Repository;

import com.io.usyc.Domain.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {
    java.util.Optional<Recibo> findByQrPayload(String qrPayload);
    java.util.Optional<Recibo> findByFolio(String folio);
    @Query("""
    select r
    from Recibo r
    where r.alumno.id = :alumnoId
      and (r.canceladoEn is null)
  """)
    List<Recibo> findPagosNoCancelados(@Param("alumnoId") String alumnoId);

    @Query("""
        select r
        from Recibo r
        join fetch r.alumno a
        join fetch r.estatus e
        join fetch r.tipoPago tp
        where (:plantelId is null or r.plantelId = :plantelId)
        order by r.fechaEmision desc, r.id desc
    """)
    List<Recibo> findAllVisible(@Param("plantelId") Integer plantelId);
}