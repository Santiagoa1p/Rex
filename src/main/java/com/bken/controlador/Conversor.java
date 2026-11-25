package com.bken.controlador;

import com.bken.model.MetodoPago;
import com.bken.model.Rol;
import com.bken.servicio.ReglaNegocioException;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Map;

// Convierte el texto que escribe el usuario en los tipos que usa el modelo.
// Si el texto no tiene el formato esperado, lanza ReglaNegocioException con un ejemplo valido.
final class Conversor {

    static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);
    static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("H:mm");

    private static final Map<String, DayOfWeek> DIAS = Map.of(
            "lunes", DayOfWeek.MONDAY,
            "martes", DayOfWeek.TUESDAY,
            "miercoles", DayOfWeek.WEDNESDAY,
            "jueves", DayOfWeek.THURSDAY,
            "viernes", DayOfWeek.FRIDAY,
            "sabado", DayOfWeek.SATURDAY,
            "domingo", DayOfWeek.SUNDAY);

    private Conversor() {
    }

    // Quita espacios al inicio y al final. Devuelve null si no se escribio nada.
    static String texto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    static LocalDate fecha(String valor) {
        try {
            return LocalDate.parse(obligatorio(valor, "fecha"), FORMATO_FECHA);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La fecha " + valor + " no es valida. Ejemplo: 16/11/2026");
        }
    }

    static LocalTime hora(String valor) {
        try {
            return LocalTime.parse(obligatorio(valor, "hora"), FORMATO_HORA);
        } catch (DateTimeParseException e) {
            throw new ReglaNegocioException("La hora " + valor + " no es valida. Ejemplo: 14:30");
        }
    }

    // Acepta hasta 2 decimales con punto. No acepta separadores de miles para no confundir 85.000 con 85.
    static BigDecimal dinero(String valor, String campo) {
        String limpio = obligatorio(valor, campo);
        try {
            BigDecimal numero = new BigDecimal(limpio);
            if (numero.scale() > 2) {
                throw new NumberFormatException();
            }
            return numero;
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException(
                    "El " + campo + " " + valor + " no es valido. Escribelo sin puntos de miles, ejemplo: 85000");
        }
    }

    static int entero(String valor, String campo) {
        try {
            return Integer.parseInt(obligatorio(valor, campo));
        } catch (NumberFormatException e) {
            throw new ReglaNegocioException("El campo " + campo + " debe ser un numero entero.");
        }
    }

    // Acepta el dia en espanol, con o sin tilde: lunes, miercoles, sabado...
    static DayOfWeek dia(String valor) {
        String sinTildes = Normalizer.normalize(obligatorio(valor, "dia"), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        DayOfWeek dia = DIAS.get(sinTildes);
        if (dia == null) {
            throw new ReglaNegocioException("El dia " + valor + " no es valido. Ejemplo: lunes");
        }
        return dia;
    }

    static MetodoPago metodoPago(String valor) {
        try {
            return MetodoPago.valueOf(obligatorio(valor, "metodo de pago").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("El metodo de pago debe ser EFECTIVO, TARJETA o TRANSFERENCIA.");
        }
    }

    static Rol rol(String valor) {
        try {
            return Rol.valueOf(obligatorio(valor, "rol").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("El rol debe ser ADMINISTRADOR, RECEPCIONISTA o ESPECIALISTA.");
        }
    }

    private static String obligatorio(String valor, String campo) {
        String limpio = texto(valor);
        if (limpio == null) {
            throw new ReglaNegocioException("El campo " + campo + " es obligatorio.");
        }
        return limpio;
    }
}
