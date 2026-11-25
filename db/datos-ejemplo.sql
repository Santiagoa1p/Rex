-- Datos de ejemplo de BkenS ERP para demostraciones.
-- Ejecutar DESPUES de schema.sql (por ejemplo en el SQL Editor de Neon).
-- Se puede ejecutar varias veces: no duplica registros (ON CONFLICT DO NOTHING / NOT EXISTS).
-- Las fechas de los turnos son relativas al dia en que se ejecuta (ayer, hoy y manana),
-- para que la agenda de "hoy" siempre tenga turnos.

-- Usuarios de prueba. Contrasena de ambos: clave123
-- (se guarda el hash SHA-256, igual que hace la clase Usuario).
INSERT INTO usuarios (id, nombre_usuario, contrasena_hash, rol, activo) VALUES
    ('U002', 'recepcion', '5ac0852e770506dcd80f1a36d20ba7878bf82244b836d9324593bd14bc56dcb5', 'RECEPCIONISTA', TRUE),
    ('U003', 'laura',     '5ac0852e770506dcd80f1a36d20ba7878bf82244b836d9324593bd14bc56dcb5', 'ESPECIALISTA',  TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO clientes (id, nombre, telefono, correo, fecha_registro) VALUES
    ('C001', 'Ana Torres',     '3001234567', 'ana.torres@correo.com', CURRENT_DATE - 40),
    ('C002', 'Luis Martinez',  '3109876543', NULL,                    CURRENT_DATE - 25),
    ('C003', 'Sofia Ramirez',  '3204567890', 'sofia.r@correo.com',    CURRENT_DATE - 18),
    ('C004', 'Camilo Herrera', '3156781234', 'camilo.h@correo.com',   CURRENT_DATE - 9),
    ('C005', 'Valentina Rios', '3012345678', NULL,                    CURRENT_DATE - 3)
ON CONFLICT DO NOTHING;

INSERT INTO especialistas (id, nombre, telefono, correo, especialidad) VALUES
    ('E001', 'Laura Gomez', '3007654321', 'laura@bkens.com',  'Estetica facial'),
    ('E002', 'Marta Diaz',  '3012223344', 'marta@bkens.com',  'Masajes'),
    ('E003', 'Carlos Ruiz', '3045556677', NULL,               'Barberia')
ON CONFLICT DO NOTHING;

-- Horarios: los tres atienden todos los dias, para que la demostracion funcione cualquier dia.
-- Solo se insertan si el especialista todavia no tiene horarios.
INSERT INTO horarios_disponibilidad (especialista_id, dia, hora_inicio, hora_fin)
SELECT 'E001', dia, TIME '08:00', TIME '18:00'
FROM unnest(ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']) AS dia
WHERE NOT EXISTS (SELECT 1 FROM horarios_disponibilidad WHERE especialista_id = 'E001');

INSERT INTO horarios_disponibilidad (especialista_id, dia, hora_inicio, hora_fin)
SELECT 'E002', dia, TIME '09:00', TIME '19:00'
FROM unnest(ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']) AS dia
WHERE NOT EXISTS (SELECT 1 FROM horarios_disponibilidad WHERE especialista_id = 'E002');

INSERT INTO horarios_disponibilidad (especialista_id, dia, hora_inicio, hora_fin)
SELECT 'E003', dia, TIME '10:00', TIME '18:00'
FROM unnest(ARRAY['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']) AS dia
WHERE NOT EXISTS (SELECT 1 FROM horarios_disponibilidad WHERE especialista_id = 'E003');

INSERT INTO servicios (id_servicio, nombre_servicio, precio, duracion_minutos) VALUES
    ('S001', 'Limpieza facial',     85000, 60),
    ('S002', 'Masaje relajante',   120000, 90),
    ('S003', 'Manicure',            35000, 45),
    ('S004', 'Corte de cabello',    25000, 30),
    ('S005', 'Diseno de cejas',     20000, 30)
ON CONFLICT DO NOTHING;

-- Turnos de ayer (cobrados), hoy (en distintos estados) y manana (por atender).
-- Ningun turno se cruza con otro del mismo especialista o del mismo cliente.
INSERT INTO turnos (id_turno, cliente_id, especialista_id, servicio_id, fecha, hora, estado) VALUES
    ('T001', 'C003', 'E002', 'S002', CURRENT_DATE - 1, '10:00', 'COMPLETADO'),
    ('T002', 'C005', 'E001', 'S001', CURRENT_DATE - 1, '14:00', 'COMPLETADO'),
    ('T003', 'C001', 'E001', 'S001', CURRENT_DATE,     '09:00', 'COMPLETADO'),
    ('T004', 'C002', 'E002', 'S002', CURRENT_DATE,     '10:00', 'COMPLETADO'),
    ('T005', 'C003', 'E001', 'S003', CURRENT_DATE,     '11:00', 'CONFIRMADO'),
    ('T006', 'C004', 'E003', 'S004', CURRENT_DATE,     '14:00', 'PENDIENTE'),
    ('T007', 'C005', 'E002', 'S002', CURRENT_DATE,     '15:00', 'PENDIENTE'),
    ('T008', 'C001', 'E003', 'S004', CURRENT_DATE,     '16:00', 'CANCELADO'),
    ('T009', 'C002', 'E001', 'S005', CURRENT_DATE + 1, '09:00', 'PENDIENTE'),
    ('T010', 'C004', 'E002', 'S002', CURRENT_DATE + 1, '11:00', 'CONFIRMADO')
ON CONFLICT DO NOTHING;

-- Pagos de los turnos completados. El monto es el precio del servicio, como hace Turno.cobrar().
INSERT INTO transacciones (id_factura, turno_id, monto_total, metodo_pago, fecha_pago) VALUES
    ('F001', 'T001', 120000, 'TRANSFERENCIA', CURRENT_DATE - 1),
    ('F002', 'T002',  85000, 'EFECTIVO',      CURRENT_DATE - 1),
    ('F003', 'T003',  85000, 'EFECTIVO',      CURRENT_DATE),
    ('F004', 'T004', 120000, 'TARJETA',       CURRENT_DATE)
ON CONFLICT DO NOTHING;
