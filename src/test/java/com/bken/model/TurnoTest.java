package com.bken.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnoTest {

    // Arma un turno con el estado inicial que necesite cada prueba.
    private Turno crearTurno(EstadoTurno estado) {
        Cliente cliente = new Cliente(
                "C001", "Ana Torres", "3001234567", "ana@correo.com", LocalDate.of(2026, 11, 1));
        Especialista especialista = new Especialista(
                "E001", "Laura Gomez", "3007654321", "laura@correo.com",
                "Estetica facial", new ArrayList<>());
        Servicio servicio = new Servicio(
                "S001", "Limpieza facial", new BigDecimal("85000"), 60);
        return new Turno("T001", cliente, especialista, servicio,
                LocalDate.of(2026, 11, 9), LocalTime.of(10, 0), estado);
    }

    @Test
    void confirmarPasaDePendienteAConfirmado() {
        Turno turno = crearTurno(EstadoTurno.PENDIENTE);
        turno.confirmar();
        assertEquals(EstadoTurno.CONFIRMADO, turno.getEstado());
    }

    @Test
    void noSePuedeConfirmarUnTurnoYaConfirmado() {
        Turno turno = crearTurno(EstadoTurno.CONFIRMADO);
        assertThrows(IllegalStateException.class, turno::confirmar);
    }

    @Test
    void noSePuedeConfirmarUnTurnoCancelado() {
        Turno turno = crearTurno(EstadoTurno.CANCELADO);
        assertThrows(IllegalStateException.class, turno::confirmar);
    }

    @Test
    void noSePuedeConfirmarUnTurnoCompletado() {
        Turno turno = crearTurno(EstadoTurno.COMPLETADO);
        assertThrows(IllegalStateException.class, turno::confirmar);
    }

    @Test
    void cancelarPasaDePendienteACancelado() {
        Turno turno = crearTurno(EstadoTurno.PENDIENTE);
        turno.cancelar();
        assertEquals(EstadoTurno.CANCELADO, turno.getEstado());
    }

    @Test
    void cancelarPasaDeConfirmadoACancelado() {
        Turno turno = crearTurno(EstadoTurno.CONFIRMADO);
        turno.cancelar();
        assertEquals(EstadoTurno.CANCELADO, turno.getEstado());
    }

    @Test
    void noSePuedeCancelarUnTurnoCompletado() {
        Turno turno = crearTurno(EstadoTurno.COMPLETADO);
        assertThrows(IllegalStateException.class, turno::cancelar);
    }

    @Test
    void noSePuedeCancelarUnTurnoYaCancelado() {
        Turno turno = crearTurno(EstadoTurno.CANCELADO);
        assertThrows(IllegalStateException.class, turno::cancelar);
    }

      @Test
    void cobrarUnTurnoConfirmadoCreaTransaccionYLoCompleta() {
        Turno turno = crearTurno(EstadoTurno.CONFIRMADO);

        Transaccion transaccion = turno.cobrar("F001", MetodoPago.EFECTIVO, LocalDate.of(2026, 11, 9));

        assertEquals(EstadoTurno.COMPLETADO, turno.getEstado());
        assertEquals(new BigDecimal("85000"), transaccion.getMontoTotal());
        assertEquals(MetodoPago.EFECTIVO, transaccion.getMetodoPago());
        assertSame(turno, transaccion.getTurno());
        assertSame(transaccion, turno.getTransaccion());
    }

    @Test
    void noSePuedeCobrarUnTurnoPendiente() {
        Turno turno = crearTurno(EstadoTurno.PENDIENTE);
        assertThrows(IllegalStateException.class,
                () -> turno.cobrar("F001", MetodoPago.TARJETA, LocalDate.now()));
    }

    @Test
    void noSePuedeCobrarUnTurnoCancelado() {
        Turno turno = crearTurno(EstadoTurno.CANCELADO);
        assertThrows(IllegalStateException.class,
                () -> turno.cobrar("F001", MetodoPago.TARJETA, LocalDate.now()));
    }

    @Test
    void noSePuedeCobrarDosVecesElMismoTurno() {
        Turno turno = crearTurno(EstadoTurno.CONFIRMADO);
        turno.cobrar("F001", MetodoPago.EFECTIVO, LocalDate.now());
        assertThrows(IllegalStateException.class,
                () -> turno.cobrar("F002", MetodoPago.EFECTIVO, LocalDate.now()));
    }



}