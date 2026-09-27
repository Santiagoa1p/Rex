package com.bken.model;

import java.time.LocalDate;

// Cliente hereda los datos comunes definidos en Persona.
public class Cliente extends Persona {
    private LocalDate fechaRegistro;

    public Cliente() {
    }

    public Cliente(String id, String nombre, String telefono, String correo, LocalDate fechaRegistro) {
        super(id, nombre, telefono, correo);
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}