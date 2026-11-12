package com.bken.controlador;

import com.bken.model.Servicio;
import com.bken.servicio.CatalogoServicio;

import java.util.List;

// Recibe los datos del catalogo de servicios y se los pasa al servicio.
public class CatalogoControlador {

    private final CatalogoServicio servicio;

    public CatalogoControlador() {
        this(new CatalogoServicio());
    }

    public CatalogoControlador(CatalogoServicio servicio) {
        this.servicio = servicio;
    }

    public Respuesta<Servicio> registrar(String id, String nombre, String precio, String duracionMinutos) {
        return Respuesta.de(() -> {
            Servicio nuevo = new Servicio(Conversor.texto(id), Conversor.texto(nombre),
                    Conversor.dinero(precio, "precio"), Conversor.entero(duracionMinutos, "duracion"));
            servicio.registrar(nuevo);
            return nuevo;
        }, "Servicio registrado.");
    }

    public Respuesta<List<Servicio>> listar() {
        return Respuesta.de(servicio::listar, "Servicios del catalogo.");
    }

    public Respuesta<Servicio> actualizar(String id, String nombre, String precio, String duracionMinutos) {
        return Respuesta.de(() -> {
            Servicio existente = servicio.buscar(Conversor.texto(id));
            existente.setNombreServicio(Conversor.texto(nombre));
            existente.setPrecio(Conversor.dinero(precio, "precio"));
            existente.setDuracionMinutos(Conversor.entero(duracionMinutos, "duracion"));
            servicio.actualizar(existente);
            return existente;
        }, "Servicio actualizado.");
    }

    public Respuesta<Void> eliminar(String id) {
        return Respuesta.deAccion(() -> servicio.eliminar(Conversor.texto(id)), "Servicio eliminado.");
    }
}
