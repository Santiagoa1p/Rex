package com.bken.dao.jdbc;

import com.bken.config.ConexionDatabase;
import com.bken.dao.DaoException;
import com.bken.dao.TurnoDao;
import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.EstadoTurno;
import com.bken.model.HorarioDisponibilidad;
import com.bken.model.MetodoPago;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import com.bken.model.Turno;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Persistencia de turnos. Un turno se lee completo en una sola consulta (JOIN) con su cliente,
// especialista, servicio y, si ya fue cobrado, su transaccion.
public class TurnoDaoJdbc implements TurnoDao {

    // Consulta base: cada columna lleva un alias para no confundir el "id" de cada tabla.
    private static final String SELECT_TURNOS =
            "SELECT t.id_turno, t.fecha, t.hora, t.estado,"
            + " c.id AS c_id, c.nombre AS c_nombre, c.telefono AS c_telefono, c.correo AS c_correo,"
            + " c.fecha_registro AS c_fecha_registro,"
            + " e.id AS e_id, e.nombre AS e_nombre, e.telefono AS e_telefono, e.correo AS e_correo,"
            + " e.especialidad AS e_especialidad,"
            + " s.id_servicio, s.nombre_servicio, s.precio, s.duracion_minutos,"
            + " tr.id_factura, tr.monto_total, tr.metodo_pago, tr.fecha_pago"
            + " FROM turnos t"
            + " JOIN clientes c ON c.id = t.cliente_id"
            + " JOIN especialistas e ON e.id = t.especialista_id"
            + " JOIN servicios s ON s.id_servicio = t.servicio_id"
            + " LEFT JOIN transacciones tr ON tr.turno_id = t.id_turno ";

    private static final String INSERTAR =
            "INSERT INTO turnos (id_turno, cliente_id, especialista_id, servicio_id, fecha, hora, estado)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String ACTUALIZAR =
            "UPDATE turnos SET cliente_id = ?, especialista_id = ?, servicio_id = ?, fecha = ?, hora = ?, estado = ?"
            + " WHERE id_turno = ?";
    private static final String ELIMINAR =
            "DELETE FROM turnos WHERE id_turno = ?";
    // Si el turno ya tenia su transaccion guardada, no se vuelve a insertar.
    private static final String INSERTAR_TRANSACCION =
            "INSERT INTO transacciones (id_factura, turno_id, monto_total, metodo_pago, fecha_pago)"
            + " VALUES (?, ?, ?, ?, ?) ON CONFLICT (turno_id) DO NOTHING";
    private static final String HORARIOS_DE_ESPECIALISTAS =
            "SELECT especialista_id, dia, hora_inicio, hora_fin FROM horarios_disponibilidad"
            + " WHERE especialista_id = ANY (?) ORDER BY id";

