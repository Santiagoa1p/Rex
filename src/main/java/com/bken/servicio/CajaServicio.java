package com.bken.servicio;

import com.bken.dao.TransaccionDao;
import com.bken.dao.jdbc.TransaccionDaoJdbc;
import com.bken.model.Transaccion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Consultas de caja: pagos registrados y totales.
// Los pagos se crean al cobrar un turno (TurnoServicio.cobrar).
public class CajaServicio {

    private final TransaccionDao transaccionDao;

    public CajaServicio() {
        this(new TransaccionDaoJdbc());
    }

    public CajaServicio(TransaccionDao transaccionDao) {
        this.transaccionDao = transaccionDao;
    }

    public List<Transaccion> listar() {
        return transaccionDao.listar();
    }

    public List<Transaccion> listarPorFecha(LocalDate fecha) {
        Validaciones.obligatorio(fecha, "fecha");
        List<Transaccion> delDia = new ArrayList<>();
        for (Transaccion transaccion : transaccionDao.listar()) {
            if (transaccion.getFechaPago().equals(fecha)) {
                delDia.add(transaccion);
            }
        }
        return delDia;
    }

    public BigDecimal totalPorFecha(LocalDate fecha) {
        return sumar(listarPorFecha(fecha));
    }

    public BigDecimal totalGeneral() {
        return sumar(listar());
    }

    private BigDecimal sumar(List<Transaccion> transacciones) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaccion transaccion : transacciones) {
            total = total.add(transaccion.getMontoTotal());
        }
        return total;
    }
}
