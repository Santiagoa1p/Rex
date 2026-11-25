-- CUIDADO: borra todos los clientes, especialistas, horarios, servicios, turnos y pagos.
-- Los usuarios NO se borran, para no perder el acceso al sistema.
-- Uso: antes de una demostracion, ejecutar este archivo y luego datos-ejemplo.sql,
-- para dejar los datos de ejemplo limpios y con los turnos en la fecha de hoy.
TRUNCATE TABLE transacciones, turnos, horarios_disponibilidad, especialistas, servicios, clientes RESTART IDENTITY;
