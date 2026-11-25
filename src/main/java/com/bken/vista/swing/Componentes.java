package com.bken.vista.swing;

import com.bken.controlador.Respuesta;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

// Piezas que se repiten en todas las pantallas: tablas, formularios, botones y mensajes.
final class Componentes {

    private Componentes() {
    }

    // Modelo de tabla de solo lectura: los datos se cambian con los formularios, no escribiendo en la tabla.
    static DefaultTableModel modeloTabla(String... columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    static JTable tabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setAutoCreateRowSorter(true);
        tabla.setRowHeight(24);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    // Valor de la columna indicada en la fila seleccionada, o null si no hay fila seleccionada.
    static String valorSeleccionado(JTable tabla, int columna) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            return null;
        }
        Object valor = tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), columna);
        return valor == null ? null : valor.toString();
    }

    // Formulario de dos columnas: etiqueta a la izquierda y campo a la derecha.
    static JPanel formulario(String titulo, String[] etiquetas, JComponent[] campos) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo), BorderFactory.createEmptyBorder(4, 8, 8, 8)));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        for (int i = 0; i < etiquetas.length; i++) {
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            panel.add(new JLabel(etiquetas[i]), c);
            c.gridx = 1;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            panel.add(campos[i], c);
        }
        return panel;
    }

    static JButton boton(String texto, Runnable accion) {
        JButton boton = new JButton(texto);
        boton.addActionListener(evento -> accion.run());
        return boton;
    }

    static JPanel filaDeBotones(JButton... botones) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        for (JButton boton : botones) {
            panel.add(boton);
        }
        return panel;
    }

    // Muestra el resultado de una accion (registrar, eliminar...) y dice si salio bien.
    static boolean informar(Component padre, Respuesta<?> respuesta) {
        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(padre, respuesta.getMensaje(), "BkenS ERP", JOptionPane.INFORMATION_MESSAGE);
        } else {
            error(padre, respuesta.getMensaje());
        }
        return respuesta.isExito();
    }

    // Para consultas: solo se avisa si algo fallo.
    static boolean exitoOAvisar(Component padre, Respuesta<?> respuesta) {
        if (!respuesta.isExito()) {
            error(padre, respuesta.getMensaje());
        }
        return respuesta.isExito();
    }

    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "BkenS ERP", JOptionPane.ERROR_MESSAGE);
    }

    // De un elemento de lista como "C001 - Ana Torres" devuelve solo el id "C001".
    static String idDe(Object elemento) {
        if (elemento == null) {
            return null;
        }
        String texto = elemento.toString();
        int separador = texto.indexOf(" - ");
        return separador < 0 ? texto : texto.substring(0, separador);
    }
}