    // Guarda el turno y, si ya viene cobrado, tambien su transaccion.
    @Override
    public void guardar(Turno turno) {
        try (Connection conexion = ConexionDatabase.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
                    sentencia.setString(1, turno.getIdTurno());
                    sentencia.setString(2, turno.getCliente().getId());
                    sentencia.setString(3, turno.getEspecialista().getId());
                    sentencia.setString(4, turno.getServicio().getIdServicio());
                    sentencia.setDate(5, Date.valueOf(turno.getFecha()));
                    sentencia.setTime(6, Time.valueOf(turno.getHora()));
                    sentencia.setString(7, turno.getEstado().name());
                    sentencia.executeUpdate();
                }
                guardarTransaccionSiExiste(conexion, turno);
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo guardar el turno " + turno.getIdTurno(), e);
        }
    }

    @Override
    public Optional<Turno> buscarPorId(String id) {
        return consultar("WHERE t.id_turno = ?", id).stream().findFirst();
    }

    @Override
    public List<Turno> listar() {
        return consultar("ORDER BY t.fecha, t.hora");
    }

    @Override
    public List<Turno> listarPorFecha(LocalDate fecha) {
        return consultar("WHERE t.fecha = ? ORDER BY t.hora", Date.valueOf(fecha));
    }

    // Actualiza el turno. Si se cobro con turno.cobrar(), guarda la transaccion en la misma operacion,
    // asi el cambio a COMPLETADO y el pago quedan guardados juntos o no se guarda ninguno.
    @Override
    public void actualizar(Turno turno) {
        try (Connection conexion = ConexionDatabase.getConnection()) {
            conexion.setAutoCommit(false);
            try {
                try (PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
                    sentencia.setString(1, turno.getCliente().getId());
                    sentencia.setString(2, turno.getEspecialista().getId());
                    sentencia.setString(3, turno.getServicio().getIdServicio());
                    sentencia.setDate(4, Date.valueOf(turno.getFecha()));
                    sentencia.setTime(5, Time.valueOf(turno.getHora()));
                    sentencia.setString(6, turno.getEstado().name());
                    sentencia.setString(7, turno.getIdTurno());
                    if (sentencia.executeUpdate() == 0) {
                        conexion.rollback();
                        throw new DaoException("No existe el turno " + turno.getIdTurno());
                    }
                }
                guardarTransaccionSiExiste(conexion, turno);
                conexion.commit();
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo actualizar el turno " + turno.getIdTurno(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, id);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            // Falla si el turno ya fue cobrado: la transaccion es un registro contable y no se borra.
            throw new DaoException("No se pudo eliminar el turno " + id, e);
        }
    }

    // Ejecuta la consulta base con la condicion y el orden indicados.
    // Tambien la usa TransaccionDaoJdbc, por eso no es privada.
    List<Turno> consultar(String condicion, Object... parametros) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(SELECT_TURNOS + condicion)) {
            for (int i = 0; i < parametros.length; i++) {
                sentencia.setObject(i + 1, parametros[i]);
            }

            List<Turno> turnos = new ArrayList<>();
            // Un mismo especialista se reutiliza en todos sus turnos para cargar sus horarios una sola vez.
            Map<String, Especialista> especialistas = new HashMap<>();
            try (ResultSet fila = sentencia.executeQuery()) {
                while (fila.next()) {
                    turnos.add(mapear(fila, especialistas));
                }
            }
            cargarHorarios(conexion, especialistas);
            return turnos;
        } catch (SQLException e) {
            throw new DaoException("No se pudo consultar los turnos", e);
        }
    }

    private void guardarTransaccionSiExiste(Connection conexion, Turno turno) throws SQLException {
        Transaccion transaccion = turno.getTransaccion();
        if (transaccion == null) {
            return;
        }
        try (PreparedStatement sentencia = conexion.prepareStatement(INSERTAR_TRANSACCION)) {
            sentencia.setString(1, transaccion.getIdFactura());
            sentencia.setString(2, turno.getIdTurno());
            sentencia.setBigDecimal(3, transaccion.getMontoTotal());
            sentencia.setString(4, transaccion.getMetodoPago().name());
            sentencia.setDate(5, Date.valueOf(transaccion.getFechaPago()));
            sentencia.executeUpdate();
        }
    }

    // Lee en una sola consulta los horarios de todos los especialistas que aparecieron en los turnos.
    private void cargarHorarios(Connection conexion, Map<String, Especialista> especialistas)
            throws SQLException {
        if (especialistas.isEmpty()) {
            return;
        }
        Array ids = conexion.createArrayOf("varchar", especialistas.keySet().toArray());
        try (PreparedStatement sentencia = conexion.prepareStatement(HORARIOS_DE_ESPECIALISTAS)) {
            sentencia.setArray(1, ids);
            try (ResultSet fila = sentencia.executeQuery()) {
                while (fila.next()) {
                    especialistas.get(fila.getString("especialista_id")).getHorarios().add(
                            new HorarioDisponibilidad(
                                    DayOfWeek.valueOf(fila.getString("dia")),
                                    fila.getTime("hora_inicio").toLocalTime(),
                                    fila.getTime("hora_fin").toLocalTime()));
                }
            }
        } finally {
            ids.free();
        }
    }

    private Turno mapear(ResultSet fila, Map<String, Especialista> especialistas) throws SQLException {
        Cliente cliente = new Cliente(
                fila.getString("c_id"),
                fila.getString("c_nombre"),
                fila.getString("c_telefono"),
                fila.getString("c_correo"),
                fila.getDate("c_fecha_registro").toLocalDate());

        Especialista especialista = especialistas.get(fila.getString("e_id"));
        if (especialista == null) {
            especialista = new Especialista(
                    fila.getString("e_id"),
                    fila.getString("e_nombre"),
                    fila.getString("e_telefono"),
                    fila.getString("e_correo"),
                    fila.getString("e_especialidad"),
                    new ArrayList<>());
            especialistas.put(especialista.getId(), especialista);
        }

        Servicio servicio = new Servicio(
                fila.getString("id_servicio"),
                fila.getString("nombre_servicio"),
                fila.getBigDecimal("precio"),
                fila.getInt("duracion_minutos"));

        Turno turno = new Turno(
                fila.getString("id_turno"),
                cliente,
                especialista,
                servicio,
                fila.getDate("fecha").toLocalDate(),
                fila.getTime("hora").toLocalTime(),
                EstadoTurno.valueOf(fila.getString("estado")));

        // Con LEFT JOIN, id_factura es null cuando el turno todavia no se ha cobrado.
        if (fila.getString("id_factura") != null) {
            turno.setTransaccion(new Transaccion(
                    fila.getString("id_factura"),
                    turno,
                    fila.getBigDecimal("monto_total"),
                    MetodoPago.valueOf(fila.getString("metodo_pago")),
                    fila.getDate("fecha_pago").toLocalDate()));
        }
        return turno;
    }
}
