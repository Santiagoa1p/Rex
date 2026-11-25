package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

// Ventana de inicio de sesion. Si la base de datos no tiene usuarios,
// primero pide crear la cuenta del administrador.
public class VentanaLogin extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;
    private boolean crearAdministrador;

    private final JLabel lblIndicacion = new JLabel(" ", SwingConstants.CENTER);
    private final JTextField txtUsuario = new JTextField(16);
    private final JPasswordField txtContrasena = new JPasswordField(16);
    private final JButton btnAceptar = new JButton();

    // Revisa la conexion antes de mostrar la ventana. Si no hay conexion, avisa y no abre nada.
    public static void abrir(Controladores controladores) {
        Respuesta<Boolean> hayUsuarios = controladores.sesion().hayUsuarios();
        if (!hayUsuarios.isExito()) {
            Componentes.error(null, "No se pudo conectar con la base de datos:\n" + hayUsuarios.getMensaje());
            return;
        }
        new VentanaLogin(controladores, !hayUsuarios.getDato()).setVisible(true);
    }

    VentanaLogin(Controladores controladores, boolean crearAdministrador) {
        super("Iniciar sesion");
        this.controladores = controladores;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel titulo = new JLabel("BkenS ERP", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        JPanel arriba = new JPanel(new BorderLayout(4, 4));
        arriba.add(titulo, BorderLayout.NORTH);
        arriba.add(lblIndicacion, BorderLayout.CENTER);
        contenido.add(arriba, BorderLayout.NORTH);

        contenido.add(Componentes.formulario("Credenciales",
                new String[] {"Usuario", "Contrasena"},
                new JComponent[] {txtUsuario, txtContrasena}), BorderLayout.CENTER);

        btnAceptar.addActionListener(evento -> aceptar());
        btnAceptar.setPreferredSize(new Dimension(180, 30));
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pie.add(btnAceptar);
        contenido.add(pie, BorderLayout.SOUTH);

        setContentPane(contenido);
        // Enter en cualquier campo equivale a presionar el boton.
        getRootPane().setDefaultButton(btnAceptar);
        cambiarModo(crearAdministrador);
        pack();
        setLocationRelativeTo(null);
    }

    private void cambiarModo(boolean crearAdministrador) {
        this.crearAdministrador = crearAdministrador;
        if (crearAdministrador) {
            lblIndicacion.setText("Primera vez: crea la cuenta del administrador.");
            btnAceptar.setText("Crear administrador");
        } else {
            lblIndicacion.setText("Ingresa tu usuario y contrasena.");
            btnAceptar.setText("Ingresar");
        }
    }

    private void aceptar() {
        String usuario = txtUsuario.getText();
        String contrasena = new String(txtContrasena.getPassword());
        if (crearAdministrador) {
            if (Componentes.informar(this, controladores.sesion().crearAdministradorInicial(usuario, contrasena))) {
                txtContrasena.setText("");
                cambiarModo(false);
            }
            return;
        }
        Respuesta<Usuario> respuesta = controladores.sesion().iniciarSesion(usuario, contrasena);
        if (respuesta.isExito()) {
            dispose();
            new VentanaPrincipal(respuesta.getDato(), controladores).setVisible(true);
        } else {
            Componentes.error(this, respuesta.getMensaje());
            txtContrasena.setText("");
            txtContrasena.requestFocusInWindow();
        }
    }
}
