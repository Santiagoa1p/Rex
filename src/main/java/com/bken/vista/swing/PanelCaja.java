package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Transaccion;
import com.bken.vista.Formato;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

// Pagos registrados y total de caja, por dia o en general.
class PanelCaja extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;

    private final DefaultTableModel modelo = Componentes.modeloTabla(
            "Factura", "Fecha de pago", "Turno", "Cliente", "Servicio", "Metodo", "Monto");
    private final JTextField txtFecha = new JTextField(Formato.fecha(LocalDate.now()), 10);
    private final JLabel lblTotal = new JLabel();

    private transient Supplier<Respuesta<List<Transaccion>>> consultaPagos;
    private transient Supplier<Respuesta<BigDecimal>> consultaTotal;

    PanelCaja(Controladores controladores) {
        super(new BorderLayout(8, 8));
        this.controladores = controladores;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        filtros.add(new JLabel("Fecha (dd/mm/aaaa):"));
        filtros.add(txtFecha);
        filtros.add(Componentes.boton("Ver dia", this::verDia));
        filtros.add(Componentes.boton("Ver todos", this::verTodos));
        add(filtros, BorderLayout.NORTH);

        add(new JScrollPane(Componentes.tabla(modelo)), BorderLayout.CENTER);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pie.add(lblTotal);
        add(pie, BorderLayout.SOUTH);

        verDiaSinRecargar();
    }

    @Override
    public void refrescar() {
        Respuesta<List<Transaccion>> pagos = consultaPagos.get();
        modelo.setRowCount(0);
        lblTotal.setText("");
        if (!Componentes.exitoOAvisar(this, pagos)) {
            return;
        }
        for (Transaccion pago : pagos.getDato()) {
            modelo.addRow(new Object[] {
                    pago.getIdFactura(),
                    Formato.fecha(pago.getFechaPago()),
                    pago.getTurno().getIdTurno(),
                    pago.getTurno().getCliente().getNombre(),
                    pago.getTurno().getServicio().getNombreServicio(),
                    pago.getMetodoPago().getEtiqueta(),
                    Formato.pesos(pago.getMontoTotal())});
        }
        Respuesta<BigDecimal> total = consultaTotal.get();
        if (total.isExito()) {
            lblTotal.setText("Total: " + Formato.pesos(total.getDato()) + "  (" + pagos.getDato().size() + " pagos)");
        }
    }

    private void verDiaSinRecargar() {
        String fecha = txtFecha.getText();
        consultaPagos = () -> controladores.caja().pagosDelDia(fecha);
        consultaTotal = () -> controladores.caja().totalDelDia(fecha);
    }

    private void verDia() {
        verDiaSinRecargar();
        refrescar();
    }

    private void verTodos() {
        consultaPagos = controladores.caja()::todosLosPagos;
        consultaTotal = controladores.caja()::totalGeneral;
        refrescar();
    }
}
