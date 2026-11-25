package com.bken.vista;

import com.bken.model.HorarioDisponibilidad;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

// Formatos para mostrar datos en pantalla. Lo usan la vista de consola y la vista Swing.
// Se evitan tildes y simbolos especiales para que se vean bien en cualquier terminal de Windows.
public final class Formato {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Miles con punto, como se escribe en Colombia: $85.000
    private static final DecimalFormat PESOS =
            new DecimalFormat("$#,##0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
    private static final String[] NOMBRES_DIAS =
            {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};

    private Formato() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha.format(FECHA);
    }

    public static String pesos(BigDecimal valor) {
        return PESOS.format(valor);
    }

    public static String dia(DayOfWeek dia) {
        return NOMBRES_DIAS[dia.getValue() - 1];
    }

    public static String[] nombresDias() {
        return NOMBRES_DIAS.clone();
    }

    public static String horario(HorarioDisponibilidad horario) {
        return dia(horario.getDia()) + " " + horario.getHoraInicio() + "-" + horario.getHoraFin();
    }

    // Todos los horarios en una linea: "lunes 09:00-17:00, martes 09:00-12:00".
    public static String horarios(List<HorarioDisponibilidad> horarios) {
        if (horarios == null || horarios.isEmpty()) {
            return "Sin horarios";
        }
        StringBuilder texto = new StringBuilder();
        for (HorarioDisponibilidad horario : horarios) {
            if (texto.length() > 0) {
                texto.append(", ");
            }
            texto.append(horario(horario));
        }
        return texto.toString();
    }

    public static String valorOGuion(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
