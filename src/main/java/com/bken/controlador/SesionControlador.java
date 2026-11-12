package com.bken.controlador;

import com.bken.model.Rol;
import com.bken.model.Usuario;
import com.bken.servicio.ReglaNegocioException;
import com.bken.servicio.UsuarioServicio;

import java.util.List;

// Inicio de sesion y administracion de cuentas de usuario.
public class SesionControlador {

    // Id que recibe el primer administrador, creado cuando la tabla usuarios esta vacia.
    static final String ID_ADMINISTRADOR_INICIAL = "U001";

    private final UsuarioServicio servicio;

    public SesionControlador() {
        this(new UsuarioServicio());
    }

    public SesionControlador(UsuarioServicio servicio) {
        this.servicio = servicio;
    }

    // La primera vez que se usa el sistema no hay usuarios: la vista debe pedir crear un administrador.
    public Respuesta<Boolean> hayUsuarios() {
        return Respuesta.de(() -> !servicio.listar().isEmpty(), "Usuarios consultados.");
    }

    public Respuesta<Usuario> crearAdministradorInicial(String nombreUsuario, String contrasena) {
        return Respuesta.de(() -> {
            if (!servicio.listar().isEmpty()) {
                throw new ReglaNegocioException("Ya existe al menos un usuario en el sistema.");
            }
            return servicio.registrar(ID_ADMINISTRADOR_INICIAL, Conversor.texto(nombreUsuario), contrasena,
                    Rol.ADMINISTRADOR);
        }, "Administrador creado. Ya puedes iniciar sesion.");
    }

    public Respuesta<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        return Respuesta.de(() -> servicio.iniciarSesion(Conversor.texto(nombreUsuario), contrasena),
                "Bienvenido.");
    }

    public Respuesta<Usuario> registrarUsuario(String id, String nombreUsuario, String contrasena, String rol) {
        return Respuesta.de(() -> servicio.registrar(Conversor.texto(id), Conversor.texto(nombreUsuario),
                contrasena, Conversor.rol(rol)), "Usuario registrado.");
    }

    public Respuesta<List<Usuario>> listarUsuarios() {
        return Respuesta.de(servicio::listar, "Usuarios del sistema.");
    }

    public Respuesta<Void> desactivar(String id) {
        return Respuesta.deAccion(() -> servicio.desactivar(Conversor.texto(id)), "Usuario desactivado.");
    }
}
