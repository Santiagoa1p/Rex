package com.bken.servicio;

import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.EstadoTurno;
import com.bken.model.HorarioDisponibilidad;
import com.bken.model.MetodoPago;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TurnoServicioTest {
    // Siempre un lunes futuro, para que la prueba no dependa del dia en que se ejecuta.
    private static final LocalDate LUNES = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    private TurnoServicio servicio;

    @BeforeEach
    void preparar() {
        DaosEnMemoria.Clientes clientes = new DaosEnMemoria.Clientes();
        DaosEnMemoria.Especialistas especialistas = new DaosEnMemoria.Especialistas();
        DaosEnMemoria.Servicios servicios = new DaosEnMemoria.Servicios();

        clientes.guardar(new Cliente("C001", "Ana", null, null, LocalDate.of(2026, 11, 1)));
        clientes.guardar(new Cliente("C002", "Luis", null, null, LocalDate.of(2026, 11, 1)));
        List<HorarioDisponibilidad> lunesDe9a17 = new ArrayList<>(List.of(
                new HorarioDisponibilidad(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0))));
        especialistas.guardar(new Especialista("E001", "Laura", null, null, "Facial", lunesDe9a17));
        especialistas.guardar(new Especialista("E002", "Marta", null, null, "Masajes", new ArrayList<>(lunesDe9a17)));
        servicios.guardar(new Servicio("S001", "Limpieza facial", new BigDecimal("85000"), 60));

        servicio = new TurnoServicio(new DaosEnMemoria.Turnos(), clientes, especialistas, servicios);
    }

    private void agendar(String id, String cliente, String especialista, int hora, int minuto) {
        servicio.agendar(id, cliente, especialista, "S001", LUNES, LocalTime.of(hora, minuto));
    }

    @Test
    void agendaUnTurnoPendiente() {
        agendar("T001", "C001", "E001", 10, 0);
        assertEquals(EstadoTurno.PENDIENTE, servicio.buscar("T001").getEstado());
    }

    @Test
    void rechazaUnaFechaPasada() {
        assertThrows(ReglaNegocioException.class, () -> servicio.agendar(
                "T001", "C001", "E001", "S001", LocalDate.now().minusDays(1), LocalTime.of(10, 0)));
    }

    @Test
    void rechazaUnaHoraFueraDelHorarioDelEspecialista() {
        // El servicio dura 60 minutos y el especialista sale a las 17:00.
        assertThrows(ReglaNegocioException.class, () -> agendar("T001", "C001", "E001", 16, 30));
    }

    @Test
    void rechazaUnClienteOServicioInexistente() {
        assertThrows(ReglaNegocioException.class, () -> agendar("T001", "C999", "E001", 10, 0));
        assertThrows(ReglaNegocioException.class, () -> servicio.agendar(
                "T001", "C001", "E001", "S999", LUNES, LocalTime.of(10, 0)));
    }

    @Test
    void rechazaUnTurnoQueSeCruzaConOtroDelMismoEspecialista() {
        agendar("T001", "C001", "E001", 10, 0);
        assertThrows(ReglaNegocioException.class, () -> agendar("T002", "C002", "E001", 10, 30));
    }

    @Test
    void rechazaUnTurnoQueSeCruzaConOtroDelMismoCliente() {
        agendar("T001", "C001", "E001", 10, 0);
        assertThrows(ReglaNegocioException.class, () -> agendar("T002", "C001", "E002", 10, 30));
    }

    @Test
    void permiteTurnosSeguidosSinCruce() {
        agendar("T001", "C001", "E001", 10, 0);
        assertDoesNotThrow(() -> agendar("T002", "C002", "E001", 11, 0));
    }

    @Test
    void unTurnoCanceladoLiberaLaFranja() {
        agendar("T001", "C001", "E001", 10, 0);
        servicio.cancelar("T001");
        assertDoesNotThrow(() -> agendar("T002", "C002", "E001", 10, 0));
    }

    @Test
    void cobraUnTurnoConfirmado() {
        agendar("T001", "C001", "E001", 10, 0);
        servicio.confirmar("T001");
        Transaccion transaccion = servicio.cobrar("T001", "F001", MetodoPago.EFECTIVO, LUNES);
        assertEquals(0, new BigDecimal("85000").compareTo(transaccion.getMontoTotal()));
        assertEquals(EstadoTurno.COMPLETADO, servicio.buscar("T001").getEstado());
    }

    @Test
    void noCobraUnTurnoPendiente() {
        agendar("T001", "C001", "E001", 10, 0);
        assertThrows(ReglaNegocioException.class,
                () -> servicio.cobrar("T001", "F001", MetodoPago.EFECTIVO, LUNES));
    }

    @Test
    void noCobraSinMetodoDePago() {
        agendar("T001", "C001", "E001", 10, 0);
        servicio.confirmar("T001");
        assertThrows(ReglaNegocioException.class, () -> servicio.cobrar("T001", "F001", null, LUNES));
    }

    @Test
    void laAgendaDelDiaTraeSoloLosTurnosDeEseDia() {
        agendar("T001", "C001", "E001", 10, 0);
        assertEquals(1, servicio.agendaDelDia(LUNES).size());
        assertEquals(0, servicio.agendaDelDia(LUNES.plusDays(1)).size());
    }
}
