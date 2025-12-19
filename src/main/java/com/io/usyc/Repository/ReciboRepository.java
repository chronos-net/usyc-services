package com.io.usyc.Repository;

import com.io.usyc.Domain.Recibo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReciboRepository extends JpaRepository<Recibo, Long> {
    java.util.Optional<Recibo> findByQrPayload(String qrPayload);
    java.util.Optional<Recibo> findByFolio(String folio);
}