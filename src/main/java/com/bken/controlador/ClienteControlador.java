package com.bken.controlador;

import com.bken.model.Cliente;
import com.bken.servicio.ClienteServicio;

import java.util.List;

// Recibe los datos de clientes que escribe el usuario y se los pasa al servicio.
public class ClienteControlador {

    private final ClienteServicio servicio;

    public ClienteControlador() {
        this(new ClienteServicio());
    }

    public ClienteControlador(ClienteServicio servicio) {
        this.servicio = servicio;
    }

    public Respuesta<Cliente> registrar(String id, String nombre, String telefono, String correo) {
        return Respuesta.de(() -> {
            Cliente cliente = new Cliente(Conversor.texto(id), Conversor.texto(nombre),
                    Conversor.texto(telefono), Conversor.texto(correo), null);
            servicio.registrar(cliente);
            return cliente;
        }, "Cliente registrado.");
    }

    public Respuesta<List<Cliente>> listar() {
        return Respuesta.de(servicio::listar, "Clientes registrados.");
    }

    public Respuesta<Cliente> buscar(String id) {
        return Respuesta.de(() -> servicio.buscar(Conversor.texto(id)), "Cliente encontrado.");
    }

    // Cambia nombre, telefono y correo. La fecha de registro se conserva.
    public Respuesta<Cliente> actualizar(String id, String nombre, String telefono, String correo) {
        return Respuesta.de(() -> {
            Cliente cliente = servicio.buscar(Conversor.texto(id));
            cliente.setNombre(Conversor.texto(nombre));
            cliente.setTelefono(Conversor.texto(telefono));
            cliente.setCorreo(Conversor.texto(correo));
            servicio.actualizar(cliente);
            return cliente;
        }, "Cliente actualizado.");
    }

    public Respuesta<Void> eliminar(String id) {
        return Respuesta.deAccion(() -> servicio.eliminar(Conversor.texto(id)), "Cliente eliminado.");
    }
}
