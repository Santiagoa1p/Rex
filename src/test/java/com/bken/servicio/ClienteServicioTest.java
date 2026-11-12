package com.bken.servicio;

import com.bken.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClienteServicioTest {
    private ClienteServicio servicio;

    @BeforeEach
    void preparar() {
        servicio = new ClienteServicio(new DaosEnMemoria.Clientes());
    }

    private Cliente cliente(String id) {
        return new Cliente(id, "Ana Torres", "3001234567", "ana@correo.com", LocalDate.of(2026, 11, 1));
    }

    @Test
    void registraUnClienteValido() {
        servicio.registrar(cliente("C001"));
        assertEquals("Ana Torres", servicio.buscar("C001").getNombre());
    }

    @Test
    void asignaLaFechaDeHoySiNoTieneFechaDeRegistro() {
        Cliente cliente = cliente("C001");
        cliente.setFechaRegistro(null);
        servicio.registrar(cliente);
        assertEquals(LocalDate.now(), servicio.buscar("C001").getFechaRegistro());
    }

    @Test
    void rechazaUnClienteSinNombre() {
        Cliente cliente = cliente("C001");
        cliente.setNombre("  ");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(cliente));
    }

    @Test
    void rechazaUnCorreoInvalido() {
        Cliente cliente = cliente("C001");
        cliente.setCorreo("ana.correo.com");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(cliente));
    }

    @Test
    void rechazaUnTelefonoConLetras() {
        Cliente cliente = cliente("C001");
        cliente.setTelefono("300-ABC");
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(cliente));
    }

    @Test
    void rechazaUnIdRepetido() {
        servicio.registrar(cliente("C001"));
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(cliente("C001")));
    }

    @Test
    void noActualizaNiEliminaUnClienteInexistente() {
        assertThrows(ReglaNegocioException.class, () -> servicio.actualizar(cliente("C999")));
        assertThrows(ReglaNegocioException.class, () -> servicio.eliminar("C999"));
    }
}
