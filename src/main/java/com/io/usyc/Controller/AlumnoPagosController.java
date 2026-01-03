package com.io.usyc.Controller;

import com.io.usyc.Dto.AlumnoPagosResumenRes;
import com.io.usyc.Dto.ReciboRes;
import com.io.usyc.Service.AlumnoPagosService;
import com.io.usyc.Service.SecurityUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alumnos")
@Tag(name = "Alumnos - Pagos", description = "Consulta de pagos reales y proyección de pagos pendientes según carrera.")
public class AlumnoPagosController {

    @Autowired
    private AlumnoPagosService alumnoPagosService;


    @Operation(
            summary = "Pagos reales + Proyección de pagos restantes",
            description = "Devuelve todos los pagos registrados (recibos no cancelados) y la proyección mensual desde fecha de ingreso " +
                    "hasta la fecha de término calculada por la duración de la carrera (años + meses)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Alumno no encontrado o datos inválidos", content = @Content)
    })
    @GetMapping("/{alumnoId}/pagos-resumen")
    public ResponseEntity<AlumnoPagosResumenRes> obtenerResumen(
            @Parameter(description = "ID del alumno AAAACCNNN", example = "202501001")
            @PathVariable String alumnoId
    ) {
        return ResponseEntity.ok(alumnoPagosService.obtenerResumen(alumnoId));
    }


    @Operation(
            summary = "Obtener pagos (recibos)",
            description = "Regresa todos los recibos visibles para el usuario autenticado. " +
                    "Si el usuario tiene plantelId=null (admin), regresa todos. Si no, filtra por plantel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    @GetMapping("/filter")
    public ResponseEntity<List<ReciboRes>> obtenerPagos(
            @AuthenticationPrincipal SecurityUserDetails principal
    ) {
        return ResponseEntity.ok(alumnoPagosService.obtenerPagos(principal));
    }


}

