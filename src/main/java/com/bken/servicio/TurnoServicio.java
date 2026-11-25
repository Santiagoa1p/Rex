package com.bken.servicio;

import com.bken.dao.ClienteDao;
import com.bken.dao.EspecialistaDao;
import com.bken.dao.ServicioDao;
import com.bken.dao.TurnoDao;
import com.bken.dao.jdbc.ClienteDaoJdbc;
import com.bken.dao.jdbc.EspecialistaDaoJdbc;
import com.bken.dao.jdbc.ServicioDaoJdbc;
import com.bken.dao.jdbc.TurnoDaoJdbc;
import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.EstadoTurno;
import com.bken.model.MetodoPago;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import com.bken.model.Turno;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

// Reglas de negocio de la agenda: agendar, confirmar, cancelar y cobrar turnos.
public class TurnoServicio {

    private final TurnoDao turnoDao;
    private final ClienteDao clienteDao;
    private final EspecialistaDao especialistaDao;
    private final ServicioDao servicioDao;

    public TurnoServicio() {
        this(new TurnoDaoJdbc(), new ClienteDaoJdbc(), new EspecialistaDaoJdbc(), new ServicioDaoJdbc());
    }

    public TurnoServicio(TurnoDao turnoDao, ClienteDao clienteDao,
                         EspecialistaDao especialistaDao, ServicioDao servicioDao) {
        this.turnoDao = turnoDao;
        this.clienteDao = clienteDao;
        this.especialistaDao = especialistaDao;
        this.servicioDao = servicioDao;
    }

    // Crea un turno PENDIENTE si el especialista atiende a esa hora
    // y ni el especialista ni el cliente tienen otro turno que se cruce.
    public Turno agendar(String idTurno, String idCliente, String idEspecialista, String idServicio,
                         LocalDate fecha, LocalTime hora) {
        Validaciones.obligatorio(idTurno, "id del turno");
        Validaciones.obligatorio(fecha, "fecha");
        Validaciones.obligatorio(hora, "hora");
        if (turnoDao.buscarPorId(idTurno).isPresent()) {
            throw new ReglaNegocioException("Ya existe un turno con el id " + idTurno + ".");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("No se puede agendar un turno en una fecha pasada.");
        }

        Cliente cliente = clienteDao.buscarPorId(idCliente)
                .orElseThrow(() -> new ReglaNegocioException("No existe el cliente " + idCliente + "."));
        Especialista especialista = especialistaDao.buscarPorId(idEspecialista)
                .orElseThrow(() -> new ReglaNegocioException("No existe el especialista " + idEspecialista + "."));
        Servicio servicio = servicioDao.buscarPorId(idServicio)
                .orElseThrow(() -> new ReglaNegocioException("No existe el servicio " + idServicio + "."));

        if (!especialista.estaDisponible(fecha, hora, servicio.getDuracionMinutos())) {
            throw new ReglaNegocioException(
                    especialista.getNombre() + " no atiende el " + fecha + " a las " + hora + ".");
        }

        LocalTime fin = hora.plusMinutes(servicio.getDuracionMinutos());
        for (Turno otro : turnoDao.listarPorFecha(fecha)) {
            if (otro.getEstado() == EstadoTurno.CANCELADO || !seCruzan(otro, hora, fin)) {
                continue;
            }
            if (otro.getEspecialista().getId().equals(idEspecialista)) {
                throw new ReglaNegocioException(
                        especialista.getNombre() + " ya tiene un turno a las " + otro.getHora() + ".");
            }
            if (otro.getCliente().getId().equals(idCliente)) {
                throw new ReglaNegocioException(
                        cliente.getNombre() + " ya tiene un turno a las " + otro.getHora() + ".");
            }
        }

        Turno turno = new Turno(idTurno, cliente, especialista, servicio, fecha, hora, EstadoTurno.PENDIENTE);
        turnoDao.guardar(turno);
        return turno;
    }

    public Turno buscar(String idTurno) {
        return turnoDao.buscarPorId(idTurno)
                .orElseThrow(() -> new ReglaNegocioException("No existe el turno " + idTurno + "."));
    }

    public List<Turno> agendaDelDia(LocalDate fecha) {
        Validaciones.obligatorio(fecha, "fecha");
        return turnoDao.listarPorFecha(fecha);
    }

    public List<Turno> listar() {
        return turnoDao.listar();
    }

    public void confirmar(String idTurno) {
        Turno turno = buscar(idTurno);
        try {
            turno.confirmar();
        } catch (IllegalStateException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
        turnoDao.actualizar(turno);
    }

    public void cancelar(String idTurno) {
        Turno turno = buscar(idTurno);
        try {
            turno.cancelar();
        } catch (IllegalStateException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
        turnoDao.actualizar(turno);
    }

    // Cobra un turno confirmado. El DAO guarda el turno COMPLETADO y la transaccion juntos.
    public Transaccion cobrar(String idTurno, String idFactura, MetodoPago metodoPago, LocalDate fechaPago) {
        Validaciones.obligatorio(idFactura, "numero de factura");
        Validaciones.obligatorio(metodoPago, "metodo de pago");
        Validaciones.obligatorio(fechaPago, "fecha de pago");
        Turno turno = buscar(idTurno);
        Transaccion transaccion;
        try {
            transaccion = turno.cobrar(idFactura, metodoPago, fechaPago);
        } catch (IllegalStateException e) {
            throw new ReglaNegocioException(e.getMessage());
        }
        turnoDao.actualizar(turno);
        return transaccion;
    }

    // Dos franjas se cruzan si cada una empieza antes de que termine la otra.
    private boolean seCruzan(Turno otro, LocalTime inicio, LocalTime fin) {
        LocalTime otroFin = otro.getHora().plusMinutes(otro.getServicio().getDuracionMinutos());
        return inicio.isBefore(otroFin) && otro.getHora().isBefore(fin);
    }
}
