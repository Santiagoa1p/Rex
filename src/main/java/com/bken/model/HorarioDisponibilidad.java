package com.bken.model;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class HorarioDisponibilidad implements Serializable {
 private static final long serialVersionUID = 1L;

    private DayOfWeek dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    public HorarioDisponibilidad() {
    }

    public HorarioDisponibilidad(DayOfWeek dia, LocalTime horaInicio, LocalTime horaFin) {
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public DayOfWeek getDia() {
        return dia;
    }

    public void setDia(DayOfWeek dia) {
        this.dia = dia;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

}
