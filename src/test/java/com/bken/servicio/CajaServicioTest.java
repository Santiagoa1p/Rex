package com.bken.servicio;

import com.bken.model.MetodoPago;
import com.bken.model.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CajaServicioTest {
    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);

    private CajaServicio caja;

    @BeforeEach
    void preparar() {
        DaosEnMemoria.Transacciones transacciones = new DaosEnMemoria.Transacciones();
        transacciones.guardar(new Transaccion("F001", null, new BigDecimal("85000"), MetodoPago.EFECTIVO, HOY));
        transacciones.guardar(new Transaccion("F002", null, new BigDecimal("40000.50"), MetodoPago.TARJETA, HOY));
        transacciones.guardar(new Transaccion("F003", null, new BigDecimal("30000"), MetodoPago.EFECTIVO,
                HOY.plusDays(1)));
        caja = new CajaServicio(transacciones);
    }

    @Test
    void sumaSoloLosPagosDelDia() {
        assertEquals(0, new BigDecimal("125000.50").compareTo(caja.totalPorFecha(HOY)));
        assertEquals(2, caja.listarPorFecha(HOY).size());
    }

    @Test
    void sumaTodosLosPagos() {
        assertEquals(0, new BigDecimal("155000.50").compareTo(caja.totalGeneral()));
    }

    @Test
    void unDiaSinPagosTotalizaCero() {
        assertEquals(0, BigDecimal.ZERO.compareTo(caja.totalPorFecha(HOY.minusDays(1))));
    }
}
