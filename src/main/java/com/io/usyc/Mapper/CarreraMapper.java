package com.io.usyc.Mapper;


import com.io.usyc.Domain.CarreraConceptoConfig;
import com.io.usyc.Domain.CatCarrera;
import com.io.usyc.Dto.CarreraConceptoConfigRes;
import com.io.usyc.Dto.CarreraRes;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CarreraMapper {

    public static CarreraRes toRes(CatCarrera c, List<CarreraConceptoConfig> configs) {

        BigDecimal total = configs.stream()
                .filter(x -> Boolean.TRUE.equals(x.getActivo()))
                .map(x -> x.getMonto().multiply(BigDecimal.valueOf(x.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CarreraConceptoConfigRes> conceptos = configs.stream()
                .map(x -> new CarreraConceptoConfigRes(
                        x.getConcepto().getId(),
                        x.getConcepto().getCodigo(),
                        x.getConcepto().getNombre(),
                        x.getMonto(),
                        x.getCantidad(),
                        x.getActivo()
                ))
                .toList();

        return new CarreraRes(
                c.getId(),
                c.getEscolaridad().getId(),
                c.getEscolaridad().getNombre(),
                c.getNombre(),
                c.getDuracionAnios(),
                c.getDuracionMeses(),
                c.getActivo(),
                total,
                conceptos
        );
    }
}