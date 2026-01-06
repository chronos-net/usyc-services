package com.io.usyc.Controller;

import com.io.usyc.Dto.CorteCajaDiarioRes;
import com.io.usyc.Service.CorteCajaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes/corte-caja")
@Tag(name = "Reportes - Corte de Caja", description = "Corte de caja por día")
public class CorteCajaController {

    private final CorteCajaService service;

    public CorteCajaController(CorteCajaService service) {
        this.service = service;
    }

    @Operation(summary = "Corte de caja diario", description = "Genera el corte de caja por fechaPago. Puede filtrarse por plantel.")
    @GetMapping
    public ResponseEntity<CorteCajaDiarioRes> corteDiario(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha,
            @RequestParam(required = false) Integer plantelId
    ) {
        return ResponseEntity.ok(service.generarCorteDiario(fecha, plantelId));
    }
}

