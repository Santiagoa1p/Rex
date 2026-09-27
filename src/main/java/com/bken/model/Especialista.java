package com.bken.model;

// Especialista hereda los datos comunes definidos en Persona.
public class Especialista extends Persona {
    private String especialidad;
    private String horarioDisponibilidad;

    public Especialista() {
    }

    public Especialista(String id, String nombre, String telefono, String correo,
                        String especialidad, String horarioDisponibilidad) {
        super(id, nombre, telefono, correo);
        this.especialidad = especialidad;
        this.horarioDisponibilidad = horarioDisponibilidad;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getHorarioDisponibilidad() {
        return horarioDisponibilidad;
    }

    public void setHorarioDisponibilidad(String horarioDisponibilidad) {
        this.horarioDisponibilidad = horarioDisponibilidad;
    }
}