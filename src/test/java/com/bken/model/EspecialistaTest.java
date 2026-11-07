package com.bken.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EspecialistaTest {
    // 2026-11-09 es lunes y 2026-11-07 es sabado.
    private static final LocalDate LUNES = LocalDate.of(2026, 11, 9);
    private static final LocalDate SABADO = LocalDate.of(2026, 11, 7);

    private Especialista especialista;

    @BeforeEach
    void preparar() {
        List<HorarioDisponibilidad> horarios = new ArrayList<>();
        horarios.add(new HorarioDisponibilidad(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        especialista = new Especialista(
                "E001", "Laura Gomez", "3007654321", "laura@correo.com",
                "Estetica facial", horarios);
    }

    @Test
    void estaDisponibleDentroDelHorario() {
        assertTrue(especialista.estaDisponible(LUNES, LocalTime.of(10, 0), 60));
    }

    @Test
    void estaDisponibleSiTerminaExactamenteAlCierre() {
        assertTrue(especialista.estaDisponible(LUNES, LocalTime.of(16, 0), 60));
    }

    @Test
    void noEstaDisponibleSiEmpiezaAntesDeAbrir() {
        assertFalse(especialista.estaDisponible(LUNES, LocalTime.of(8, 30), 60));
    }

    @Test
    void noEstaDisponibleSiTerminaDespuesDelCierre() {
        assertFalse(especialista.estaDisponible(LUNES, LocalTime.of(16, 30), 60));
    }

    @Test
    void noEstaDisponibleUnDiaSinHorario() {
        assertFalse(especialista.estaDisponible(SABADO, LocalTime.of(10, 0), 60));
    }

    @Test
    void noEstaDisponibleSiNoTieneHorarios() {
        Especialista sinHorarios = new Especialista(
                "E002", "Sin Horario", "3000000000", "x@correo.com",
                "Masajes", new ArrayList<>());
        assertFalse(sinHorarios.estaDisponible(LUNES, LocalTime.of(10, 0), 60));
    }

    @Test
    void noEstaDisponibleSiElServicioCruzaLaMedianoche() {
        assertFalse(especialista.estaDisponible(LUNES, LocalTime.of(23, 30), 60));
    }
}