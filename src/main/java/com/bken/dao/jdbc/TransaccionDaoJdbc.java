package com.bken.dao.jdbc;

import com.bken.config.ConexionDatabase;
import com.bken.dao.DaoException;
import com.bken.dao.TransaccionDao;
import com.bken.model.Transaccion;
import com.bken.model.Turno;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Persistencia de transacciones (pagos de turnos).
// Para leer, reutiliza la consulta de TurnoDaoJdbc: cada transaccion se devuelve con su turno completo.
public class TransaccionDaoJdbc implements TransaccionDao {

    private static final String INSERTAR =
            "INSERT INTO transacciones (id_factura, turno_id, monto_total, metodo_pago, fecha_pago)"
            + " VALUES (?, ?, ?, ?, ?)";
    private static final String ACTUALIZAR =
            "UPDATE transacciones SET monto_total = ?, metodo_pago = ?, fecha_pago = ? WHERE id_factura = ?";
    private static final String ELIMINAR =
            "DELETE FROM transacciones WHERE id_factura = ?";

    private final TurnoDaoJdbc turnoDao = new TurnoDaoJdbc();

    @Override
    public void guardar(Transaccion transaccion) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setString(1, transaccion.getIdFactura());
            sentencia.setString(2, transaccion.getTurno().getIdTurno());
            sentencia.setBigDecimal(3, transaccion.getMontoTotal());
            sentencia.setString(4, transaccion.getMetodoPago().name());
            sentencia.setDate(5, Date.valueOf(transaccion.getFechaPago()));
            sentencia.executeUpdate();
        } catch (SQLException e) {
            // Falla, por ejemplo, si el turno ya tiene una transaccion (turno_id es UNIQUE).
            throw new DaoException("No se pudo guardar la transaccion " + transaccion.getIdFactura(), e);
        }
    }

    @Override
    public Optional<Transaccion> buscarPorId(String id) {
        return turnoDao.consultar("WHERE tr.id_factura = ?", id).stream()
                .findFirst()
                .map(Turno::getTransaccion);
    }

    @Override
    public List<Transaccion> listar() {
        List<Transaccion> transacciones = new ArrayList<>();
        for (Turno turno : turnoDao.consultar("WHERE tr.id_factura IS NOT NULL ORDER BY tr.fecha_pago, tr.id_factura")) {
            transacciones.add(turno.getTransaccion());
        }
        return transacciones;
    }

    @Override
    public void actualizar(Transaccion transaccion) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
            sentencia.setBigDecimal(1, transaccion.getMontoTotal());
            sentencia.setString(2, transaccion.getMetodoPago().name());
            sentencia.setDate(3, Date.valueOf(transaccion.getFechaPago()));
            sentencia.setString(4, transaccion.getIdFactura());
            if (sentencia.executeUpdate() == 0) {
                throw new DaoException("No existe la transaccion " + transaccion.getIdFactura());
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo actualizar la transaccion " + transaccion.getIdFactura(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, id);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("No se pudo eliminar la transaccion " + id, e);
        }
    }
}
