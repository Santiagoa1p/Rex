package com.bken.model;

public enum EstadoTurno {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    CANCELADO("Cancelado"),
    COMPLETADO("Completado");

    private final String etiqueta;

    EstadoTurno(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
