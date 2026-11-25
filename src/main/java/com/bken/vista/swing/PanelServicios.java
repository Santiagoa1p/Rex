package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Servicio;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Catalogo de servicios que ofrece el centro.
class PanelServicios extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;

    private final DefaultTableModel modelo = Componentes.modeloTabla("Id", "Servicio", "Precio", "Duracion (min)");
    private final JTable tabla = Componentes.tabla(modelo);

    private final JTextField txtId = new JTextField(12);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtPrecio = new JTextField(12);
    private final JTextField txtDuracion = new JTextField(6);

    // Precio sin formato de cada fila, para pasarlo al formulario tal como se debe escribir.
    private final Map<String, String> preciosSinFormato = new HashMap<>();

    PanelServicios(Controladores controladores) {
        super(new BorderLayout(8, 8));
        this.controladores = controladores;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.add(Componentes.formulario("Datos del servicio",
                new String[] {"Id", "Nombre", "Precio (ej: 85000)", "Duracion (minutos)"},
                new JComponent[] {txtId, txtNombre, txtPrecio, txtDuracion}), BorderLayout.NORTH);
        lateral.add(Componentes.filaDeBotones(
                Componentes.boton("Registrar", this::registrar),
                Componentes.boton("Actualizar", this::actualizar),
                Componentes.boton("Eliminar", this::eliminar),
                Componentes.boton("Limpiar", this::limpiar)), BorderLayout.CENTER);
        add(lateral, BorderLayout.EAST);

        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                String id = Componentes.valorSeleccionado(tabla, 0);
                txtId.setText(id);
                txtNombre.setText(Componentes.valorSeleccionado(tabla, 1));
                txtPrecio.setText(preciosSinFormato.get(id));
                txtDuracion.setText(Componentes.valorSeleccionado(tabla, 3));
            }
        });
    }

    @Override
    public void refrescar() {
        Respuesta<List<Servicio>> respuesta = controladores.catalogo().listar();
        modelo.setRowCount(0);
        preciosSinFormato.clear();
        if (!Componentes.exitoOAvisar(this, respuesta)) {
            return;
        }
        for (Servicio servicio : respuesta.getDato()) {
            preciosSinFormato.put(servicio.getIdServicio(), servicio.getPrecio().stripTrailingZeros().toPlainString());
            modelo.addRow(new Object[] {
                    servicio.getIdServicio(),
                    servicio.getNombreServicio(),
                    Formato.pesos(servicio.getPrecio()),
                    servicio.getDuracionMinutos()});
        }
    }

    private void registrar() {
        if (Componentes.informar(this, controladores.catalogo().registrar(
                txtId.getText(), txtNombre.getText(), txtPrecio.getText(), txtDuracion.getText()))) {
            limpiar();
            refrescar();
        }
    }

    private void actualizar() {
        if (Componentes.informar(this, controladores.catalogo().actualizar(
                txtId.getText(), txtNombre.getText(), txtPrecio.getText(), txtDuracion.getText()))) {
            refrescar();
        }
    }

    private void eliminar() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            Componentes.error(this, "Selecciona un servicio o escribe su id.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "Eliminar el servicio " + id + "?",
                "BkenS ERP", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION
                && Componentes.informar(this, controladores.catalogo().eliminar(id))) {
            limpiar();
            refrescar();
        }
    }

    private void limpiar() {
        tabla.clearSelection();
        txtId.setText("");
        txtNombre.setText("");
        txtPrecio.setText("");
        txtDuracion.setText("");
    }
}
