package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CatTipoPago;
import com.io.usyc.Dto.TipoPagoCreateReq;
import com.io.usyc.Dto.TipoPagoRes;
import com.io.usyc.Dto.TipoPagoUpdateReq;
import com.io.usyc.Repository.CatTipoPagoRepository;
import com.io.usyc.Service.TipoPagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoPagoServiceImpl implements TipoPagoService {

    private final CatTipoPagoRepository tipoPagoRepo;

    public TipoPagoServiceImpl(CatTipoPagoRepository tipoPagoRepo) {
        this.tipoPagoRepo = tipoPagoRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoPagoRes> listar(boolean soloActivos) {
        var list = soloActivos
                ? tipoPagoRepo.findAllByActiveTrueOrderByNameAsc()
                : tipoPagoRepo.findAllByOrderByNameAsc();

        return list.stream().map(this::toRes).toList();
    }

    @Override
    public TipoPagoRes crear(TipoPagoCreateReq req) {
        var code = normalizarCode(req.code());

        if (tipoPagoRepo.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Ya existe un tipo de pago con code: " + code);
        }

        CatTipoPago e = new CatTipoPago();
        e.setCode(code);
        e.setName(req.name().trim());
        e.setActive(req.active());

        tipoPagoRepo.save(e);
        return toRes(e);
    }

    @Override
    public TipoPagoRes actualizar(Integer id, TipoPagoUpdateReq req) {
        var e = tipoPagoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de pago no encontrado: " + id));

        e.setName(req.name().trim());
        e.setActive(req.active());

        return toRes(e);
    }

    @Override
    public void desactivar(Integer id) {
        var e = tipoPagoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de pago no encontrado: " + id));
        e.setActive(false);
    }

    private TipoPagoRes toRes(CatTipoPago e) {
        return new TipoPagoRes(e.getId(), e.getCode(), e.getName(), e.getActive());
    }

    private String normalizarCode(String code) {
        if (code == null) throw new IllegalArgumentException("code es requerido");
        return code.trim().toUpperCase();
    }
}
