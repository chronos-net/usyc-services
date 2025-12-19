package com.io.usyc.Controller;

import com.io.usyc.Dto.ReciboCrearReq;
import com.io.usyc.Dto.ReciboRes;
import com.io.usyc.Dto.ReciboValidacionRes;
import com.io.usyc.Service.ReciboService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recibos")
@Tag(name = "Recibos", description = "Registro de pagos (emisión de recibos), cancelación y verificación por QR.")
public class ReciboController {

    private final ReciboService reciboService;

    public ReciboController(ReciboService reciboService) {
        this.reciboService = reciboService;
    }

    @Operation(
            summary = "Registrar pago / emitir recibo",
            description = "Registra un pago y emite un recibo. Si el concepto es INSCRIPCION o MENSUALIDAD, el monto se toma de la carrera. " +
                    "Si el concepto es OTRO, se requiere montoManual."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recibo creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ReciboRes> registrar(@RequestBody ReciboCrearReq req) {
        ReciboRes res = reciboService.registrarPago(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @Operation(
            summary = "Validar recibo por QR",
            description = "Valida la autenticidad del recibo a partir del qrPayload. Devuelve: VALIDO, CANCELADO, NO_ENCONTRADO o ALTERADO."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validación realizada"),
            @ApiResponse(responseCode = "400", description = "QR inválido", content = @Content)
    })
    @GetMapping("/validar-qr")
    public ResponseEntity<ReciboValidacionRes> validarQr(
            @Parameter(description = "Contenido del QR (qrPayload)", required = true)
            @RequestParam String qrPayload
    ) {
        return ResponseEntity.ok(reciboService.validarQr(qrPayload));
    }

    @Operation(
            summary = "Cancelar recibo",
            description = "Cancela un recibo, marcándolo como CANCELADO y guardando motivo. No se elimina."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Recibo cancelado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
    })
    @PostMapping("/{reciboId}/cancelar")
    public ResponseEntity<ReciboRes> cancelar(
            @PathVariable Long reciboId,
            @RequestParam String motivo
    ) {
        return ResponseEntity.ok(reciboService.cancelar(reciboId, motivo));
    }
}
