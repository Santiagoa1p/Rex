package com.bken.dao;

import com.bken.model.Usuario;

import java.util.Optional;

public interface UsuarioDao extends Dao<Usuario> {
    Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario);
}