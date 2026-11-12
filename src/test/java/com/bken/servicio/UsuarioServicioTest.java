package com.bken.servicio;

import com.bken.model.Rol;
import com.bken.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsuarioServicioTest {
    private UsuarioServicio servicio;

    @BeforeEach
    void preparar() {
        servicio = new UsuarioServicio(new DaosEnMemoria.Usuarios());
        servicio.registrar("U001", "recepcion", "clave123", Rol.RECEPCIONISTA);
    }

    @Test
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = servicio.iniciarSesion("recepcion", "clave123");
        assertEquals(Rol.RECEPCIONISTA, usuario.getRol());
    }

    @Test
    void noIniciaSesionConContrasenaIncorrecta() {
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciarSesion("recepcion", "otra"));
    }

    @Test
    void noIniciaSesionConUsuarioInexistente() {
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciarSesion("nadie", "clave123"));
    }

    @Test
    void noIniciaSesionConUnaCuentaDesactivada() {
        servicio.desactivar("U001");
        assertThrows(ReglaNegocioException.class, () -> servicio.iniciarSesion("recepcion", "clave123"));
    }

    @Test
    void rechazaUnNombreDeUsuarioRepetido() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar("U002", "recepcion", "otraClave", Rol.ESPECIALISTA));
    }

    @Test
    void rechazaUnaContrasenaCorta() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar("U002", "caja", "123", Rol.RECEPCIONISTA));
    }
}
