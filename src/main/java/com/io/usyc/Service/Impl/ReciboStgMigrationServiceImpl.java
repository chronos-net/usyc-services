package com.io.usyc.Service.Impl;

import com.io.usyc.Service.ReciboStgMigrationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReciboStgMigrationServiceImpl implements ReciboStgMigrationService {

    private final JdbcTemplate jdbc;

    private final int estatusPagadoId;
    private final int tipoPagoNdId;

    public ReciboStgMigrationServiceImpl(
            JdbcTemplate jdbc,
            @Value("${usyc.migration.estatus-pagado-id:1}") int estatusPagadoId,
            @Value("${usyc.migration.tipo-pago-nd-id:0}") int tipoPagoNdId
    ) {
        this.jdbc = jdbc;
        this.estatusPagadoId = estatusPagadoId;
        this.tipoPagoNdId = tipoPagoNdId;
    }

    /**
     * Valida si el alumno ya tiene recibos migrados desde staging.
     * Regresa true si existe al menos un recibo con folio_legacy no null.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean alreadyMigratedForAlumno(String alumnoId) {
        String sql = """
            SELECT EXISTS(
              SELECT 1
              FROM recibo r
              WHERE r.alumno_id = ?
                AND r.folio_legacy IS NOT NULL
            )
        """;
        Boolean exists = jdbc.queryForObject(sql, Boolean.class, alumnoId);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Migra recibos desde public.recibo_stg hacia recibo, asignándolos al alumno.
     * Inserta solo registros válidos y evita duplicados usando folio_legacy.
     *
     * @return cantidad de recibos insertados
     */
    @Override
    @Transactional
    public int migrateByNombreToAlumno(String nombre, String alumnoId) {
        if (alumnoId == null || alumnoId.trim().isEmpty()) {
            throw new IllegalArgumentException("alumnoId es obligatorio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("nombre es obligatorio.");
        }

        String nombreKey = normalize(nombre);

        // Si no tienes permisos, quita esto y calcula el hash en Java
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto;");

        String sql = """
        INSERT INTO recibo (
          folio,
          folio_legacy,
          fecha_emision,
          fecha_pago,
          alumno_id,
          plantel_id,
          concepto,
          monto,
          moneda,
          estatus_id,
          qr_payload,
          qr_hash,
          creado_en,
          actualizado_en,
          tipo_pago_id
        )
        SELECT
          -- ✅ folio nuevo sin choque: basado en fecha + folio legacy (y truncado a 40)
          LEFT(
            'USYC-' || to_char(TO_DATE(TRIM(s.fecha_texto), 'DD/MM/YYYY'), 'YYYYMMDD') || '-' || TRIM(s.folio),
            40
          ) AS folio,

          TRIM(s.folio) AS folio_legacy,

          TO_DATE(TRIM(s.fecha_texto), 'DD/MM/YYYY') AS fecha_emision,
          TO_DATE(TRIM(s.fecha_texto), 'DD/MM/YYYY') AS fecha_pago,

          ? AS alumno_id,

          -- ✅ plantel sale del alumno
          s.plantel_id AS plantel_id,

          LEFT(TRIM(s.concepto), 180) AS concepto,

          CAST(REPLACE(REPLACE(TRIM(s.valor_texto), ',', ''), '$', '') AS NUMERIC(12,2)) AS monto,

          'MXN' AS moneda,

          ? AS estatus_id,

          -- qr_payload estable (usa folio legacy)
          (
            'RECIBO|' || TRIM(s.folio) || '|' || ? || '|' ||
            REPLACE(REPLACE(TRIM(s.valor_texto), ',', ''), '$', '') || '|' ||
            to_char(TO_DATE(TRIM(s.fecha_texto), 'DD/MM/YYYY'), 'YYYYMMDD')
          ) AS qr_payload,

          encode(digest(
            'RECIBO|' || TRIM(s.folio) || '|' || ? || '|' ||
            REPLACE(REPLACE(TRIM(s.valor_texto), ',', ''), '$', '') || '|' ||
            to_char(TO_DATE(TRIM(s.fecha_texto), 'DD/MM/YYYY'), 'YYYYMMDD'),
            'sha256'
          ), 'hex') AS qr_hash,

          now(), now(),

          ? AS tipo_pago_id

        FROM public.recibo_stg s
        JOIN alumno a ON a.alumno_id = ?

        WHERE
          -- match por nombre normalizado
          UPPER(REGEXP_REPLACE(TRIM(s.recibido_de), '\\s+', ' ', 'g')) = ?

          -- básicos
          AND TRIM(COALESCE(s.folio,'')) <> ''
          AND TRIM(COALESCE(s.fecha_texto,'')) <> ''
          AND TRIM(COALESCE(s.valor_texto,'')) <> ''
          AND TRIM(COALESCE(s.concepto,'')) <> ''

          -- fecha válida dd/mm/yyyy
          AND TRIM(s.fecha_texto) ~ '^[0-9]{2}/[0-9]{2}/[0-9]{4}$'

          -- monto válido
          AND REPLACE(REPLACE(TRIM(s.valor_texto), ',', ''), '$', '') ~ '^-?[0-9]+(\\.[0-9]+)?$'

          -- evita duplicados por folio_legacy
          AND NOT EXISTS (
            SELECT 1
            FROM recibo r
            WHERE r.folio_legacy = TRIM(s.folio)
          );
    """;

        // Params en orden de aparición:
        // 1) alumno_id (select)
        // 2) estatus_id
        // 3) alumnoId (payload)
        // 4) alumnoId (hash)
        // 5) tipo_pago_id
        // 6) alumnoId (join alumno a)
        // 7) nombreKey (where)
        return jdbc.update(
                sql,
                alumnoId,
                estatusPagadoId,
                alumnoId,
                alumnoId,
                tipoPagoNdId,
                alumnoId,
                nombreKey
        );
    }


    private String normalize(String s) {
        return s.trim().replaceAll("\\s+", " ").toUpperCase();
    }
}
