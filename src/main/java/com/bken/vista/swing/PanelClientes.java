package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Cliente;
import com.bken.vista.Formato;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

// Registro y consulta de clientes. Al seleccionar una fila, sus datos pasan al formulario para editarlos.
class PanelClientes extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;

    private final DefaultTableModel modelo = Componentes.modeloTabla(
            "Id", "Nombre", "Telefono", "Correo", "Fecha de registro");
    private final JTable tabla = Componentes.tabla(modelo);

    private final JTextField txtId = new JTextField(12);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtTelefono = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(18);

    PanelClientes(Controladores controladores) {
        super(new BorderLayout(8, 8));
        this.controladores = controladores;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.add(Componentes.formulario("Datos del cliente",
                new String[] {"Id", "Nombre", "Telefono", "Correo"},
                new JComponent[] {txtId, txtNombre, txtTelefono, txtCorreo}), BorderLayout.NORTH);
        lateral.add(Componentes.filaDeBotones(
                Componentes.boton("Registrar", this::registrar),
                Componentes.boton("Actualizar", this::actualizar),
                Componentes.boton("Eliminar", this::eliminar),
                Componentes.boton("Limpiar", this::limpiar)), BorderLayout.CENTER);
        add(lateral, BorderLayout.EAST);

        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });
    }

    @Override
    public void refrescar() {
        Respuesta<List<Cliente>> respuesta = controladores.clientes().listar();
        modelo.setRowCount(0);
        if (!Componentes.exitoOAvisar(this, respuesta)) {
            return;
        }
        for (Cliente cliente : respuesta.getDato()) {
            modelo.addRow(new Object[] {
                    cliente.getId(),
                    cliente.getNombre(),
                    Formato.valorOGuion(cliente.getTelefono()),
                    Formato.valorOGuion(cliente.getCorreo()),
                    Formato.fecha(cliente.getFechaRegistro())});
        }
    }

    private void cargarSeleccion() {
        if (tabla.getSelectedRow() < 0) {
            return;
        }
        txtId.setText(Componentes.valorSeleccionado(tabla, 0));
        txtNombre.setText(Componentes.valorSeleccionado(tabla, 1));
        txtTelefono.setText(sinGuion(Componentes.valorSeleccionado(tabla, 2)));
        txtCorreo.setText(sinGuion(Componentes.valorSeleccionado(tabla, 3)));
    }

    private void registrar() {
        if (Componentes.informar(this, controladores.clientes().registrar(
                txtId.getText(), txtNombre.getText(), txtTelefono.getText(), txtCorreo.getText()))) {
            limpiar();
            refrescar();
        }
    }

    private void actualizar() {
        if (Componentes.informar(this, controladores.clientes().actualizar(
                txtId.getText(), txtNombre.getText(), txtTelefono.getText(), txtCorreo.getText()))) {
            refrescar();
        }
    }

    private void eliminar() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            Componentes.error(this, "Selecciona un cliente o escribe su id.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "Eliminar el cliente " + id + "?",
                "BkenS ERP", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION
                && Componentes.informar(this, controladores.clientes().eliminar(id))) {
            limpiar();
            refrescar();
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        txtId.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
    }

    private static String sinGuion(String valor) {
        return "-".equals(valor) ? "" : valor;
    }
}
