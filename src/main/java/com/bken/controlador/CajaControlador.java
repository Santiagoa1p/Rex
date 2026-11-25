package com.bken.controlador;

import com.bken.model.Transaccion;
import com.bken.servicio.CajaServicio;

import java.math.BigDecimal;
import java.util.List;

// Consultas de caja para la vista.
public class CajaControlador {

    private final CajaServicio servicio;

    public CajaControlador() {
        this(new CajaServicio());
    }

    public CajaControlador(CajaServicio servicio) {
        this.servicio = servicio;
    }

    public Respuesta<List<Transaccion>> pagosDelDia(String fecha) {
        return Respuesta.de(() -> servicio.listarPorFecha(Conversor.fecha(fecha)), "Pagos del dia.");
    }

    public Respuesta<BigDecimal> totalDelDia(String fecha) {
        return Respuesta.de(() -> servicio.totalPorFecha(Conversor.fecha(fecha)), "Total del dia.");
    }

    public Respuesta<List<Transaccion>> todosLosPagos() {
        return Respuesta.de(servicio::listar, "Todos los pagos.");
    }

    public Respuesta<BigDecimal> totalGeneral() {
        return Respuesta.de(servicio::totalGeneral, "Total general.");
    }
}
