package com.bken.servicio;

// Se lanza cuando una operacion no cumple las reglas del negocio
// (dato obligatorio vacio, horario ocupado, credenciales incorrectas, etc.).
// El mensaje esta pensado para mostrarse tal cual al usuario.
public class ReglaNegocioException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
