package com.bken.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class Turno implements Serializable {
    private String idTurno;
    private Cliente cliente;
    private Especialista especialista;
    private Servicio servicio;
    private LocalDate fecha;
    private LocalTime hora;
    private EstadoTurno estado;
    private Transaccion transaccion;
    
    private static final long serialVersionUID = 1L;

    public Turno() {
    }

    public Turno(String idTurno, Cliente cliente, Especialista especialista, Servicio servicio,
                 LocalDate fecha, LocalTime hora, EstadoTurno estado) {
        this.idTurno = idTurno;
        this.cliente = cliente;
        this.especialista = especialista;
        this.servicio = servicio;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
    }

    public String getIdTurno() {
        return idTurno;
    }

    public void setIdTurno(String idTurno) {
        this.idTurno = idTurno;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Especialista getEspecialista() {
        return especialista;
    }

    public void setEspecialista(Especialista especialista) {
        this.especialista = especialista;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public void setEstado(EstadoTurno estado) {
        this.estado = estado;
    }

// Un turno solo se confirma si esta pendiente.
    public void confirmar() {
        if (estado != EstadoTurno.PENDIENTE) {
            throw new IllegalStateException(
                    "Solo se puede confirmar un turno pendiente. Estado actual: " + estado.getEtiqueta());
        }
        estado = EstadoTurno.CONFIRMADO;
    }

    // Un turno solo se cancela si aun no fue completado ni cancelado.
    public void cancelar() {
        if (estado != EstadoTurno.PENDIENTE && estado != EstadoTurno.CONFIRMADO) {
            throw new IllegalStateException(
                    "Solo se puede cancelar un turno pendiente o confirmado. Estado actual: " + estado.getEtiqueta());
        }
        estado = EstadoTurno.CANCELADO;
    }

      public Transaccion getTransaccion() {
        return transaccion;
    }

    // Cobra un turno confirmado: genera la transaccion con el precio del servicio.
    public Transaccion cobrar(String idFactura, MetodoPago metodoPago, LocalDate fechaPago) {
        if (estado != EstadoTurno.CONFIRMADO) {
            throw new IllegalStateException(
                    "Solo se puede cobrar un turno confirmado. Estado actual: " + estado.getEtiqueta());
        }
        transaccion = new Transaccion(idFactura, this, servicio.getPrecio(), metodoPago, fechaPago);
        estado = EstadoTurno.COMPLETADO;
        return transaccion;
    }



}