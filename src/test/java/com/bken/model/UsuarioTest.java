package com.bken.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {
    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario("U001", "recepcion", "clave123", Rol.RECEPCIONISTA);
    }

    @Test
    void autenticaConCredencialesCorrectas() {
        assertTrue(usuario.autenticar("recepcion", "clave123"));
    }

    @Test
    void noAutenticaConContrasenaIncorrecta() {
        assertFalse(usuario.autenticar("recepcion", "otraClave"));
    }

    @Test
    void noAutenticaConUsuarioIncorrecto() {
        assertFalse(usuario.autenticar("otro", "clave123"));
    }

    @Test
    void noAutenticaUnaCuentaInactiva() {
        usuario.setActivo(false);
        assertFalse(usuario.autenticar("recepcion", "clave123"));
    }

    @Test
    void noGuardaLaContrasenaEnTextoPlano() {
        assertNotEquals("clave123", usuario.getContrasenaHash());
    }

    @Test
    void tieneRolIndicaElRolAsignado() {
        assertTrue(usuario.tieneRol(Rol.RECEPCIONISTA));
        assertFalse(usuario.tieneRol(Rol.ADMINISTRADOR));
    }
}