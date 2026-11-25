package com.bken.vista.swing;

import com.bken.controlador.CajaControlador;
import com.bken.controlador.CatalogoControlador;
import com.bken.controlador.ClienteControlador;
import com.bken.controlador.EspecialistaControlador;
import com.bken.controlador.SesionControlador;
import com.bken.controlador.TurnoControlador;

// Agrupa los controladores que usan las ventanas, para no pasarlos uno por uno.
public record Controladores(
        SesionControlador sesion,
        ClienteControlador clientes,
        EspecialistaControlador especialistas,
        CatalogoControlador catalogo,
        TurnoControlador turnos,
        CajaControlador caja) {

    // Controladores conectados a la base de datos de config.properties.
    public static Controladores conBaseDeDatos() {
        return new Controladores(new SesionControlador(), new ClienteControlador(),
                new EspecialistaControlador(), new CatalogoControlador(),
                new TurnoControlador(), new CajaControlador());
    }
}
