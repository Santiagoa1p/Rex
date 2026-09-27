package com.bken.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaccion implements Serializable {
    private String idFactura;
    private Turno turno;
    private BigDecimal montoTotal;
    private String metodoPago;
    private LocalDate fechaPago;

    public Transaccion() {
    }

    public Transaccion(String idFactura, Turno turno, BigDecimal montoTotal,
                       String metodoPago, LocalDate fechaPago) {
        this.idFactura = idFactura;
        this.turno = turno;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.fechaPago = fechaPago;
    }

    public String getIdFactura() {
        return idFactura;
    }

    public void setIdFactura(String idFactura) {
        this.idFactura = idFactura;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }
}