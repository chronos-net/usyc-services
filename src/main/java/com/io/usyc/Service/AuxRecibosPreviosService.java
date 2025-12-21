package com.io.usyc.Service;


import com.io.usyc.Dto.AuxRecibosPreviosCountRes;

public interface AuxRecibosPreviosService {

    AuxRecibosPreviosCountRes countRecibosPreviosByNombre(String nombre);
}
