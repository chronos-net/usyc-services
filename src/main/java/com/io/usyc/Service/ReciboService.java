package com.io.usyc.Service;

import com.io.usyc.Dto.ReciboCrearReq;
import com.io.usyc.Dto.ReciboRes;
import com.io.usyc.Dto.ReciboValidacionRes;

public interface ReciboService {
    ReciboRes registrarPago(ReciboCrearReq req);
    ReciboValidacionRes validarQr(String qrPayload);
    ReciboRes cancelar(Long reciboId, String motivo);
}
