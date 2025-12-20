package com.io.usyc.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import com.io.usyc.Config.StorageProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class QrCodeService {

    private final StorageProperties storage;

    public QrCodeService(StorageProperties storage) {
        this.storage = storage;
    }

    public String generarYGuardarQr(String folio, String qrPayload) {
        try {
            Path dir = Paths.get(storage.qrDir());
            Files.createDirectories(dir);

            String fileName = "QR_" + folio.replaceAll("[^a-zA-Z0-9\\-]", "_") + ".png";
            Path out = dir.resolve(fileName);

            byte[] png = generarPng(qrPayload, 320, 320);
            Files.write(out, png);

            return fileName; // <-- esto es lo que guardas en BD
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar/guardar el QR.", e);
        }
    }

    public byte[] leerQrComoBytes(String fileName) {
        try {
            Path file = Paths.get(storage.qrDir()).resolve(fileName);
            if (!Files.exists(file)) throw new IllegalArgumentException("No existe el archivo QR: " + fileName);
            return Files.readAllBytes(file);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo leer el QR.", e);
        }
    }

    private static byte[] generarPng(String text, int width, int height) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
            return baos.toByteArray();
        }
    }
}
