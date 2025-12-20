package com.io.usyc.Controller;

import com.io.usyc.Dto.*;
import com.io.usyc.Service.PlantelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogos/planteles")
public class PlantelController {

    private final PlantelService plantelService;

    public PlantelController(PlantelService plantelService) {
        this.plantelService = plantelService;
    }

    @GetMapping
    public List<PlantelRes> listar(@RequestParam(defaultValue = "true") boolean soloActivos) {
        return plantelService.listar(soloActivos);
    }

    @PostMapping
    public PlantelRes crear(@Valid @RequestBody PlantelCreateReq req) {
        return plantelService.crear(req);
    }

    @PutMapping("/{id}")
    public PlantelRes actualizar(@PathVariable Integer id, @Valid @RequestBody PlantelUpdateReq req) {
        return plantelService.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public void desactivar(@PathVariable Integer id) {
        plantelService.desactivar(id);
    }
}
