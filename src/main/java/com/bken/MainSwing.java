package com.bken;

import com.bken.vista.swing.Controladores;
import com.bken.vista.swing.VentanaLogin;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Punto de entrada de la interfaz grafica (ventanas Swing).
// La interfaz de consola sigue disponible en Main.
public class MainSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Usa el aspecto de ventanas del sistema operativo (Windows).
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
                // Si no se puede, Swing usa su aspecto por defecto.
            }
            VentanaLogin.abrir(Controladores.conBaseDeDatos());
        });
    }
}
