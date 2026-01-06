package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.Recibo;
import com.io.usyc.Dto.*;
import com.io.usyc.Repository.Projection.CorteResumenRow;
import com.io.usyc.Repository.Projection.CorteTipoPagoRow;
import com.io.usyc.Repository.ReciboRepository;
import com.io.usyc.Service.CorteCajaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CorteCajaServiceImpl implements CorteCajaService {

    private final ReciboRepository reciboRepo;

    public CorteCajaServiceImpl(ReciboRepository reciboRepo) {
        this.reciboRepo = reciboRepo;
    }

    @Override
    public CorteCajaDiarioRes generarCorteDiario(LocalDate fecha, Integer plantelId) {
        if (fecha == null) {
            fecha = LocalDate.now();
        }

        CorteResumenRow resumenRow = reciboRepo.resumenCorte(fecha, plantelId);

        ResumenCorteDto resumen = new ResumenCorteDto(
                nvlLong(resumenRow.getTotalRecibos()),
                nvlMoney(resumenRow.getTotalMonto()),
                nvlLong(resumenRow.getTotalCancelados()),
                nvlMoney(resumenRow.getTotalMontoCancelado())
        );

        List<ResumenPorTipoPagoDto> porTipoPago = reciboRepo.resumenPorTipoPago(fecha, plantelId)
                .stream()
                .map(this::mapTipoPago)
                .toList();

        List<ReciboCorteItemDto> recibos = reciboRepo.findRecibosDelDia(fecha, plantelId)
                .stream()
                .map(this::mapRecibo)
                .toList();

        return new CorteCajaDiarioRes(fecha, plantelId, resumen, porTipoPago, recibos);
    }

    private ResumenPorTipoPagoDto mapTipoPago(CorteTipoPagoRow row) {
        return new ResumenPorTipoPagoDto(
                row.getTipoPagoId(),
                row.getTipoPagoDesc(),
                nvlLong(row.getTotalRecibos()),
                nvlMoney(row.getTotalMonto())
        );
    }

    private ReciboCorteItemDto mapRecibo(Recibo r) {
        // Ajusta alumnoId/nombre según tu entidad Alumno
        String alumnoId = (r.getAlumno() != null) ? String.valueOf(r.getAlumno().getId()) : null;
        String alumnoNombre = (r.getAlumno() != null) ? r.getAlumno().getNombreCompleto() : null;

        boolean cancelado = r.getCanceladoEn() != null;

        return new ReciboCorteItemDto(
                r.getId(),
                r.getFolio(),
                r.getFolioLegacy(),
                r.getFechaEmision(),
                r.getFechaPago(),
                alumnoId,
                alumnoNombre,
                r.getConcepto(),
                r.getMonto(),
                r.getMoneda(),
                r.getEstatus() != null ? r.getEstatus().getId() : null,
                r.getEstatus() != null ? r.getEstatus().getNombre() : null,
                r.getTipoPago() != null ? r.getTipoPago().getId() : null,
                r.getTipoPago() != null ? r.getTipoPago().getName() : null,
                cancelado,
                r.getPlantelId()
        );
    }

    private long nvlLong(Long v) { return v == null ? 0L : v; }
    private java.math.BigDecimal nvlMoney(java.math.BigDecimal v) { return v == null ? java.math.BigDecimal.ZERO : v; }
}
