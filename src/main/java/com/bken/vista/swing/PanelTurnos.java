package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.MetodoPago;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import com.bken.model.Turno;
import com.bken.vista.Formato;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

// Agenda: tabla de turnos con filtros por dia, formulario para agendar
// y botones para confirmar, cancelar y cobrar el turno seleccionado.
// En modo solo lectura (rol especialista) solo se consulta la agenda.
class PanelTurnos extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;
    private final boolean soloLectura;

    private final DefaultTableModel modelo = Componentes.modeloTabla(
            "Id", "Fecha", "Hora", "Servicio", "Cliente", "Especialista", "Estado");
    private final JTable tabla = Componentes.tabla(modelo);
    private final JTextField txtFiltroFecha = new JTextField(Formato.fecha(LocalDate.now()), 10);
    private final JLabel lblTitulo = new JLabel();

    private final JTextField txtId = new JTextField(10);
    private final JComboBox<String> cbCliente = new JComboBox<>();
    private final JComboBox<String> cbEspecialista = new JComboBox<>();
    private final JComboBox<String> cbServicio = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(Formato.fecha(LocalDate.now()), 10);
    private final JTextField txtHora = new JTextField("09:00", 6);

    // Ultima consulta de la tabla, para repetirla despues de confirmar, cancelar o cobrar.
    private transient Supplier<Respuesta<List<Turno>>> consultaActual;
    private String tituloActual;

    PanelTurnos(Controladores controladores, boolean soloLectura) {
        super(new BorderLayout(8, 8));
        this.controladores = controladores;
        this.soloLectura = soloLectura;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtros.add(new JLabel("Fecha (dd/mm/aaaa):"));
        filtros.add(txtFiltroFecha);
        filtros.add(Componentes.boton("Ver dia", this::verDia));
        filtros.add(Componentes.boton("Hoy", this::verHoy));
        if (!soloLectura) {
            filtros.add(Componentes.boton("Ver todos", this::verTodos));
        }
        filtros.add(lblTitulo);
        add(filtros, BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout());
        centro.add(new JScrollPane(tabla), BorderLayout.CENTER);
        if (!soloLectura) {
            centro.add(Componentes.filaDeBotones(
                    Componentes.boton("Confirmar", this::confirmar),
                    Componentes.boton("Cancelar turno", this::cancelar),
                    Componentes.boton("Cobrar", this::cobrar)), BorderLayout.SOUTH);
        }
        add(centro, BorderLayout.CENTER);

        if (!soloLectura) {
            add(crearFormularioAgendar(), BorderLayout.EAST);
        }
        consultaActual = controladores.turnos()::agendaDeHoy;
        tituloActual = "Agenda de hoy";
    }

    private JPanel crearFormularioAgendar() {
        JPanel formulario = Componentes.formulario("Agendar turno",
                new String[] {"Id del turno", "Cliente", "Especialista", "Servicio", "Fecha", "Hora (HH:mm)"},
                new JComponent[] {txtId, cbCliente, cbEspecialista, cbServicio, txtFecha, txtHora});
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(formulario, BorderLayout.NORTH);
        JButton agendar = Componentes.boton("Agendar", this::agendar);
        contenedor.add(Componentes.filaDeBotones(agendar), BorderLayout.CENTER);
        return contenedor;
    }

    @Override
    public void refrescar() {
        if (!soloLectura) {
            cargarListas();
        }
        cargarTabla();
    }

    private void verDia() {
        String fecha = txtFiltroFecha.getText();
        consultaActual = () -> controladores.turnos().agendaDelDia(fecha);
        tituloActual = "Agenda del " + fecha.trim();
        cargarTabla();
    }

    private void verHoy() {
        txtFiltroFecha.setText(Formato.fecha(LocalDate.now()));
        consultaActual = controladores.turnos()::agendaDeHoy;
        tituloActual = "Agenda de hoy";
        cargarTabla();
    }

    private void verTodos() {
        consultaActual = controladores.turnos()::listar;
        tituloActual = "Todos los turnos";
        cargarTabla();
    }

    private void cargarTabla() {
        Respuesta<List<Turno>> respuesta = consultaActual.get();
        modelo.setRowCount(0);
        if (!Componentes.exitoOAvisar(this, respuesta)) {
            return;
        }
        for (Turno turno : respuesta.getDato()) {
            modelo.addRow(new Object[] {
                    turno.getIdTurno(),
                    Formato.fecha(turno.getFecha()),
                    turno.getHora().toString(),
                    turno.getServicio().getNombreServicio(),
                    turno.getCliente().getNombre(),
                    turno.getEspecialista().getNombre(),
                    turno.getEstado().getEtiqueta()});
        }
        lblTitulo.setText("   " + tituloActual + ": " + respuesta.getDato().size() + " turno(s)");
    }

    // Llena las listas desplegables con los clientes, especialistas y servicios registrados.
    private void cargarListas() {
        Respuesta<List<Cliente>> clientes = controladores.clientes().listar();
        Respuesta<List<Especialista>> especialistas = controladores.especialistas().listar();
        Respuesta<List<Servicio>> servicios = controladores.catalogo().listar();
        cbCliente.removeAllItems();
        cbEspecialista.removeAllItems();
        cbServicio.removeAllItems();
        if (clientes.isExito()) {
            clientes.getDato().forEach(c -> cbCliente.addItem(c.getId() + " - " + c.getNombre()));
        }
        if (especialistas.isExito()) {
            especialistas.getDato().forEach(e -> cbEspecialista.addItem(
                    e.getId() + " - " + e.getNombre() + " (" + e.getEspecialidad() + ")"));
        }
        if (servicios.isExito()) {
            servicios.getDato().forEach(s -> cbServicio.addItem(s.getIdServicio() + " - " + s.getNombreServicio()
                    + " (" + s.getDuracionMinutos() + " min, " + Formato.pesos(s.getPrecio()) + ")"));
        }
    }

    private void agendar() {
        Respuesta<Turno> respuesta = controladores.turnos().agendar(
                txtId.getText(),
                Componentes.idDe(cbCliente.getSelectedItem()),
                Componentes.idDe(cbEspecialista.getSelectedItem()),
                Componentes.idDe(cbServicio.getSelectedItem()),
                txtFecha.getText(),
                txtHora.getText());
        if (Componentes.informar(this, respuesta)) {
            txtId.setText("");
            // Muestra la agenda del dia en que quedo el turno.
            txtFiltroFecha.setText(txtFecha.getText().trim());
            verDia();
        }
    }

    // Devuelve el id del turno seleccionado o avisa si no hay ninguno.
    private String turnoSeleccionado() {
        String id = Componentes.valorSeleccionado(tabla, 0);
        if (id == null) {
            Componentes.error(this, "Selecciona un turno en la tabla.");
        }
        return id;
    }

    private void confirmar() {
        String id = turnoSeleccionado();
        if (id != null && Componentes.informar(this, controladores.turnos().confirmar(id))) {
            cargarTabla();
        }
    }

    private void cancelar() {
        String id = turnoSeleccionado();
        if (id == null) {
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "Cancelar el turno " + id + "?",
                "BkenS ERP", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION && Componentes.informar(this, controladores.turnos().cancelar(id))) {
            cargarTabla();
        }
    }

    private void cobrar() {
        String id = turnoSeleccionado();
        if (id == null) {
            return;
        }
        String factura = JOptionPane.showInputDialog(this, "Numero de factura (ej: F001):",
                "Cobrar turno " + id, JOptionPane.QUESTION_MESSAGE);
        if (factura == null) {
            return;
        }
        MetodoPago metodo = (MetodoPago) JOptionPane.showInputDialog(this, "Metodo de pago:",
                "Cobrar turno " + id, JOptionPane.QUESTION_MESSAGE, null, MetodoPago.values(), MetodoPago.EFECTIVO);
        if (metodo == null) {
            return;
        }
        Respuesta<Transaccion> respuesta = controladores.turnos().cobrar(id, factura, metodo.name());
        if (respuesta.isExito()) {
            JOptionPane.showMessageDialog(this, "Turno cobrado. Total: "
                    + Formato.pesos(respuesta.getDato().getMontoTotal()), "BkenS ERP", JOptionPane.INFORMATION_MESSAGE);
            cargarTabla();
        } else {
            Componentes.error(this, respuesta.getMensaje());
        }
    }
}
