package com.io.usyc.Service;


import com.io.usyc.Dto.CorteCajaDiarioRes;

import java.time.LocalDate;

public interface CorteCajaService {
    CorteCajaDiarioRes generarCorteDiario(LocalDate fecha, Integer plantelId);
}