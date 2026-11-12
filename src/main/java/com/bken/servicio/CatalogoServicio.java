package com.bken.servicio;

import com.bken.dao.ServicioDao;
import com.bken.dao.jdbc.ServicioDaoJdbc;
import com.bken.model.Servicio;

import java.math.BigDecimal;
import java.util.List;

// Reglas de negocio del catalogo de servicios que ofrece el centro
// (se llama Catalogo para no repetir "ServicioServicio").
public class CatalogoServicio {

    // Un servicio no puede durar mas de una jornada de 8 horas.
    static final int DURACION_MAXIMA_MINUTOS = 480;

    private final ServicioDao servicioDao;

    public CatalogoServicio() {
        this(new ServicioDaoJdbc());
    }

    public CatalogoServicio(ServicioDao servicioDao) {
        this.servicioDao = servicioDao;
    }

    public void registrar(Servicio servicio) {
        validar(servicio);
        if (servicioDao.buscarPorId(servicio.getIdServicio()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un servicio con el id " + servicio.getIdServicio() + ".");
        }
        servicioDao.guardar(servicio);
    }

    public Servicio buscar(String id) {
        return servicioDao.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el servicio " + id + "."));
    }

    public List<Servicio> listar() {
        return servicioDao.listar();
    }

    public void actualizar(Servicio servicio) {
        validar(servicio);
        buscar(servicio.getIdServicio());
        servicioDao.actualizar(servicio);
    }

    public void eliminar(String id) {
        buscar(id);
        servicioDao.eliminar(id);
    }

    private void validar(Servicio servicio) {
        Validaciones.obligatorio(servicio, "servicio");
        Validaciones.obligatorio(servicio.getIdServicio(), "id");
        Validaciones.obligatorio(servicio.getNombreServicio(), "nombre del servicio");
        Validaciones.obligatorio(servicio.getPrecio(), "precio");
        if (servicio.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El precio debe ser mayor que cero.");
        }
        if (servicio.getDuracionMinutos() <= 0 || servicio.getDuracionMinutos() > DURACION_MAXIMA_MINUTOS) {
            throw new ReglaNegocioException(
                    "La duracion debe estar entre 1 y " + DURACION_MAXIMA_MINUTOS + " minutos.");
        }
    }
}
