package com.io.usyc.Controller;

import com.io.usyc.Dto.AlumnoListItemRes;
import com.io.usyc.Service.AlumnoCatalogoService;
import com.io.usyc.Service.SecurityUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/alumnos")
@Tag(name = "Alumnos", description = "Consulta de alumnos (paginado).")
public class AlumnoCatalogoController {

    private final AlumnoCatalogoService alumnoCatalogoService;

    public AlumnoCatalogoController(AlumnoCatalogoService alumnoCatalogoService) {
        this.alumnoCatalogoService = alumnoCatalogoService;
    }

    @Operation(
            summary = "Listar alumnos (paginado)",
            description = """
        Devuelve alumnos paginados.
        Parámetros típicos:
        - page: página (0..n)
        - size: tamaño (ej: 10, 20, 50)
        - sort: campo,dir (ej: nombreCompleto,asc)
        Ejemplo:
        /api/alumnos?page=0&size=10&sort=nombreCompleto,asc
      """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página generada correctamente")
    })
    @GetMapping
    public ResponseEntity<Page<AlumnoListItemRes>> listar(
            @ParameterObject Pageable pageable,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer escolaridadId,
            @RequestParam(required = false) Integer plantelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIngresoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaIngresoHasta,
            @AuthenticationPrincipal Object principal
    ) {
        SecurityUserDetails sud = (SecurityUserDetails) principal;
        return ResponseEntity.ok(alumnoCatalogoService.listar(
                pageable,
                sud,
                q,
                escolaridadId,
                plantelId,
                fechaIngresoDesde,
                fechaIngresoHasta
        ));
    }


}
