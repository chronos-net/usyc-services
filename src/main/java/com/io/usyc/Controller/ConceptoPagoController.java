package com.io.usyc.Controller;

import com.io.usyc.Dto.*;
import com.io.usyc.Service.ConceptoPagoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/conceptos-pago")
@Tag(name = "Catálogos - Conceptos de Pago", description = "Administración del catálogo de conceptos de pago y su tipo de cálculo de monto.")
public class ConceptoPagoController {

    private final ConceptoPagoService conceptoService;

    public ConceptoPagoController(ConceptoPagoService conceptoService) {
        this.conceptoService = conceptoService;
    }

    @Operation(summary = "Listar conceptos", description = "Lista conceptos de pago. Puede filtrar por activos.")
    @ApiResponses(@ApiResponse(responseCode = "200", description = "Listado obtenido"))
    @GetMapping
    public ResponseEntity<List<ConceptoRes>> listar(
            @Parameter(description = "Si es true, solo devuelve conceptos activos")
            @RequestParam(required = false) Boolean soloActivos
    ) {
        return ResponseEntity.ok(conceptoService.listar(soloActivos));
    }

    @Operation(summary = "Obtener concepto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Concepto encontrado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @GetMapping("/{conceptoId}")
    public ResponseEntity<ConceptoRes> obtener(@PathVariable Long conceptoId) {
        return ResponseEntity.ok(conceptoService.obtener(conceptoId));
    }

    @Operation(summary = "Obtener concepto por código", description = "Ej: INSCRIPCION, MENSUALIDAD, CONSTANCIA")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Concepto encontrado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @GetMapping("/por-codigo/{codigo}")
    public ResponseEntity<ConceptoRes> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(conceptoService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Crear concepto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Concepto creado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ConceptoRes> crear(@RequestBody ConceptoCreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(conceptoService.crear(req));
    }

    @Operation(summary = "Actualizar concepto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Concepto actualizado"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PutMapping("/{conceptoId}")
    public ResponseEntity<ConceptoRes> actualizar(@PathVariable Long conceptoId, @RequestBody ConceptoUpdateReq req) {
        return ResponseEntity.ok(conceptoService.actualizar(conceptoId, req));
    }

    @Operation(summary = "Activar concepto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Concepto activado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @PatchMapping("/{conceptoId}/activar")
    public ResponseEntity<Void> activar(@PathVariable Long conceptoId) {
        conceptoService.activar(conceptoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Desactivar concepto")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Concepto desactivado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @PatchMapping("/{conceptoId}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long conceptoId) {
        conceptoService.desactivar(conceptoId);
        return ResponseEntity.noContent().build();
    }
}

