package com.io.usyc.Controller;

import com.io.usyc.Dto.CarreraCreateReq;
import com.io.usyc.Dto.CarreraRes;
import com.io.usyc.Dto.CarreraUpdateReq;
import com.io.usyc.Service.CarreraService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/carreras")
@Tag(name = "Catálogos - Carreras", description = "Administración del catálogo de carreras y sus montos (mensualidad/inscripción)")
public class CarreraController {

    private final CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @Operation(summary = "Listar carreras", description = "Devuelve el catálogo de carreras. Puede filtrarse por activas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<CarreraRes>> listar(
            @Parameter(description = "Si es true, solo devuelve carreras activas")
            @RequestParam(required = false) Boolean soloActivos
    ) {
        return ResponseEntity.ok(carreraService.listar(soloActivos));
    }

    @Operation(summary = "Listar carreras por escolaridad", description = "Devuelve carreras filtradas por escolaridad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos", content = @Content)
    })
    @GetMapping("/por-escolaridad/{escolaridadId}")
    public ResponseEntity<List<CarreraRes>> listarPorEscolaridad(
            @PathVariable Long escolaridadId,
            @Parameter(description = "Si es true, solo devuelve carreras activas")
            @RequestParam(required = false) Boolean soloActivos
    ) {
        return ResponseEntity.ok(carreraService.listarPorEscolaridad(escolaridadId, soloActivos));
    }

    @Operation(summary = "Obtener carrera por ID", description = "Devuelve una carrera por su ID de 2 dígitos (ej: 01, 10).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera encontrada"),
            @ApiResponse(responseCode = "400", description = "No encontrada o id inválido", content = @Content)
    })
    @GetMapping("/{carreraId}")
    public ResponseEntity<CarreraRes> obtener(@PathVariable String carreraId) {
        return ResponseEntity.ok(carreraService.obtener(carreraId));
    }

    @Operation(
            summary = "Crear carrera",
            description = "Crea una nueva carrera vinculada a una escolaridad, configurando N conceptos con monto y cantidad para proyección."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Carrera creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o id duplicado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CarreraRes> crear(@RequestBody CarreraCreateReq req) {
        CarreraRes res = carreraService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "Actualizar carrera", description = "Actualiza datos de una carrera (nombre, escolaridad, montos, activo).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Carrera actualizada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o no encontrada", content = @Content)
    })
    @PutMapping("/{carreraId}")
    public ResponseEntity<CarreraRes> actualizar(@PathVariable String carreraId, @RequestBody CarreraUpdateReq req) {
        return ResponseEntity.ok(carreraService.actualizar(carreraId, req));
    }

    @Operation(summary = "Activar carrera", description = "Marca una carrera como activa.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrera activada"),
            @ApiResponse(responseCode = "400", description = "No encontrada", content = @Content)
    })
    @PatchMapping("/{carreraId}/activar")
    public ResponseEntity<Void> activar(@PathVariable String carreraId) {
        carreraService.activar(carreraId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desactivar carrera", description = "Marca una carrera como inactiva.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Carrera desactivada"),
            @ApiResponse(responseCode = "400", description = "No encontrada", content = @Content)
    })
    @PatchMapping("/{carreraId}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable String carreraId) {
        carreraService.desactivar(carreraId);
        return ResponseEntity.noContent().build();
    }
}
