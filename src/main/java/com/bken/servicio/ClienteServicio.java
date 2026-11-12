package com.bken.servicio;

import com.bken.dao.ClienteDao;
import com.bken.dao.jdbc.ClienteDaoJdbc;
import com.bken.model.Cliente;

import java.time.LocalDate;
import java.util.List;

// Reglas de negocio para registrar y administrar clientes.
public class ClienteServicio {

    private final ClienteDao clienteDao;

    public ClienteServicio() {
        this(new ClienteDaoJdbc());
    }

    // Recibe el DAO por parametro para poder probar el servicio sin base de datos.
    public ClienteServicio(ClienteDao clienteDao) {
        this.clienteDao = clienteDao;
    }

    public void registrar(Cliente cliente) {
        validar(cliente);
        if (clienteDao.buscarPorId(cliente.getId()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un cliente con el id " + cliente.getId() + ".");
        }
        if (cliente.getFechaRegistro() == null) {
            cliente.setFechaRegistro(LocalDate.now());
        }
        clienteDao.guardar(cliente);
    }

    public Cliente buscar(String id) {
        return clienteDao.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el cliente " + id + "."));
    }

    public List<Cliente> listar() {
        return clienteDao.listar();
    }

    public void actualizar(Cliente cliente) {
        validar(cliente);
        Validaciones.obligatorio(cliente.getFechaRegistro(), "fecha de registro");
        buscar(cliente.getId());
        clienteDao.actualizar(cliente);
    }

    public void eliminar(String id) {
        buscar(id);
        clienteDao.eliminar(id);
    }

    private void validar(Cliente cliente) {
        Validaciones.obligatorio(cliente, "cliente");
        Validaciones.obligatorio(cliente.getId(), "id");
        Validaciones.obligatorio(cliente.getNombre(), "nombre");
        Validaciones.telefonoOpcional(cliente.getTelefono());
        Validaciones.correoOpcional(cliente.getCorreo());
    }
}
