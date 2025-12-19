package com.io.usyc.Controller;

import com.io.usyc.Dto.EscolaridadCreateReq;
import com.io.usyc.Dto.EscolaridadRes;
import com.io.usyc.Dto.EscolaridadUpdateReq;
import com.io.usyc.Service.EscolaridadService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/escolaridades")
@Tag(name = "Catálogos - Escolaridades", description = "Administración del catálogo de escolaridades (SEC, BACH, LIC, etc.)")
public class EscolaridadController {

    private final EscolaridadService escolaridadService;

    public EscolaridadController(EscolaridadService escolaridadService) {
        this.escolaridadService = escolaridadService;
    }

    @Operation(summary = "Listar escolaridades", description = "Devuelve el catálogo de escolaridades. Puede filtrarse por activas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<EscolaridadRes>> listar(
            @Parameter(description = "Si es true, solo devuelve escolaridades activas")
            @RequestParam(required = false) Boolean soloActivos
    ) {
        return ResponseEntity.ok(escolaridadService.listar(soloActivos));
    }

    @Operation(summary = "Obtener escolaridad por ID", description = "Devuelve una escolaridad por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Escolaridad encontrada"),
            @ApiResponse(responseCode = "400", description = "ID inválido o no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EscolaridadRes> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(escolaridadService.obtener(id));
    }

    @Operation(summary = "Obtener escolaridad por código", description = "Devuelve una escolaridad por su código (ej: LIC).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Escolaridad encontrada"),
            @ApiResponse(responseCode = "400", description = "Código inválido o no encontrada", content = @Content)
    })
    @GetMapping("/por-codigo/{codigo}")
    public ResponseEntity<EscolaridadRes> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(escolaridadService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Crear escolaridad", description = "Crea una nueva escolaridad en el catálogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Escolaridad creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o código duplicado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EscolaridadRes> crear(@RequestBody EscolaridadCreateReq req) {
        EscolaridadRes res = escolaridadService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "Actualizar escolaridad", description = "Actualiza nombre/activo de una escolaridad existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Escolaridad actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EscolaridadRes> actualizar(@PathVariable Long id, @RequestBody EscolaridadUpdateReq req) {
        return ResponseEntity.ok(escolaridadService.actualizar(id, req));
    }

    @Operation(summary = "Activar escolaridad", description = "Marca una escolaridad como activa.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Escolaridad activada"),
            @ApiResponse(responseCode = "400", description = "No encontrada", content = @Content)
    })
    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long id) {
        escolaridadService.activar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desactivar escolaridad", description = "Marca una escolaridad como inactiva.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Escolaridad desactivada"),
            @ApiResponse(responseCode = "400", description = "No encontrada", content = @Content)
    })
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        escolaridadService.desactivar(id);
        return ResponseEntity.noContent().build();
    }
}
