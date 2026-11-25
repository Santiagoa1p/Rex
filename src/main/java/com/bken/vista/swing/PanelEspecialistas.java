package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Especialista;
import com.bken.model.HorarioDisponibilidad;
import com.bken.vista.Formato;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Registro de especialistas con sus horarios de atencion.
class PanelEspecialistas extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;

    private final DefaultTableModel modelo = Componentes.modeloTabla(
            "Id", "Nombre", "Especialidad", "Telefono", "Horarios");
    private final JTable tabla = Componentes.tabla(modelo);

    private final JTextField txtId = new JTextField(12);
    private final JTextField txtNombre = new JTextField(18);
    private final JTextField txtTelefono = new JTextField(12);
    private final JTextField txtCorreo = new JTextField(18);
    private final JTextField txtEspecialidad = new JTextField(18);

    private final JComboBox<String> cbDia = new JComboBox<>(Formato.nombresDias());
    private final JTextField txtDesde = new JTextField("09:00", 5);
    private final JTextField txtHasta = new JTextField("17:00", 5);
    private final DefaultListModel<String> modeloHorarios = new DefaultListModel<>();
    private final JList<String> listaHorarios = new JList<>(modeloHorarios);
    // Horarios que se van armando en el formulario (mismo orden que la lista).
    private final transient List<HorarioDisponibilidad> horarios = new ArrayList<>();

    // Especialistas cargados, para pasar sus datos completos al formulario al seleccionar una fila.
    private final transient Map<String, Especialista> cargados = new HashMap<>();

    PanelEspecialistas(Controladores controladores) {
        super(new BorderLayout(8, 8));
        this.controladores = controladores;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabla.getColumnModel().getColumn(4).setPreferredWidth(260);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.add(Componentes.formulario("Datos del especialista",
                new String[] {"Id", "Nombre", "Telefono", "Correo", "Especialidad"},
                new JComponent[] {txtId, txtNombre, txtTelefono, txtCorreo, txtEspecialidad}), BorderLayout.NORTH);
        lateral.add(crearPanelHorarios(), BorderLayout.CENTER);
        lateral.add(Componentes.filaDeBotones(
                Componentes.boton("Registrar", this::registrar),
                Componentes.boton("Guardar horarios", this::guardarHorarios),
                Componentes.boton("Eliminar", this::eliminar),
                Componentes.boton("Limpiar", this::limpiar)), BorderLayout.SOUTH);
        add(lateral, BorderLayout.EAST);

        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                cargarSeleccion(cargados.get(Componentes.valorSeleccionado(tabla, 0)));
            }
        });
    }

    private JPanel crearPanelHorarios() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBorder(BorderFactory.createTitledBorder("Horarios de atencion"));
        JPanel nuevo = new JPanel();
        nuevo.add(cbDia);
        nuevo.add(txtDesde);
        nuevo.add(txtHasta);
        nuevo.add(Componentes.boton("Agregar", this::agregarHorario));
        panel.add(nuevo, BorderLayout.NORTH);
        JScrollPane desplazable = new JScrollPane(listaHorarios);
        desplazable.setPreferredSize(new Dimension(260, 110));
        panel.add(desplazable, BorderLayout.CENTER);
        panel.add(Componentes.filaDeBotones(Componentes.boton("Quitar horario", this::quitarHorario)),
                BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void refrescar() {
        Respuesta<List<Especialista>> respuesta = controladores.especialistas().listar();
        modelo.setRowCount(0);
        cargados.clear();
        if (!Componentes.exitoOAvisar(this, respuesta)) {
            return;
        }
        for (Especialista especialista : respuesta.getDato()) {
            cargados.put(especialista.getId(), especialista);
            modelo.addRow(new Object[] {
                    especialista.getId(),
                    especialista.getNombre(),
                    especialista.getEspecialidad(),
                    Formato.valorOGuion(especialista.getTelefono()),
                    Formato.horarios(especialista.getHorarios())});
        }
    }

    private void cargarSeleccion(Especialista especialista) {
        if (especialista == null) {
            return;
        }
        txtId.setText(especialista.getId());
        txtNombre.setText(especialista.getNombre());
        txtTelefono.setText(especialista.getTelefono() == null ? "" : especialista.getTelefono());
        txtCorreo.setText(especialista.getCorreo() == null ? "" : especialista.getCorreo());
        txtEspecialidad.setText(especialista.getEspecialidad());
        horarios.clear();
        modeloHorarios.clear();
        for (HorarioDisponibilidad horario : especialista.getHorarios()) {
            horarios.add(horario);
            modeloHorarios.addElement(Formato.horario(horario));
        }
    }

    private void agregarHorario() {
        Respuesta<HorarioDisponibilidad> respuesta = controladores.especialistas().crearHorario(
                (String) cbDia.getSelectedItem(), txtDesde.getText(), txtHasta.getText());
        if (Componentes.exitoOAvisar(this, respuesta)) {
            horarios.add(respuesta.getDato());
            modeloHorarios.addElement(Formato.horario(respuesta.getDato()));
        }
    }

    private void quitarHorario() {
        int indice = listaHorarios.getSelectedIndex();
        if (indice < 0) {
            Componentes.error(this, "Selecciona un horario de la lista.");
            return;
        }
        horarios.remove(indice);
        modeloHorarios.remove(indice);
    }

    private void registrar() {
        if (Componentes.informar(this, controladores.especialistas().registrar(
                txtId.getText(), txtNombre.getText(), txtTelefono.getText(), txtCorreo.getText(),
                txtEspecialidad.getText(), horarios))) {
            limpiar();
            refrescar();
        }
    }

    // Reemplaza los horarios del especialista por los que estan en la lista.
    private void guardarHorarios() {
        if (Componentes.informar(this, controladores.especialistas().cambiarHorarios(txtId.getText(), horarios))) {
            refrescar();
        }
    }

    private void eliminar() {
        String id = txtId.getText().trim();
        if (id.isEmpty()) {
            Componentes.error(this, "Selecciona un especialista o escribe su id.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "Eliminar el especialista " + id + "?",
                "BkenS ERP", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION
                && Componentes.informar(this, controladores.especialistas().eliminar(id))) {
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
        txtEspecialidad.setText("");
        horarios.clear();
        modeloHorarios.clear();
    }
}
