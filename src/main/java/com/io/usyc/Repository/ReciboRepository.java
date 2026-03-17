package com.io.usyc.Repository;

import com.io.usyc.Domain.Recibo;
import com.io.usyc.Repository.Projection.CorteResumenRow;
import com.io.usyc.Repository.Projection.CorteTipoPagoRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {

    java.util.Optional<Recibo> findByQrPayload(String qrPayload);
    java.util.Optional<Recibo> findByFolio(String folio);

    @Query("""
        select r
        from Recibo r
        where r.alumno.id = :alumnoId
          and r.canceladoEn is null
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

    @Query("""
        select
          count(r.id) as totalRecibos,
          coalesce(sum(r.monto), 0) as totalMonto,
          sum(case when r.canceladoEn is not null then 1 else 0 end) as totalCancelados,
          coalesce(sum(case when r.canceladoEn is not null then r.monto else 0 end), 0) as totalMontoCancelado
        from Recibo r
        where r.fechaPago = :fecha
          and (:plantelId is null or r.plantelId = :plantelId)
    """)
    CorteResumenRow resumenCorte(@Param("fecha") LocalDate fecha,
                                 @Param("plantelId") Integer plantelId);

    @Query("""
        select
          r.tipoPago.id as tipoPagoId,
          r.tipoPago.name as tipoPagoDesc,
          count(r.id) as totalRecibos,
          coalesce(sum(r.monto), 0) as totalMonto
        from Recibo r
        where r.fechaPago = :fecha
          and r.canceladoEn is null
          and (:plantelId is null or r.plantelId = :plantelId)
        group by r.tipoPago.id, r.tipoPago.name
        order by r.tipoPago.name
    """)
    List<CorteTipoPagoRow> resumenPorTipoPago(@Param("fecha") LocalDate fecha,
                                              @Param("plantelId") Integer plantelId);

    @Query("""
        select r
        from Recibo r
        join fetch r.alumno a
        join fetch r.estatus e
        join fetch r.tipoPago tp
        where r.fechaPago = :fecha
          and (:plantelId is null or r.plantelId = :plantelId)
        order by r.creadoEn asc
    """)
    List<Recibo> findRecibosDelDia(@Param("fecha") LocalDate fecha,
                                   @Param("plantelId") Integer plantelId);

    @Query("""
        select
          count(r.id) as totalRecibos,
          coalesce(sum(r.monto), 0) as totalMonto,
          sum(case when r.canceladoEn is not null then 1 else 0 end) as totalCancelados,
          coalesce(sum(case when r.canceladoEn is not null then r.monto else 0 end), 0) as totalMontoCancelado
        from Recibo r
        where r.fechaPago >= :fechaInicio
          and r.fechaPago <= :fechaFin
          and (:plantelId is null or r.plantelId = :plantelId)
    """)
    CorteResumenRow resumenCorteRango(@Param("fechaInicio") LocalDate fechaInicio,
                                      @Param("fechaFin") LocalDate fechaFin,
                                      @Param("plantelId") Integer plantelId);

    @Query("""
        select
          r.tipoPago.id as tipoPagoId,
          r.tipoPago.name as tipoPagoDesc,
          count(r.id) as totalRecibos,
          coalesce(sum(r.monto), 0) as totalMonto
        from Recibo r
        where r.fechaPago >= :fechaInicio
          and r.fechaPago <= :fechaFin
          and r.canceladoEn is null
          and (:plantelId is null or r.plantelId = :plantelId)
        group by r.tipoPago.id, r.tipoPago.name
        order by r.tipoPago.name
    """)
    List<CorteTipoPagoRow> resumenPorTipoPagoRango(@Param("fechaInicio") LocalDate fechaInicio,
                                                   @Param("fechaFin") LocalDate fechaFin,
                                                   @Param("plantelId") Integer plantelId);

    @Query("""
        select r
        from Recibo r
        join fetch r.alumno a
        join fetch r.estatus e
        join fetch r.tipoPago tp
        where r.fechaPago >= :fechaInicio
          and r.fechaPago <= :fechaFin
          and (:plantelId is null or r.plantelId = :plantelId)
        order by r.fechaPago asc, r.creadoEn asc
    """)
    List<Recibo> findRecibosDelRango(@Param("fechaInicio") LocalDate fechaInicio,
                                     @Param("fechaFin") LocalDate fechaFin,
                                     @Param("plantelId") Integer plantelId);
}