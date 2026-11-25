package com.bken.dao.jdbc;

import com.bken.config.ConexionDatabase;
import com.bken.dao.DaoException;
import com.bken.dao.EspecialistaDao;
import com.bken.model.Especialista;
import com.bken.model.HorarioDisponibilidad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Persistencia de especialistas y de sus horarios (tablas especialistas y horarios_disponibilidad).
// Guardar y actualizar tocan las dos tablas, por eso se hacen dentro de una transaccion:
// o se guarda todo o no se guarda nada.
public class EspecialistaDaoJdbc implements EspecialistaDao {

    private static final String INSERTAR =
            "INSERT INTO especialistas (id, nombre, telefono, correo, especialidad) VALUES (?, ?, ?, ?, ?)";
    private static final String BUSCAR_POR_ID =
            "SELECT id, nombre, telefono, correo, especialidad FROM especialistas WHERE id = ?";
    private static final String LISTAR =
            "SELECT id, nombre, telefono, correo, especialidad FROM especialistas ORDER BY nombre";
    private static final String ACTUALIZAR =
            "UPDATE especialistas SET nombre = ?, telefono = ?, correo = ?, especialidad = ? WHERE id = ?";
    // Los horarios se borran solos por el ON DELETE CASCADE del esquema.
    private static final String ELIMINAR =
            "DELETE FROM especialistas WHERE id = ?";

    private static final String INSERTAR_HORARIO =
            "INSERT INTO horarios_disponibilidad (especialista_id, dia, hora_inicio, hora_fin) VALUES (?, ?, ?, ?)";
    private static final String HORARIOS_DE_ESPECIALISTA =
            "SELECT dia, hora_inicio, hora_fin FROM horarios_disponibilidad WHERE especialista_id = ? ORDER BY id";
    private static final String TODOS_LOS_HORARIOS =
            "SELECT especialista_id, dia, hora_inicio, hora_fin FROM horarios_disponibilidad ORDER BY id";
    private static final String BORRAR_HORARIOS =
            "DELETE FROM horarios_disponibilidad WHERE especialista_id = ?";

    @Override
    public void guardar(Especialista especialista) {
        try (Connection conexion = ConexionDatabase.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
                    sentencia.setString(1, especialista.getId());
                    sentencia.setString(2, especialista.getNombre());
                    sentencia.setString(3, especialista.getTelefono());
                    sentencia.setString(4, especialista.getCorreo());
                    sentencia.setString(5, especialista.getEspecialidad());
                    sentencia.executeUpdate();
                }
                insertarHorarios(conexion, especialista);
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo guardar el especialista " + especialista.getId(), e);
        }
    }

    @Override
    public Optional<Especialista> buscarPorId(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(BUSCAR_POR_ID)) {
            sentencia.setString(1, id);
            try (ResultSet fila = sentencia.executeQuery()) {
                if (!fila.next()) {
                    return Optional.empty();
                }
                Especialista especialista = mapear(fila);
                especialista.setHorarios(buscarHorarios(conexion, id));
                return Optional.of(especialista);
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo buscar el especialista " + id, e);
        }
    }

    @Override
    public List<Especialista> listar() {
        try (Connection conexion = ConexionDatabase.getConnection()) {
            // Se leen todos los horarios en una sola consulta y se reparten por especialista.
            Map<String, List<HorarioDisponibilidad>> horariosPorEspecialista = new HashMap<>();
            try (PreparedStatement sentencia = conexion.prepareStatement(TODOS_LOS_HORARIOS);
                 ResultSet fila = sentencia.executeQuery()) {
                while (fila.next()) {
                    horariosPorEspecialista
                            .computeIfAbsent(fila.getString("especialista_id"), clave -> new ArrayList<>())
                            .add(mapearHorario(fila));
                }
            }

            List<Especialista> especialistas = new ArrayList<>();
            try (PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
                 ResultSet fila = sentencia.executeQuery()) {
                while (fila.next()) {
                    Especialista especialista = mapear(fila);
                    especialista.setHorarios(
                            horariosPorEspecialista.getOrDefault(especialista.getId(), new ArrayList<>()));
                    especialistas.add(especialista);
                }
            }
            return especialistas;
        } catch (SQLException e) {
            throw new DaoException("No se pudo listar los especialistas", e);
        }
    }

    // Actualiza los datos del especialista y reemplaza todos sus horarios por los actuales.
    @Override
    public void actualizar(Especialista especialista) {
        try (Connection conexion = ConexionDatabase.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
                    sentencia.setString(1, especialista.getNombre());
                    sentencia.setString(2, especialista.getTelefono());
                    sentencia.setString(3, especialista.getCorreo());
                    sentencia.setString(4, especialista.getEspecialidad());
                    sentencia.setString(5, especialista.getId());
                    if (sentencia.executeUpdate() == 0) {
                        conexion.rollback();
                        throw new DaoException("No existe el especialista " + especialista.getId());
                    }
                }
                try (PreparedStatement sentencia = conexion.prepareStatement(BORRAR_HORARIOS)) {
                    sentencia.setString(1, especialista.getId());
                    sentencia.executeUpdate();
                }
                insertarHorarios(conexion, especialista);
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo actualizar el especialista " + especialista.getId(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, id);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            // Falla, por ejemplo, si el especialista tiene turnos registrados.
            throw new DaoException("No se pudo eliminar el especialista " + id, e);
        }
    }

    // Inserta todos los horarios del especialista en un solo lote.
    private void insertarHorarios(Connection conexion, Especialista especialista) throws SQLException {
        if (especialista.getHorarios() == null || especialista.getHorarios().isEmpty()) {
            return;
        }
        try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR_HORARIO)) {
            for (HorarioDisponibilidad horario : especialista.getHorarios()) {
                sentencia.setString(1, especialista.getId());
                sentencia.setString(2, horario.getDia().name());
                sentencia.setTime(3, Time.valueOf(horario.getHoraInicio()));
                sentencia.setTime(4, Time.valueOf(horario.getHoraFin()));
                sentencia.addBatch();
            }
            sentencia.executeBatch();
        }
    }

    private List<HorarioDisponibilidad> buscarHorarios(Connection conexion, String especialistaId)
            throws SQLException {
        List<HorarioDisponibilidad> horarios = new ArrayList<>();
        try (PreparedStatement sentencia = conexion.prepareStatement(HORARIOS_DE_ESPECIALISTA)) {
            sentencia.setString(1, especialistaId);
            try (ResultSet fila = sentencia.executeQuery()) {
                while (fila.next()) {
                    horarios.add(mapearHorario(fila));
                }
            }
        }
        return horarios;
    }

    // Convierte la fila actual en un Especialista, todavia sin horarios.
    private Especialista mapear(ResultSet fila) throws SQLException {
        return new Especialista(
                fila.getString("id"),
                fila.getString("nombre"),
                fila.getString("telefono"),
                fila.getString("correo"),
                fila.getString("especialidad"),
                new ArrayList<>());
    }

    private HorarioDisponibilidad mapearHorario(ResultSet fila) throws SQLException {
        return new HorarioDisponibilidad(
                DayOfWeek.valueOf(fila.getString("dia")),
                fila.getTime("hora_inicio").toLocalTime(),
                fila.getTime("hora_fin").toLocalTime());
    }
}
