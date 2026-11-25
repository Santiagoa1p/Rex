package com.bken.controlador;

import com.bken.model.Transaccion;
import com.bken.model.Turno;
import com.bken.servicio.TurnoServicio;

import java.time.LocalDate;
import java.util.List;

// Recibe las operaciones de la agenda (agendar, confirmar, cancelar, cobrar) y se las pasa al servicio.
public class TurnoControlador {

    private final TurnoServicio servicio;

    public TurnoControlador() {
        this(new TurnoServicio());
    }

    public TurnoControlador(TurnoServicio servicio) {
        this.servicio = servicio;
    }

    // La fecha se escribe dd/MM/aaaa y la hora HH:mm.
    public Respuesta<Turno> agendar(String idTurno, String idCliente, String idEspecialista, String idServicio,
                                    String fecha, String hora) {
        return Respuesta.de(() -> servicio.agendar(
                Conversor.texto(idTurno), Conversor.texto(idCliente), Conversor.texto(idEspecialista),
                Conversor.texto(idServicio), Conversor.fecha(fecha), Conversor.hora(hora)), "Turno agendado.");
    }

    public Respuesta<List<Turno>> agendaDelDia(String fecha) {
        return Respuesta.de(() -> servicio.agendaDelDia(Conversor.fecha(fecha)), "Agenda del dia.");
    }

    public Respuesta<List<Turno>> agendaDeHoy() {
        return Respuesta.de(() -> servicio.agendaDelDia(LocalDate.now()), "Agenda de hoy.");
    }

    public Respuesta<List<Turno>> listar() {
        return Respuesta.de(servicio::listar, "Todos los turnos.");
    }

    public Respuesta<Void> confirmar(String idTurno) {
        return Respuesta.deAccion(() -> servicio.confirmar(Conversor.texto(idTurno)), "Turno confirmado.");
    }

    public Respuesta<Void> cancelar(String idTurno) {
        return Respuesta.deAccion(() -> servicio.cancelar(Conversor.texto(idTurno)), "Turno cancelado.");
    }

    // El pago se registra con la fecha de hoy.
    public Respuesta<Transaccion> cobrar(String idTurno, String idFactura, String metodoPago) {
        return Respuesta.de(() -> servicio.cobrar(Conversor.texto(idTurno), Conversor.texto(idFactura),
                Conversor.metodoPago(metodoPago), LocalDate.now()), "Turno cobrado.");
    }
}
