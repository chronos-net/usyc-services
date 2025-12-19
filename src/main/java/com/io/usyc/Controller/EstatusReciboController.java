package com.io.usyc.Controller;

import com.io.usyc.Dto.EstatusReciboCreateReq;
import com.io.usyc.Dto.EstatusReciboRes;
import com.io.usyc.Dto.EstatusReciboUpdateReq;
import com.io.usyc.Service.EstatusReciboService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/estatus-recibo")
@Tag(name = "Catálogos - Estatus de Recibo", description = "Administración del catálogo de estatus del recibo (EMITIDO, PAGADO, CANCELADO)")
public class EstatusReciboController {

    private final EstatusReciboService estatusReciboService;

    public EstatusReciboController(EstatusReciboService estatusReciboService) {
        this.estatusReciboService = estatusReciboService;
    }

    @Operation(summary = "Listar estatus de recibo", description = "Devuelve el catálogo completo de estatus.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente")
    })
    @GetMapping
    public ResponseEntity<List<EstatusReciboRes>> listar() {
        return ResponseEntity.ok(estatusReciboService.listar());
    }

    @Operation(summary = "Obtener estatus por ID", description = "Devuelve un estatus por su identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatus encontrado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EstatusReciboRes> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(estatusReciboService.obtener(id));
    }

    @Operation(summary = "Obtener estatus por código", description = "Devuelve un estatus por su código (EMITIDO, PAGADO, CANCELADO).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatus encontrado"),
            @ApiResponse(responseCode = "400", description = "No encontrado", content = @Content)
    })
    @GetMapping("/por-codigo/{codigo}")
    public ResponseEntity<EstatusReciboRes> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(estatusReciboService.obtenerPorCodigo(codigo));
    }

    @Operation(summary = "Crear estatus", description = "Crea un nuevo estatus de recibo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estatus creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o código duplicado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EstatusReciboRes> crear(@RequestBody EstatusReciboCreateReq req) {
        EstatusReciboRes res = estatusReciboService.crear(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(summary = "Actualizar estatus", description = "Actualiza el nombre de un estatus de recibo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatus actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "No encontrado o datos inválidos", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<EstatusReciboRes> actualizar(@PathVariable Long id, @RequestBody EstatusReciboUpdateReq req) {
        return ResponseEntity.ok(estatusReciboService.actualizar(id, req));
    }
}

