package com.io.usyc.Service;


import com.io.usyc.Dto.CorteCajaDiarioRes;
import com.io.usyc.Dto.CorteCajaRangoRes;

import java.time.LocalDate;

public interface CorteCajaService {
    CorteCajaDiarioRes generarCorteDiario(LocalDate fecha, Integer plantelId);

    CorteCajaRangoRes generarCorteRango(LocalDate fechaInicio, LocalDate fechaFin, Integer plantelId);
}