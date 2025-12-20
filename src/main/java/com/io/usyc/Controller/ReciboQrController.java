package com.io.usyc.Controller;

import com.io.usyc.Domain.Recibo;
import com.io.usyc.Repository.ReciboRepository;
import com.io.usyc.Service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recibos")
public class ReciboQrController {

    private final ReciboRepository reciboRepo;
    private final QrCodeService qrCodeService;

    public ReciboQrController(ReciboRepository reciboRepo, QrCodeService qrCodeService) {
        this.reciboRepo = reciboRepo;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping(value = "/{reciboId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQr(@PathVariable Long reciboId) {
        Recibo r = reciboRepo.findById(reciboId)
                .orElseThrow(() -> new IllegalArgumentException("No existe recibo con id: " + reciboId));

        if (r.getQrFileName() == null || r.getQrFileName().isBlank()) {
            throw new IllegalArgumentException("El recibo no tiene QR generado.");
        }

        byte[] png = qrCodeService.leerQrComoBytes(r.getQrFileName());
        return ResponseEntity.ok(png);
    }
}
