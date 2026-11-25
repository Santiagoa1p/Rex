package com.bken.servicio;

import java.util.regex.Pattern;

// Validaciones comunes que usan todos los servicios.
final class Validaciones {

    private static final Pattern CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{7,15}$");

    private Validaciones() {
    }

    static void obligatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
    }

    static void obligatorio(Object valor, String campo) {
        if (valor == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
    }

    // El correo es opcional, pero si se escribe debe tener un formato valido.
    static void correoOpcional(String correo) {
        if (correo != null && !correo.isBlank() && !CORREO.matcher(correo).matches()) {
            throw new ReglaNegocioException("El correo " + correo + " no es valido.");
        }
    }

    // El telefono es opcional, pero si se escribe debe tener solo digitos (7 a 15).
    static void telefonoOpcional(String telefono) {
        if (telefono != null && !telefono.isBlank() && !TELEFONO.matcher(telefono).matches()) {
            throw new ReglaNegocioException("El telefono debe tener solo numeros, entre 7 y 15 digitos.");
        }
    }
}
