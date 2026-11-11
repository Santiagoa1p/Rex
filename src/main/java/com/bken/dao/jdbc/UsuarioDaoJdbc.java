package com.bken.dao.jdbc;

import com.bken.config.ConexionDatabase;
import com.bken.dao.DaoException;
import com.bken.dao.UsuarioDao;
import com.bken.model.Rol;
import com.bken.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Persistencia de usuarios. En la base solo se guarda el hash de la contrasena.
public class UsuarioDaoJdbc implements UsuarioDao {

    private static final String COLUMNAS = "id, nombre_usuario, contrasena_hash, rol, activo";

    private static final String INSERTAR =
            "INSERT INTO usuarios (" + COLUMNAS + ") VALUES (?, ?, ?, ?, ?)";
    private static final String BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM usuarios WHERE id = ?";
    private static final String BUSCAR_POR_NOMBRE_USUARIO =
            "SELECT " + COLUMNAS + " FROM usuarios WHERE nombre_usuario = ?";
    private static final String LISTAR =
            "SELECT " + COLUMNAS + " FROM usuarios ORDER BY nombre_usuario";
    private static final String ACTUALIZAR =
            "UPDATE usuarios SET nombre_usuario = ?, contrasena_hash = ?, rol = ?, activo = ? WHERE id = ?";
    private static final String ELIMINAR =
            "DELETE FROM usuarios WHERE id = ?";

    @Override
    public void guardar(Usuario usuario) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)) {
            sentencia.setString(1, usuario.getId());
            sentencia.setString(2, usuario.getNombreUsuario());
            sentencia.setString(3, usuario.getContrasenaHash());
            sentencia.setString(4, usuario.getRol().name());
            sentencia.setBoolean(5, usuario.isActivo());
            sentencia.executeUpdate();
        } catch (SQLException e) {
            // Falla, por ejemplo, si el nombre de usuario ya existe (es UNIQUE).
            throw new DaoException("No se pudo guardar el usuario " + usuario.getNombreUsuario(), e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return buscarUno(BUSCAR_POR_ID, id);
    }

    // Se usa para iniciar sesion: se busca por nombre y luego se llama a usuario.autenticar(...).
    @Override
    public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
        return buscarUno(BUSCAR_POR_NOMBRE_USUARIO, nombreUsuario);
    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(LISTAR);
             ResultSet fila = sentencia.executeQuery()) {
            while (fila.next()) {
                usuarios.add(mapear(fila));
            }
            return usuarios;
        } catch (SQLException e) {
            throw new DaoException("No se pudo listar los usuarios", e);
        }
    }

    @Override
    public void actualizar(Usuario usuario) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ACTUALIZAR)) {
            sentencia.setString(1, usuario.getNombreUsuario());
            sentencia.setString(2, usuario.getContrasenaHash());
            sentencia.setString(3, usuario.getRol().name());
            sentencia.setBoolean(4, usuario.isActivo());
            sentencia.setString(5, usuario.getId());
            if (sentencia.executeUpdate() == 0) {
                throw new DaoException("No existe el usuario " + usuario.getId());
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo actualizar el usuario " + usuario.getId(), e);
        }
    }

    @Override
    public void eliminar(String id) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(ELIMINAR)) {
            sentencia.setString(1, id);
            sentencia.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("No se pudo eliminar el usuario " + id, e);
        }
    }

    private Optional<Usuario> buscarUno(String consulta, String valor) {
        try (Connection conexion = ConexionDatabase.getConnection();
             PreparedStatement sentencia = conexion.prepareStatement(consulta)) {
            sentencia.setString(1, valor);
            try (ResultSet fila = sentencia.executeQuery()) {
                return fila.next() ? Optional.of(mapear(fila)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("No se pudo buscar el usuario " + valor, e);
        }
    }

    // Usa Usuario.reconstruir porque en la base solo esta el hash, no la contrasena.
    private Usuario mapear(ResultSet fila) throws SQLException {
        return Usuario.reconstruir(
                fila.getString("id"),
                fila.getString("nombre_usuario"),
                fila.getString("contrasena_hash"),
                Rol.valueOf(fila.getString("rol")),
                fila.getBoolean("activo"));
    }
}
