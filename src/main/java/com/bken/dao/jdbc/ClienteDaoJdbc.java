package com.bken.dao.jdbc;

import com.bken.config.ConexionDatabase;
import com.bken.dao.ClienteDao;
import com.bken.dao.DaoException;
import com.bken.model.Cliente;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Persistencia de clientes en la tabla clientes usando JDBC.
public class ClienteDaoJdbc implements ClienteDao {

    private static final String INSERTAR =
            "INSERT INTO clientes (id, nombre, telefono, correo, fecha_registro) VALUES (?, ?, ?, ?, ?)";
    private static final String BUSCAR_POR_ID =
            "SELECT id, nombre, telefono, correo, fecha_registro FROM clientes WHERE id = ?";
    private static final String LISTAR =
            "SELECT id, nombre, telefono, correo, fecha_registro FROM clientes ORDER BY nombre";
    private static final String ACTUALIZAR =
            "UPDATE clientes SET nombre = ?, telefono = ?, correo = ?, fecha_registro = ? WHERE id = ?";
    private static final String ELIMINAR =
            "DELETE FROM clientes WHERE id = ?";

    @Override
    public void guardar(Cliente cliente) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setString(1, cliente.getId());
            sentencia.setString(2, cliente.getNombre());
            sentencia.setString(3, cliente.getTelefono());
            sentencia.setString(4, cliente.getCorreo());
            sentencia.setDate(5, Date.valueOf(cliente.getFechaRegistro()));
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("No se pudo guardar el cliente " + cliente.getId(), e);
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setString(1, id);
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next() ? Optional.of(mapear(fila)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo buscar el cliente " + id, e);
        }
    }

    @Override
    public List<Cliente> listar() {
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
             ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                clientes.add(mapear(fila));
            }
            return clientes;
        } catch (SQLException e) {
            throw new DaoException("No se pudo listar los clientes", e);
        }
    }

    @Override
    public void actualizar(Cliente cliente) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
            sentencia.setString(1, cliente.getNombre());
            sentencia.setString(2, cliente.getTelefono());
            sentencia.setString(3, cliente.getCorreo());
            sentencia.setDate(4, Date.valueOf(cliente.getFechaRegistro()));
            sentencia.setString(5, cliente.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new DaoException("No existe el cliente " + cliente.getId());
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo actualizar el cliente " + cliente.getId(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, id);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            // Falla, por ejemplo, si el cliente tiene turnos registrados.
            throw new DaoException("No se pudo eliminar el cliente " + id, e);
        }
    }

    // Convierte la fila actual del ResultSet en un Cliente.
    private Cliente mapear(ResultSet fila) throws SQLException {
        return new Cliente(
                fila.getString("id"),
                fila.getString("nombre"),
                fila.getString("telefono"),
                fila.getString("correo"),
                fila.getDate("fecha_registro").toLocalDate());
    }
}
