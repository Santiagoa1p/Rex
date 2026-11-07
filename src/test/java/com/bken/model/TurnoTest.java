package com.bken.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}