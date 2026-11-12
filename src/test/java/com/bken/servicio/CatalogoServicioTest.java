package com.bken.servicio;

import com.bken.model.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogoServicioTest {
    private CatalogoServicio catalogo;

    @BeforeEach
    void preparar() {
        catalogo = new CatalogoServicio(new DaosEnMemoria.Servicios());
    }

    @Test
    void registraUnServicioValido() {
        catalogo.registrar(new Servicio("S001", "Limpieza facial", new BigDecimal("85000"), 60));
        assertEquals(1, catalogo.listar().size());
    }

    @Test
    void rechazaPrecioCeroONegativo() {
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrar(new Servicio("S001", "Limpieza", BigDecimal.ZERO, 60)));
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrar(new Servicio("S002", "Limpieza", new BigDecimal("-1"), 60)));
    }

    @Test
    void rechazaDuracionFueraDeRango() {
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrar(new Servicio("S001", "Limpieza", BigDecimal.TEN, 0)));
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrar(new Servicio("S002", "Limpieza", BigDecimal.TEN,
                        CatalogoServicio.DURACION_MAXIMA_MINUTOS + 1)));
    }

    @Test
    void rechazaUnServicioSinNombre() {
        assertThrows(ReglaNegocioException.class,
                () -> catalogo.registrar(new Servicio("S001", "", BigDecimal.TEN, 30)));
    }
}
