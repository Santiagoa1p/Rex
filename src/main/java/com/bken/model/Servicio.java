package com.bken.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Servicio implements Serializable {
    private String idServicio;
    private String nombreServicio;
    private BigDecimal precio;
    private int duracionMinutos;

    public Servicio() {
    }

    public Servicio(String idServicio, String nombreServicio, BigDecimal precio, int duracionMinutos) {
        this.idServicio = idServicio;
        this.nombreServicio = nombreServicio;
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
    }

    public String getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(String idServicio) {
        this.idServicio = idServicio;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(int duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }
}