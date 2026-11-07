package com.bken.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

// Especialista hereda los datos comunes definidos en Persona.
public class Especialista extends Persona {
    private static final long serialVersionUID = 1L;

    private String especialidad;
    private List<HorarioDisponibilidad> horarios = new ArrayList<>();

   // private String horarioDisponibilidad;
    
    public Especialista() {
    }


    public Especialista(String id, String nombre, String telefono, String correo,
                        String especialidad, List<HorarioDisponibilidad> horarios) {
        super(id, nombre, telefono, correo);
        this.especialidad = especialidad;
        this.horarios = horarios;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public List<HorarioDisponibilidad> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioDisponibilidad> horarios) {
        this.horarios = horarios;
    }

    // Indica si algun horario cubre el dia y la franja completa del servicio.
    public boolean estaDisponible(LocalDate fecha, LocalTime hora, int duracionMinutos) {
        LocalTime fin = hora.plusMinutes(duracionMinutos);

        // Si el fin no es posterior al inicio, la duracion es invalida o el servicio cruza la medianoche.
        if (!fin.isAfter(hora)) {
            return false;
        }

        for (HorarioDisponibilidad horario : horarios) {
            boolean mismoDia = horario.getDia() == fecha.getDayOfWeek();
            boolean empiezaDentro = !hora.isBefore(horario.getHoraInicio());
            boolean terminaDentro = !fin.isAfter(horario.getHoraFin());

            if (mismoDia && empiezaDentro && terminaDentro) {
                return true;
            }
        }
        return false;
    }




}