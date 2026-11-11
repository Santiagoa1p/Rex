package com.bken.dao.jdbc;

import com.bken.config.ConexionDatabase;
import com.bken.dao.DaoException;
import com.bken.dao.ServicioDao;
import com.bken.model.Servicio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Persistencia de servicios en la tabla servicios usando JDBC.
public class ServicioDaoJdbc implements ServicioDao {

    private static final String INSERTAR =
            "INSERT INTO servicios (id_servicio, nombre_servicio, precio, duracion_minutos) VALUES (?, ?, ?, ?)";
    private static final String BUSCAR_POR_ID =
            "SELECT id_servicio, nombre_servicio, precio, duracion_minutos FROM servicios WHERE id_servicio = ?";
    private static final String LISTAR =
            "SELECT id_servicio, nombre_servicio, precio, duracion_minutos FROM servicios ORDER BY nombre_servicio";
    private static final String ACTUALIZAR =
            "UPDATE servicios SET nombre_servicio = ?, precio = ?, duracion_minutos = ? WHERE id_servicio = ?";
    private static final String ELIMINAR =
            "DELETE FROM servicios WHERE id_servicio = ?";

    @Override
    public void guardar(Servicio servicio) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setString(1, servicio.getIdServicio());
            sentencia.setString(2, servicio.getNombreServicio());
            sentencia.setBigDecimal(3, servicio.getPrecio());
            sentencia.setInt(4, servicio.getDuracionMinutos());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("No se pudo guardar el servicio " + servicio.getIdServicio(), e);
        }
    }

    @Override
    public Optional<Servicio> buscarPorId(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setString(1, id);
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next() ? Optional.of(mapear(fila)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo buscar el servicio " + id, e);
        }
    }

    @Override
    public List<Servicio> listar() {
        List<Servicio> servicios = new ArrayList<>();
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
             ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                servicios.add(mapear(fila));
            }
            return servicios;
        } catch (SQLException e) {
            throw new DaoException("No se pudo listar los servicios", e);
        }
    }

    @Override
    public void actualizar(Servicio servicio) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
            sentencia.setString(1, servicio.getNombreServicio());
            sentencia.setBigDecimal(2, servicio.getPrecio());
            sentencia.setInt(3, servicio.getDuracionMinutos());
            sentencia.setString(4, servicio.getIdServicio());
            if (sentencia.executeUpdate() == 0) {
                throw new DaoException("No existe el servicio " + servicio.getIdServicio());
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo actualizar el servicio " + servicio.getIdServicio(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, id);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            // Falla, por ejemplo, si el servicio ya se uso en algun turno.
            throw new DaoException("No se pudo eliminar el servicio " + id, e);
        }
    }

    // Convierte la fila actual del ResultSet en un Servicio.
    private Servicio mapear(ResultSet fila) throws SQLException {
        return new Servicio(
                fila.getString("id_servicio"),
                fila.getString("nombre_servicio"),
                fila.getBigDecimal("precio"),
                fila.getInt("duracion_minutos"));
    }
}
