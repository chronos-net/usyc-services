package com.io.usyc.Controller;

import com.io.usyc.Dto.TipoPagoCreateReq;
import com.io.usyc.Dto.TipoPagoRes;
import com.io.usyc.Dto.TipoPagoUpdateReq;
import com.io.usyc.Service.TipoPagoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/tipos-pago")
public class TipoPagoController {

    private final TipoPagoService tipoPagoService;

    public TipoPagoController(TipoPagoService tipoPagoService) {
        this.tipoPagoService = tipoPagoService;
    }

    @GetMapping
    public List<TipoPagoRes> listar(
            @RequestParam(name = "soloActivos", defaultValue = "true") boolean soloActivos
    ) {
        return tipoPagoService.listar(soloActivos);
    }

    @PostMapping
    public TipoPagoRes crear(@Valid @RequestBody TipoPagoCreateReq req) {
        return tipoPagoService.crear(req);
    }

    @PutMapping("/{id}")
    public TipoPagoRes actualizar(@PathVariable Integer id, @Valid @RequestBody TipoPagoUpdateReq req) {
        return tipoPagoService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public void desactivar(@PathVariable Integer id) {
        tipoPagoService.desactivar(id);
    }
}
