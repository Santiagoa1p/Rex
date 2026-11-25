package com.bken.dao;

// Error al leer o escribir en la base de datos.
// Envuelve la SQLException para que las capas superiores no dependan de JDBC.
public class DaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DaoException(String mensaje) {
        super(mensaje);
    }

    public DaoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
