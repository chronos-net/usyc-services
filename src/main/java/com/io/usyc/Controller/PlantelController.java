package com.io.usyc.Controller;

import com.io.usyc.Dto.*;
import com.io.usyc.Service.PlantelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/planteles")
@Tag(name = "Catálogos - Planteles", description = "Administración del catálogo de planteles (crear, actualizar, listar y desactivar).")
public class PlantelController {

    private final PlantelService plantelService;

    public PlantelController(PlantelService plantelService) {
        this.plantelService = plantelService;
    }

    @Operation(
            summary = "Listar planteles",
            description = "Devuelve el listado de planteles. Por defecto devuelve únicamente los activos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public List<PlantelRes> listar(
            @Parameter(description = "Si es true, devuelve solo planteles activos. Default: true", example = "true")
            @RequestParam(defaultValue = "true") boolean soloActivos
    ) {
        return plantelService.listar(soloActivos);
    }

    @Operation(
            summary = "Crear plantel",
            description = "Crea un nuevo plantel con la información enviada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plantel creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos / error de validación", content = @Content)
    })
    @PostMapping
    public PlantelRes crear(
            @Valid @RequestBody PlantelCreateReq req
    ) {
        return plantelService.crear(req);
    }

    @Operation(
            summary = "Actualizar plantel",
            description = "Actualiza los datos de un plantel existente por su id."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plantel actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "No encontrado / datos inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    public PlantelRes actualizar(
            @Parameter(description = "ID del plantel", example = "1", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody PlantelUpdateReq req
    ) {
        return plantelService.actualizar(id, req);
    }

    @Operation(
            summary = "Desactivar plantel",
            description = "Desactiva un plantel (borrado lógico). No se elimina físicamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plantel desactivado correctamente"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public void desactivar(
            @Parameter(description = "ID del plantel", example = "1", required = true)
            @PathVariable Integer id
    ) {
        plantelService.desactivar(id);
    }
}
