package com.bken.servicio;

import com.bken.dao.EspecialistaDao;
import com.bken.dao.jdbc.EspecialistaDaoJdbc;
import com.bken.model.Especialista;
import com.bken.model.HorarioDisponibilidad;

import java.util.List;

// Reglas de negocio para registrar especialistas y sus horarios de atencion.
public class EspecialistaServicio {

    private final EspecialistaDao especialistaDao;

    public EspecialistaServicio() {
        this(new EspecialistaDaoJdbc());
    }

    public EspecialistaServicio(EspecialistaDao especialistaDao) {
        this.especialistaDao = especialistaDao;
    }

    public void registrar(Especialista especialista) {
        validar(especialista);
        if (especialistaDao.buscarPorId(especialista.getId()).isPresent()) {
            throw new ReglaNegocioException("Ya existe un especialista con el id " + especialista.getId() + ".");
        }
        especialistaDao.guardar(especialista);
    }

    public Especialista buscar(String id) {
        return especialistaDao.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el especialista " + id + "."));
    }

    public List<Especialista> listar() {
        return especialistaDao.listar();
    }

    public void actualizar(Especialista especialista) {
        validar(especialista);
        buscar(especialista.getId());
        especialistaDao.actualizar(especialista);
    }

    public void eliminar(String id) {
        buscar(id);
        especialistaDao.eliminar(id);
    }

    private void validar(Especialista especialista) {
        Validaciones.obligatorio(especialista, "especialista");
        Validaciones.obligatorio(especialista.getId(), "id");
        Validaciones.obligatorio(especialista.getNombre(), "nombre");
        Validaciones.obligatorio(especialista.getEspecialidad(), "especialidad");
        Validaciones.telefonoOpcional(especialista.getTelefono());
        Validaciones.correoOpcional(especialista.getCorreo());
        validarHorarios(especialista.getHorarios());
    }

    // Cada horario debe estar completo, terminar despues de empezar
    // y no cruzarse con otro horario del mismo dia.
    private void validarHorarios(List<HorarioDisponibilidad> horarios) {
        if (horarios == null) {
            return;
        }
        for (int i = 0; i < horarios.size(); i++) {
            HorarioDisponibilidad horario = horarios.get(i);
            Validaciones.obligatorio(horario, "horario");
            Validaciones.obligatorio(horario.getDia(), "dia del horario");
            Validaciones.obligatorio(horario.getHoraInicio(), "hora de inicio");
            Validaciones.obligatorio(horario.getHoraFin(), "hora de fin");
            if (!horario.getHoraFin().isAfter(horario.getHoraInicio())) {
                throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio.");
            }
            for (int j = 0; j < i; j++) {
                HorarioDisponibilidad otro = horarios.get(j);
                boolean mismoDia = otro.getDia() == horario.getDia();
                boolean seCruzan = horario.getHoraInicio().isBefore(otro.getHoraFin())
                        && otro.getHoraInicio().isBefore(horario.getHoraFin());
                if (mismoDia && seCruzan) {
                    throw new ReglaNegocioException("Hay dos horarios que se cruzan el dia " + horario.getDia() + ".");
                }
            }
        }
    }
}
