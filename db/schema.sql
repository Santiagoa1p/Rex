-- Esquema de BkenS ERP para PostgreSQL.
-- Ejecutar una vez sobre la base de datos (por ejemplo en el SQL Editor de Neon o con psql -f db/schema.sql).
-- Se puede volver a ejecutar sin error: solo crea las tablas que no existan.

-- Personas: Cliente y Especialista heredan de Persona, cada una en su propia tabla.
CREATE TABLE IF NOT EXISTS clientes (
    id              VARCHAR(20)  PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    telefono        VARCHAR(20),
    correo          VARCHAR(100),
    fecha_registro  DATE         NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS especialistas (
    id              VARCHAR(20)  PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    telefono        VARCHAR(20),
    correo          VARCHAR(100),
    especialidad    VARCHAR(100) NOT NULL
);

-- Un especialista tiene varios horarios. Si se elimina el especialista, se eliminan sus horarios.
CREATE TABLE IF NOT EXISTS horarios_disponibilidad (
    id               SERIAL      PRIMARY KEY,
    especialista_id  VARCHAR(20) NOT NULL REFERENCES especialistas (id) ON DELETE CASCADE,
    dia              VARCHAR(10) NOT NULL
        CHECK (dia IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
    hora_inicio      TIME        NOT NULL,
    hora_fin         TIME        NOT NULL,
    CHECK (hora_fin > hora_inicio)
);

CREATE TABLE IF NOT EXISTS servicios (
    id_servicio       VARCHAR(20)   PRIMARY KEY,
    nombre_servicio   VARCHAR(100)  NOT NULL,
    precio            NUMERIC(12, 2) NOT NULL CHECK (precio >= 0),
    duracion_minutos  INTEGER       NOT NULL CHECK (duracion_minutos > 0)
);

-- Un turno no se puede borrar mientras exista el cliente, especialista o servicio (RESTRICT por defecto).
CREATE TABLE IF NOT EXISTS turnos (
    id_turno         VARCHAR(20) PRIMARY KEY,
    cliente_id       VARCHAR(20) NOT NULL REFERENCES clientes (id),
    especialista_id  VARCHAR(20) NOT NULL REFERENCES especialistas (id),
    servicio_id      VARCHAR(20) NOT NULL REFERENCES servicios (id_servicio),
    fecha            DATE        NOT NULL,
    hora             TIME        NOT NULL,
    estado           VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE'
        CHECK (estado IN ('PENDIENTE', 'CONFIRMADO', 'CANCELADO', 'COMPLETADO'))
);

-- Cada turno se cobra una sola vez (turno_id UNIQUE).
CREATE TABLE IF NOT EXISTS transacciones (
    id_factura   VARCHAR(20)    PRIMARY KEY,
    turno_id     VARCHAR(20)    NOT NULL UNIQUE REFERENCES turnos (id_turno),
    monto_total  NUMERIC(12, 2) NOT NULL CHECK (monto_total >= 0),
    metodo_pago  VARCHAR(15)    NOT NULL
        CHECK (metodo_pago IN ('EFECTIVO', 'TARJETA', 'TRANSFERENCIA')),
    fecha_pago   DATE           NOT NULL
);

-- La contrasena se guarda como hash SHA-256 en hexadecimal (64 caracteres), nunca en texto plano.
CREATE TABLE IF NOT EXISTS usuarios (
    id               VARCHAR(20) PRIMARY KEY,
    nombre_usuario   VARCHAR(50) NOT NULL UNIQUE,
    contrasena_hash  CHAR(64)    NOT NULL,
    rol              VARCHAR(15) NOT NULL
        CHECK (rol IN ('ADMINISTRADOR', 'RECEPCIONISTA', 'ESPECIALISTA')),
    activo           BOOLEAN     NOT NULL DEFAULT TRUE
);

-- Consultas frecuentes: turnos del dia (TurnoDao.listarPorFecha) y agenda de un especialista.
CREATE INDEX IF NOT EXISTS idx_turnos_fecha ON turnos (fecha);
CREATE INDEX IF NOT EXISTS idx_turnos_especialista_fecha ON turnos (especialista_id, fecha);
