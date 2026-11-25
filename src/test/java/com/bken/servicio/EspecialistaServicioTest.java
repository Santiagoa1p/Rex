package com.bken.servicio;

import com.bken.model.Especialista;
import com.bken.model.HorarioDisponibilidad;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EspecialistaServicioTest {
    private EspecialistaServicio servicio;

    @BeforeEach
    void preparar() {
        servicio = new EspecialistaServicio(new DaosEnMemoria.Especialistas());
    }

    private Especialista especialista(HorarioDisponibilidad... horarios) {
        return new Especialista("E001", "Laura Gomez", "3007654321", "laura@correo.com",
                "Estetica facial", new ArrayList<>(List.of(horarios)));
    }

    private HorarioDisponibilidad horario(DayOfWeek dia, int inicio, int fin) {
        return new HorarioDisponibilidad(dia, LocalTime.of(inicio, 0), LocalTime.of(fin, 0));
    }

    @Test
    void registraUnEspecialistaConHorariosValidos() {
        servicio.registrar(especialista(
                horario(DayOfWeek.MONDAY, 8, 12),
                horario(DayOfWeek.MONDAY, 14, 18)));
        assertEquals(2, servicio.buscar("E001").getHorarios().size());
    }

    @Test
    void rechazaUnEspecialistaSinEspecialidad() {
        Especialista especialista = especialista();
        especialista.setEspecialidad(null);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(especialista));
    }

    @Test
    void rechazaUnHorarioQueTerminaAntesDeEmpezar() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(especialista(horario(DayOfWeek.MONDAY, 17, 9))));
    }

    @Test
    void rechazaHorariosQueSeCruzanElMismoDia() {
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(especialista(
                horario(DayOfWeek.MONDAY, 8, 12),
                horario(DayOfWeek.MONDAY, 11, 15))));
    }

    @Test
    void permiteLaMismaFranjaEnDiasDistintos() {
        assertDoesNotThrow(() -> servicio.registrar(especialista(
                horario(DayOfWeek.MONDAY, 8, 12),
                horario(DayOfWeek.TUESDAY, 8, 12))));
    }
}
