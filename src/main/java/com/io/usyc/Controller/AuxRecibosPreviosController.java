package com.io.usyc.Controller;

import com.io.usyc.Dto.AuxRecibosPreviosCountRes;
import com.io.usyc.Service.AuxRecibosPreviosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aux/recibos-previos")
@Tag(
        name = "Auxiliares - Recibos Previos",
        description = "Consultas contra la base de datos histórica importada (recibo_stg)"
)
public class AuxRecibosPreviosController {

    private final AuxRecibosPreviosService service;

    public AuxRecibosPreviosController(AuxRecibosPreviosService service) {
        this.service = service;
    }
    @Operation(
            summary = "Contar recibos previos por nombre",
            description = """
            Devuelve el número de recibos encontrados en la base de datos anterior
            (tabla recibo_stg) para el nombre proporcionado.
            
            Este endpoint se utiliza para informar al usuario si el alumno
            cuenta con historial previo antes de su registro definitivo.
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Consulta realizada correctamente"
            )
    })
    @GetMapping("/count")
    public AuxRecibosPreviosCountRes count(
            @Parameter(
                    description = "Nombre completo del alumno",
                    example = "MONSERRAT SERRANO NUÑEZ",
                    required = true
            )
            @RequestParam("nombre") String nombre
    ) {
        return service.countRecibosPreviosByNombre(nombre);
    }
}
