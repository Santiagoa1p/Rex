package com.bken.controlador;

import com.bken.model.MetodoPago;
import com.bken.servicio.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConversorTest {

    @Test
    void convierteFechaEnFormatoDiaMesAnio() {
        assertEquals(LocalDate.of(2026, 11, 16), Conversor.fecha("16/11/2026"));
    }

    @Test
    void rechazaFechasImposiblesOMalEscritas() {
        assertThrows(ReglaNegocioException.class, () -> Conversor.fecha("31/02/2026"));
        assertThrows(ReglaNegocioException.class, () -> Conversor.fecha("2026-11-16"));
        assertThrows(ReglaNegocioException.class, () -> Conversor.fecha(""));
    }

    @Test
    void convierteHoraConUnoODosDigitos() {
        assertEquals(LocalTime.of(9, 0), Conversor.hora("9:00"));
        assertEquals(LocalTime.of(14, 30), Conversor.hora("14:30"));
        assertThrows(ReglaNegocioException.class, () -> Conversor.hora("25:00"));
    }

    @Test
    void conviertePrecioSinSeparadorDeMiles() {
        assertEquals(new BigDecimal("85000"), Conversor.dinero("85000", "precio"));
        assertEquals(new BigDecimal("85000.50"), Conversor.dinero("85000.50", "precio"));
        // 85.000 tiene 3 decimales: se rechaza para no guardar 85 pesos por error.
        assertThrows(ReglaNegocioException.class, () -> Conversor.dinero("85.000", "precio"));
        assertThrows(ReglaNegocioException.class, () -> Conversor.dinero("ochenta", "precio"));
    }

    @Test
    void convierteDiasEnEspanolConOSinTilde() {
        assertEquals(DayOfWeek.WEDNESDAY, Conversor.dia("miércoles"));
        assertEquals(DayOfWeek.SATURDAY, Conversor.dia("Sabado"));
        assertThrows(ReglaNegocioException.class, () -> Conversor.dia("monday"));
    }

    @Test
    void convierteMetodoDePagoSinImportarMayusculas() {
        assertEquals(MetodoPago.TARJETA, Conversor.metodoPago("tarjeta"));
        assertThrows(ReglaNegocioException.class, () -> Conversor.metodoPago("cheque"));
    }

    @Test
    void textoVacioSeConvierteEnNull() {
        assertNull(Conversor.texto("   "));
        assertEquals("Ana", Conversor.texto("  Ana "));
    }

    @Test
    void respuestaConvierteErroresDeNegocioEnMensaje() {
        Respuesta<Integer> respuesta = Respuesta.de(() -> Conversor.entero("abc", "duracion"), "ok");
        assertEquals(false, respuesta.isExito());
        assertEquals("El campo duracion debe ser un numero entero.", respuesta.getMensaje());
    }
}
