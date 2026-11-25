package com.bken.vista.swing;

import com.bken.model.Rol;
import com.bken.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

// Ventana principal despues de iniciar sesion. Cada rol ve solo sus pestanas:
// administrador todo, recepcionista turnos/clientes/caja y especialista solo la agenda.
public class VentanaPrincipal extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;
    private final JTabbedPane pestanas = new JTabbedPane();

    public VentanaPrincipal(Usuario usuario, Controladores controladores) {
        super("BkenS ERP");
        this.controladores = controladores;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBorder(BorderFactory.createEmptyBorder(8, 12, 0, 12));
        JLabel titulo = new JLabel("BkenS ERP");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        encabezado.add(titulo, BorderLayout.WEST);
        JPanel sesion = new JPanel();
        sesion.add(new JLabel(usuario.getNombreUsuario() + " (" + usuario.getRol() + ")"));
        sesion.add(Componentes.boton("Cerrar sesion", this::cerrarSesion));
        encabezado.add(sesion, BorderLayout.EAST);

        Rol rol = usuario.getRol();
        if (rol == Rol.ESPECIALISTA) {
            pestanas.addTab("Agenda", new PanelTurnos(controladores, true));
        } else {
            pestanas.addTab("Turnos", new PanelTurnos(controladores, false));
            pestanas.addTab("Clientes", new PanelClientes(controladores));
            if (rol == Rol.ADMINISTRADOR) {
                pestanas.addTab("Especialistas", new PanelEspecialistas(controladores));
                pestanas.addTab("Servicios", new PanelServicios(controladores));
            }
            pestanas.addTab("Caja", new PanelCaja(controladores));
            if (rol == Rol.ADMINISTRADOR) {
                pestanas.addTab("Usuarios", new PanelUsuarios(controladores, usuario));
            }
        }
        // Cada vez que se entra a una pestana se recargan sus datos desde la base de datos.
        pestanas.addChangeListener(evento -> refrescarPestanaActual());

        add(encabezado, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        setMinimumSize(new Dimension(1000, 600));
        setSize(1200, 680);
        setLocationRelativeTo(null);
        refrescarPestanaActual();
    }

    private void refrescarPestanaActual() {
        Component actual = pestanas.getSelectedComponent();
        if (actual instanceof Refrescable refrescable) {
            refrescable.refrescar();
        }
    }

    private void cerrarSesion() {
        dispose();
        VentanaLogin.abrir(controladores);
    }
}
