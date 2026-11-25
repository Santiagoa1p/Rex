package com.bken.controlador;

import com.bken.model.Especialista;
import com.bken.model.HorarioDisponibilidad;
import com.bken.servicio.EspecialistaServicio;

import java.util.ArrayList;
import java.util.List;

// Recibe los datos de especialistas y sus horarios y se los pasa al servicio.
public class EspecialistaControlador {

    private final EspecialistaServicio servicio;

    public EspecialistaControlador() {
        this(new EspecialistaServicio());
    }

    public EspecialistaControlador(EspecialistaServicio servicio) {
        this.servicio = servicio;
    }

    // Convierte un horario escrito por el usuario (ej: "lunes", "9:00", "17:00").
    // La vista va juntando los horarios y luego llama a registrar o actualizar.
    public Respuesta<HorarioDisponibilidad> crearHorario(String dia, String horaInicio, String horaFin) {
        return Respuesta.de(() -> new HorarioDisponibilidad(
                Conversor.dia(dia), Conversor.hora(horaInicio), Conversor.hora(horaFin)), "Horario agregado.");
    }

    public Respuesta<Especialista> registrar(String id, String nombre, String telefono, String correo,
                                             String especialidad, List<HorarioDisponibilidad> horarios) {
        return Respuesta.de(() -> {
            Especialista especialista = new Especialista(Conversor.texto(id), Conversor.texto(nombre),
                    Conversor.texto(telefono), Conversor.texto(correo), Conversor.texto(especialidad),
                    new ArrayList<>(horarios));
            servicio.registrar(especialista);
            return especialista;
        }, "Especialista registrado.");
    }

    public Respuesta<List<Especialista>> listar() {
        return Respuesta.de(servicio::listar, "Especialistas registrados.");
    }

    public Respuesta<Especialista> buscar(String id) {
        return Respuesta.de(() -> servicio.buscar(Conversor.texto(id)), "Especialista encontrado.");
    }

    // Reemplaza los horarios del especialista por los nuevos.
    public Respuesta<Especialista> cambiarHorarios(String id, List<HorarioDisponibilidad> horarios) {
        return Respuesta.de(() -> {
            Especialista especialista = servicio.buscar(Conversor.texto(id));
            especialista.setHorarios(new ArrayList<>(horarios));
            servicio.actualizar(especialista);
            return especialista;
        }, "Horarios actualizados.");
    }

    public Respuesta<Void> eliminar(String id) {
        return Respuesta.deAccion(() -> servicio.eliminar(Conversor.texto(id)), "Especialista eliminado.");
    }
}
