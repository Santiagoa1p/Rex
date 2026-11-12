package com.bken.servicio;

import com.bken.dao.ClienteDao;
import com.bken.dao.Dao;
import com.bken.dao.DaoException;
import com.bken.dao.EspecialistaDao;
import com.bken.dao.ServicioDao;
import com.bken.dao.TransaccionDao;
import com.bken.dao.TurnoDao;
import com.bken.dao.UsuarioDao;
import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import com.bken.model.Turno;
import com.bken.model.Usuario;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// DAO falsos que guardan en un Map. Permiten probar los servicios sin conectarse a la base de datos.
final class DaosEnMemoria {

    private DaosEnMemoria() {
    }

    abstract static class Base<T> implements Dao<T> {
        protected final Map<String, T> datos = new LinkedHashMap<>();

        abstract String idDe(T entidad);

        @Override
        public void guardar(T entidad) {
            if (datos.containsKey(idDe(entidad))) {
                throw new DaoException("Id duplicado: " + idDe(entidad));
            }
            datos.put(idDe(entidad), entidad);
        }

        @Override
        public Optional<T> buscarPorId(String id) {
            return Optional.ofNullable(datos.get(id));
        }

        @Override
        public List<T> listar() {
            return new ArrayList<>(datos.values());
        }

        @Override
        public void actualizar(T entidad) {
            if (!datos.containsKey(idDe(entidad))) {
                throw new DaoException("No existe: " + idDe(entidad));
            }
            datos.put(idDe(entidad), entidad);
        }

        @Override
        public void eliminar(String id) {
            datos.remove(id);
        }
    }

    static class Clientes extends Base<Cliente> implements ClienteDao {
        @Override
        String idDe(Cliente cliente) {
            return cliente.getId();
        }
    }

    static class Especialistas extends Base<Especialista> implements EspecialistaDao {
        @Override
        String idDe(Especialista especialista) {
            return especialista.getId();
        }
    }

    static class Servicios extends Base<Servicio> implements ServicioDao {
        @Override
        String idDe(Servicio servicio) {
            return servicio.getIdServicio();
        }
    }

    static class Turnos extends Base<Turno> implements TurnoDao {
        @Override
        String idDe(Turno turno) {
            return turno.getIdTurno();
        }

        @Override
        public List<Turno> listarPorFecha(LocalDate fecha) {
            List<Turno> delDia = new ArrayList<>();
            for (Turno turno : datos.values()) {
                if (turno.getFecha().equals(fecha)) {
                    delDia.add(turno);
                }
            }
            return delDia;
        }
    }

    static class Transacciones extends Base<Transaccion> implements TransaccionDao {
        @Override
        String idDe(Transaccion transaccion) {
            return transaccion.getIdFactura();
        }
    }

    static class Usuarios extends Base<Usuario> implements UsuarioDao {
        @Override
        String idDe(Usuario usuario) {
            return usuario.getId();
        }

        @Override
        public Optional<Usuario> buscarPorNombreUsuario(String nombreUsuario) {
            for (Usuario usuario : datos.values()) {
                if (usuario.getNombreUsuario().equals(nombreUsuario)) {
                    return Optional.of(usuario);
                }
            }
            return Optional.empty();
        }
    }
}
