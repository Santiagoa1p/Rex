package com.bken.dao;

import java.util.List;
import java.util.Optional;

// Contrato comun de persistencia para cualquier entidad.
public interface Dao<T> {
    void guardar(T entidad);

    Optional<T> buscarPorId(String id);

    List<T> listar();

    void actualizar(T entidad);

    void eliminar(String id);
}
