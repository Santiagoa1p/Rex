package com.bken.servicio;

import com.bken.dao.UsuarioDao;
import com.bken.dao.jdbc.UsuarioDaoJdbc;
import com.bken.model.Rol;
import com.bken.model.Usuario;

import java.util.List;

// Registro de cuentas e inicio de sesion.
public class UsuarioServicio {

    static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final UsuarioDao usuarioDao;

    public UsuarioServicio() {
        this(new UsuarioDaoJdbc());
    }

    public UsuarioServicio(UsuarioDao usuarioDao) {
        this.usuarioDao = usuarioDao;
    }

    public Usuario registrar(String id, String nombreUsuario, String contrasena, Rol rol) {
        Validaciones.obligatorio(id, "id");
        Validaciones.obligatorio(nombreUsuario, "nombre de usuario");
        Validaciones.obligatorio(contrasena, "contrasena");
        Validaciones.obligatorio(rol, "rol");
        if (contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ReglaNegocioException(
                    "La contrasena debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        }
        if (usuarioDao.buscarPorId(id).isPresent()) {
            throw new ReglaNegocioException("Ya existe un usuario con el id " + id + ".");
        }
        if (usuarioDao.buscarPorNombreUsuario(nombreUsuario).isPresent()) {
            throw new ReglaNegocioException("El nombre de usuario " + nombreUsuario + " ya esta en uso.");
        }
        Usuario usuario = new Usuario(id, nombreUsuario, contrasena, rol);
        usuarioDao.guardar(usuario);
        return usuario;
    }

    // Devuelve el usuario si las credenciales son correctas y la cuenta esta activa.
    // El mensaje de error es el mismo en todos los casos para no revelar que nombres existen.
    public Usuario iniciarSesion(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null) {
            throw new ReglaNegocioException("Usuario o contrasena incorrectos.");
        }
        return usuarioDao.buscarPorNombreUsuario(nombreUsuario)
                .filter(usuario -> usuario.autenticar(nombreUsuario, contrasena))
                .orElseThrow(() -> new ReglaNegocioException("Usuario o contrasena incorrectos."));
    }

    public List<Usuario> listar() {
        return usuarioDao.listar();
    }

    public void desactivar(String id) {
        Usuario usuario = usuarioDao.buscarPorId(id)
                .orElseThrow(() -> new ReglaNegocioException("No existe el usuario " + id + "."));
        usuario.setActivo(false);
        usuarioDao.actualizar(usuario);
    }
}
