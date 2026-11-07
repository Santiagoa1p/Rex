# BkenS ERP

## Contexto

BkenS ERP es un proyecto academico individual para apoyar la gestion basica de un centro de estetica y servicios especializados. El sistema organiza informacion de clientes, especialistas, servicios, turnos y transacciones en un modelo orientado a objetos.

## Problema y objetivo

La administracion manual de las citas y los pagos puede dificultar el seguimiento de la agenda, la disponibilidad del personal y los ingresos. El objetivo del proyecto es ofrecer una base modular para registrar y consultar estos datos de forma sencilla.

## Alcance inicial

- Modelar clientes y especialistas a partir de los datos comunes de una persona.
- Representar servicios, turnos y transacciones con sus relaciones.
- Preparar las entidades para persistencia local, sin motor de base de datos.

La interfaz de usuario, las reglas de negocio, la agenda operativa y la capa de lectura y escritura de archivos quedan para siguientes incrementos.


## Modelo de dominio

```mermaid
classDiagram
    class Persona {
        <<abstract>>
        -String id
        -String nombre
        -String telefono
        -String correo
        #Persona()
        #Persona(id, nombre, telefono, correo)
    }
    class Cliente {
        -LocalDate fechaRegistro
        +Cliente()
        +Cliente(id, nombre, telefono, correo, fechaRegistro)
    }
    class Especialista {
        -String especialidad
        -List~HorarioDisponibilidad~ horarios
        +Especialista()
        +Especialista(id, nombre, telefono, correo, especialidad, horarios)
        +estaDisponible(LocalDate fecha, LocalTime hora, int duracionMinutos) boolean
    }
    class HorarioDisponibilidad {
        -DayOfWeek dia
        -LocalTime horaInicio
        -LocalTime horaFin
        +HorarioDisponibilidad()
        +HorarioDisponibilidad(dia, horaInicio, horaFin)
    }
    class Servicio {
        -String idServicio
        -String nombreServicio
        -BigDecimal precio
        -int duracionMinutos
        +Servicio()
        +Servicio(idServicio, nombreServicio, precio, duracionMinutos)
    }
    class Turno {
        -String idTurno
        -Cliente cliente
        -Especialista especialista
        -Servicio servicio
        -LocalDate fecha
        -LocalTime hora
        -EstadoTurno estado
        -Transaccion transaccion
        +Turno()
        +Turno(idTurno, cliente, especialista, servicio, fecha, hora, estado)
        +confirmar() void
        +cancelar() void
        +cobrar(String idFactura, MetodoPago metodoPago, LocalDate fechaPago) Transaccion
    }
    class Transaccion {
        -String idFactura
        -Turno turno
        -BigDecimal montoTotal
        -MetodoPago metodoPago
        -LocalDate fechaPago
        +Transaccion()
        +Transaccion(idFactura, turno, montoTotal, metodoPago, fechaPago)
    }
    class EstadoTurno {
        <<enumeration>>
        PENDIENTE
        CONFIRMADO
        CANCELADO
        COMPLETADO
    }
    class MetodoPago {
        <<enumeration>>
        EFECTIVO
        TARJETA
        TRANSFERENCIA
    }
    class Usuario {
        -String id
        -String nombreUsuario
        -String contrasenaHash
        -Rol rol
        -boolean activo
        +autenticar(String nombreUsuario, String contrasena) boolean
        +tieneRol(Rol rol) boolean
    }
    class Rol {
        <<enumeration>>
        ADMINISTRADOR
        RECEPCIONISTA
        ESPECIALISTA
    }
    class Dao~T~ {
        <<interface>>
        +guardar(T entidad) void
        +buscarPorId(String id) Optional~T~
        +listar() List~T~
        +eliminar(String id) void
    }
    class TurnoDao {
        <<interface>>
        +listarPorFecha(LocalDate fecha) List~Turno~
    }
    class UsuarioDao {
        <<interface>>
        +buscarPorNombreUsuario(String nombreUsuario) Optional~Usuario~
    }




    Persona <|-- Cliente
    Persona <|-- Especialista
    Especialista "1" *-- "0..*" HorarioDisponibilidad : tiene
    Cliente "1" -- "0..*" Turno : agenda
    Especialista "1" -- "0..*" Turno : atiende
    Servicio "1" -- "0..*" Turno : corresponde
    Turno "1" -- "0..1" Transaccion : se cobra mediante
    Turno --> EstadoTurno
    Transaccion --> MetodoPago
    Usuario --> Rol
    Dao~T~ <|-- TurnoDao
    Dao~T~ <|-- UsuarioDao
    TurnoDao ..> Turno : gestiona
    UsuarioDao ..> Usuario : gestiona
```

## Decisiones de diseno

- `Persona` concentra los datos comunes y es abstracta; `Cliente` y `Especialista` especializan ese contrato mediante herencia.
- Los atributos son privados y se exponen mediante constructores, getters y setters.
- `LocalDate` y `LocalTime` representan fechas y horas; `BigDecimal` representa importes monetarios.
- `Turno` mantiene referencias a sus entidades relacionadas, y `Transaccion` referencia el turno cobrado.
- `EstadoTurno` y `MetodoPago` son enumeraciones: restringen los valores posibles y evitan errores de texto libre.
- `HorarioDisponibilidad` es una clase propia (día, hora de inicio, hora de fin) para que `Especialista.estaDisponible()` pueda evaluar disponibilidad en vez de interpretar un texto.
- `Turno` controla su ciclo de vida con `confirmar()`, `cancelar()` y `cobrar()`; solo `cobrar()` crea la `Transaccion`, con el precio del servicio.
- `Usuario` y `Rol` controlan el acceso; la contraseña se guarda como hash SHA-256, nunca en texto plano.
- La persistencia se separa en interfaces DAO (`Dao<T>` y una por entidad: `ClienteDao`, `EspecialistaDao`, `ServicioDao`, `TurnoDao`, `TransaccionDao`, `UsuarioDao`). Hoy se implementarán sobre archivos y en la siguiente entrega sobre base de datos, sin tocar el modelo.
- El nombre "ERP" se justifica porque el sistema integra en un solo modelo agenda, personal, catálogo de servicios, facturación y control de acceso. Hoy el núcleo es el agendamiento de turnos; inventario y reportes son módulos futuros.