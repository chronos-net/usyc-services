-- USYC: ordinal de línea de proyección liquidada (mismo concepto + periodo).
-- Tabla: recibo. Ejecutar manualmente tras autorización (no Flyway en este repo).

ALTER TABLE recibo
    ADD COLUMN IF NOT EXISTS linea_aplicada integer NULL;

COMMENT ON COLUMN recibo.linea_aplicada IS
    'Ocurrencia de la fila de proyección para mismo concepto y periodo (1, 2, …). NULL en recibos legados.';
