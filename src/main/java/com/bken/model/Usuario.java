package com.bken.model;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

// Cuenta de acceso al sistema. Nunca guarda la contrasena en texto plano.
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nombreUsuario;
    private String contrasenaHash;
    private Rol rol;
    private boolean activo;

    public Usuario() {
    }

    public Usuario(String id, String nombreUsuario, String contrasena, Rol rol) {
        this.id = id;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaHash = hash(contrasena);
        this.rol = rol;
        this.activo = true;
    }

    // Reconstruye un usuario leido de la base de datos, donde solo esta el hash.
    // No usa el constructor publico porque ese volveria a cifrar el hash.
    public static Usuario reconstruir(String id, String nombreUsuario, String contrasenaHash,
                                      Rol rol, boolean activo) {
        Usuario usuario = new Usuario();
        usuario.id = id;
        usuario.nombreUsuario = nombreUsuario;
        usuario.contrasenaHash = contrasenaHash;
        usuario.rol = rol;
        usuario.activo = activo;
        return usuario;
    }

    // Credenciales correctas y cuenta activa.
    public boolean autenticar(String nombreUsuario, String contrasena) {
        return activo
                && this.nombreUsuario.equals(nombreUsuario)
                && contrasenaHash.equals(hash(contrasena));
    }

    public boolean tieneRol(Rol rol) {
        return this.rol == rol;
    }

    private static String hash(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}