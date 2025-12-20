package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CatPlantel;
import com.io.usyc.Dto.*;
import com.io.usyc.Repository.CatPlantelRepository;
import com.io.usyc.Service.PlantelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlantelServiceImpl implements PlantelService {

    private final CatPlantelRepository plantelRepo;

    public PlantelServiceImpl(CatPlantelRepository plantelRepo) {
        this.plantelRepo = plantelRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlantelRes> listar(boolean soloActivos) {
        var list = soloActivos
                ? plantelRepo.findAllByActiveTrueOrderByNameAsc()
                : plantelRepo.findAllByOrderByNameAsc();
        return list.stream().map(this::toRes).toList();
    }

    @Override
    public PlantelRes crear(PlantelCreateReq req) {
        var code = norm(req.code());
        if (plantelRepo.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Ya existe un plantel con code: " + code);
        }
        CatPlantel p = new CatPlantel();
        p.setCode(code);
        p.setName(req.name().trim());
        p.setAddress(req.address() != null ? req.address().trim() : null);
        p.setActive(req.active());
        plantelRepo.save(p);
        return toRes(p);
    }

    @Override
    public PlantelRes actualizar(Integer id, PlantelUpdateReq req) {
        var p = plantelRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plantel no encontrado: " + id));
        p.setName(req.name().trim());
        p.setAddress(req.address() != null ? req.address().trim() : null);
        p.setActive(req.active());
        return toRes(p);
    }

    @Override
    public void desactivar(Integer id) {
        var p = plantelRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plantel no encontrado: " + id));
        p.setActive(false);
    }

    private PlantelRes toRes(CatPlantel p) {
        return new PlantelRes(p.getId(), p.getCode(), p.getName(), p.getAddress(), p.getActive());
    }

    private String norm(String v) {
        if (v == null) throw new IllegalArgumentException("code es requerido");
        return v.trim().toUpperCase();
    }
}
