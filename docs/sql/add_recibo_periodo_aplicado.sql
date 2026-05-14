-- USYC: periodo de proyección liquidado por el recibo (formato YYYY-MM).
-- Tabla real del proyecto: recibo (no "recibos").
-- Ejecutar manualmente en cada entorno tras revisión del Mtro. (no Flyway en este repo).

ALTER TABLE recibo
    ADD COLUMN IF NOT EXISTS periodo_aplicado varchar(7) NULL;

COMMENT ON COLUMN recibo.periodo_aplicado IS
    'Periodo de la proyección que liquida el recibo (YYYY-MM). Ej.: 2026-01. NULL = legado / pago sin periodo explícito.';
