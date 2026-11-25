package com.bken.controlador;

import com.bken.dao.DaoException;
import com.bken.servicio.ReglaNegocioException;

import java.sql.SQLException;
import java.util.function.Supplier;

// Resultado que el controlador entrega a la vista: si salio bien, un mensaje y el dato obtenido.
// Asi la vista (consola o Swing) no tiene que capturar excepciones, solo mostrar el mensaje.
public final class Respuesta<T> {

    // Codigo de PostgreSQL cuando se intenta borrar un registro que otro esta usando (llave foranea).
    private static final String REGISTRO_EN_USO = "23503";

    private final boolean exito;
    private final String mensaje;
    private final T dato;

    private Respuesta(boolean exito, String mensaje, T dato) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.dato = dato;
    }

    public static <T> Respuesta<T> exito(T dato, String mensaje) {
        return new Respuesta<>(true, mensaje, dato);
    }

    public static <T> Respuesta<T> error(String mensaje) {
        return new Respuesta<>(false, mensaje, null);
    }

    // Ejecuta una operacion que devuelve un dato y convierte cualquier error en un mensaje.
    static <T> Respuesta<T> de(Supplier<T> operacion, String mensajeExito) {
        try {
            return exito(operacion.get(), mensajeExito);
        } catch (ReglaNegocioException e) {
            return error(e.getMessage());
        } catch (DaoException e) {
            return error(explicar(e));
        } catch (IllegalStateException e) {
            // Por ejemplo, cuando falta el archivo config.properties.
            return error(e.getMessage());
        }
    }

    // Igual que de(...), para operaciones que no devuelven nada (eliminar, confirmar...).
    static Respuesta<Void> deAccion(Runnable accion, String mensajeExito) {
        return de(() -> {
            accion.run();
            return null;
        }, mensajeExito);
    }

    private static String explicar(DaoException e) {
        if (e.getCause() instanceof SQLException sql && REGISTRO_EN_USO.equals(sql.getSQLState())) {
            return e.getMessage() + " porque tiene turnos o pagos asociados.";
        }
        return "Error en la base de datos: " + e.getMessage() + ".";
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public T getDato() {
        return dato;
    }
}
