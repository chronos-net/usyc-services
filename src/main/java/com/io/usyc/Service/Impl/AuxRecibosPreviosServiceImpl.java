package com.io.usyc.Service.Impl;

import com.io.usyc.Dto.AuxRecibosPreviosCountRes;
import com.io.usyc.Repository.AuxRecibosPreviosRepository;
import com.io.usyc.Service.AuxRecibosPreviosService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuxRecibosPreviosServiceImpl implements AuxRecibosPreviosService {

    private final AuxRecibosPreviosRepository repository;

    public AuxRecibosPreviosServiceImpl(AuxRecibosPreviosRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuxRecibosPreviosCountRes countRecibosPreviosByNombre(String nombre) {
        String nombreKey = normalize(nombre);
        long total = repository.countByNombreKey(nombreKey);
        return new AuxRecibosPreviosCountRes(nombreKey, total);
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
