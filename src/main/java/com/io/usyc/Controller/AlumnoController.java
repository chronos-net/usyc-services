package com.io.usyc.Controller;

import com.io.usyc.Dto.AlumnoCreateReq;
import com.io.usyc.Dto.AlumnoRes;
import com.io.usyc.Dto.AlumnoUpdateReq;
import com.io.usyc.Service.AlumnoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alumnos")
@Tag(name = "Alumnos", description = "Alta y consulta de alumnos. Genera alumno_id con formato AAAACCNNN (año+carrera+consecutivo).")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @Operation(
            summary = "Crear alumno",
            description = "Crea un alumno y genera automáticamente el alumno_id con formato AAAACCNNN. " +
                    "Ejemplo: 202501001 (2025 + carrera 01 + consecutivo 001)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Alumno creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AlumnoRes> crear(@RequestBody AlumnoCreateReq req) {
        AlumnoRes res = alumnoService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(
            summary = "Obtener alumno por ID",
            description = "Obtiene el detalle básico del alumno por su alumno_id (AAAA CC NNN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alumno encontrado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @GetMapping("/{alumnoId}")
    public ResponseEntity<AlumnoRes> obtener(
            @Parameter(description = "ID del alumno con formato AAAACCNNN", example = "202501001")
            @PathVariable String alumnoId
    ) {
        return ResponseEntity.ok(alumnoService.obtener(alumnoId));
    }


    @Operation(summary = "Actualizar alumno", description = "Actualiza la información del alumno (sin cambiar el alumno_id).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Alumno actualizado"),
            @ApiResponse(responseCode = "400", description = "Validación/duplicado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Alumno / catálogo no encontrado", content = @Content)
    })
    @PutMapping(value = "/update/{alumnoId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlumnoRes> actualizar(@PathVariable String alumnoId,
                                                @RequestBody AlumnoUpdateReq req) {
        return ResponseEntity.ok(alumnoService.actualizar(alumnoId, req));
    }
}
