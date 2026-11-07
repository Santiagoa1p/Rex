package com.bken.dao;

import com.bken.model.Turno;

import java.time.LocalDate;
import java.util.List;

public interface TurnoDao extends Dao<Turno> {
    List<Turno> listarPorFecha(LocalDate fecha);
}
