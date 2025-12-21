package com.io.usyc.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuxRecibosPreviosRepository {

    private final JdbcTemplate jdbc;

    public AuxRecibosPreviosRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long countByNombreKey(String nombreKey) {
        String sql = """
                SELECT COUNT(*)
                FROM public.recibo_stg
                WHERE UPPER(REGEXP_REPLACE(TRIM(recibido_de), '\\s+', ' ', 'g')) = ?
        """;
        Long count = jdbc.queryForObject(sql, Long.class, nombreKey);
        return (count != null) ? count : 0L;
    }
}
