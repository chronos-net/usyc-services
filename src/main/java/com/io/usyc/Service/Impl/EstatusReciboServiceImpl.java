package com.io.usyc.Service.Impl;

import com.io.usyc.Domain.CatEstatusRecibo;
import com.io.usyc.Dto.EstatusReciboCreateReq;
import com.io.usyc.Dto.EstatusReciboRes;
import com.io.usyc.Dto.EstatusReciboUpdateReq;
import com.io.usyc.Repository.CatEstatusReciboRepository;
import com.io.usyc.Service.EstatusReciboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EstatusReciboServiceImpl implements EstatusReciboService {

    private final CatEstatusReciboRepository estatusRepo;

    public EstatusReciboServiceImpl(CatEstatusReciboRepository estatusRepo) {
        this.estatusRepo = estatusRepo;
    }

    @Override
    public EstatusReciboRes crear(EstatusReciboCreateReq req) {
        validarTexto(req.codigo(), "codigo");
        validarTexto(req.nombre(), "nombre");

        String codigo = req.codigo().trim().toUpperCase();

        estatusRepo.findByCodigo(codigo).ifPresent(x -> {
            throw new IllegalArgumentException("Ya existe un estatus con código: " + codigo);
        });

        CatEstatusRecibo e = new CatEstatusRecibo();
        e.setCodigo(codigo);
        e.setNombre(req.nombre().trim());

        return toRes(estatusRepo.save(e));
    }

    @Override
    public EstatusReciboRes actualizar(Long id, EstatusReciboUpdateReq req) {
        CatEstatusRecibo e = estatusRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No existe estatus con id: " + id));

        if (req.nombre() != null) {
            validarTexto(req.nombre(), "nombre");
            e.setNombre(req.nombre().trim());
        }

        return toRes(estatusRepo.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public EstatusReciboRes obtener(Long id) {
        return estatusRepo.findById(id)
                .map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe estatus con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public EstatusReciboRes obtenerPorCodigo(String codigo) {
        validarTexto(codigo, "codigo");
        return estatusRepo.findByCodigo(codigo.trim().toUpperCase())
                .map(this::toRes)
                .orElseThrow(() -> new IllegalArgumentException("No existe estatus con código: " + codigo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstatusReciboRes> listar() {
        return estatusRepo.findAll().stream().map(this::toRes).toList();
    }

    private EstatusReciboRes toRes(CatEstatusRecibo e) {
        return new EstatusReciboRes(e.getId(), e.getCodigo(), e.getNombre());
    }

    private static void validarTexto(String v, String campo) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo '" + campo + "' es obligatorio.");
        }
    }
}
