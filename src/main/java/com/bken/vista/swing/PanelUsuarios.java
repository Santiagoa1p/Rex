package com.bken.vista.swing;

import com.bken.controlador.Respuesta;
import com.bken.model.Rol;
import com.bken.model.Usuario;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

// Cuentas de acceso al sistema (solo para el administrador).
class PanelUsuarios extends JPanel implements Refrescable {
    private static final long serialVersionUID = 1L;

    private final transient Controladores controladores;
    private final transient Usuario usuarioActual;

    private final DefaultTableModel modelo = Componentes.modeloTabla("Id", "Usuario", "Rol", "Estado");
    private final JTable tabla = Componentes.tabla(modelo);

    private final JTextField txtId = new JTextField(10);
    private final JTextField txtUsuario = new JTextField(14);
    private final JPasswordField txtContrasena = new JPasswordField(14);
    private final JComboBox<Rol> cbRol = new JComboBox<>(Rol.values());

    PanelUsuarios(Controladores controladores, Usuario usuarioActual) {
        super(new BorderLayout(8, 8));
        this.controladores = controladores;
        this.usuarioActual = usuarioActual;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel lateral = new JPanel(new BorderLayout());
        lateral.add(Componentes.formulario("Nuevo usuario",
                new String[] {"Id", "Usuario", "Contrasena", "Rol"},
                new JComponent[] {txtId, txtUsuario, txtContrasena, cbRol}), BorderLayout.NORTH);
        lateral.add(Componentes.filaDeBotones(
                Componentes.boton("Registrar", this::registrar),
                Componentes.boton("Desactivar seleccionado", this::desactivar)), BorderLayout.CENTER);
        add(lateral, BorderLayout.EAST);
    }

    @Override
    public void refrescar() {
        Respuesta<List<Usuario>> respuesta = controladores.sesion().listarUsuarios();
        modelo.setRowCount(0);
        if (!Componentes.exitoOAvisar(this, respuesta)) {
            return;
        }
        for (Usuario usuario : respuesta.getDato()) {
            modelo.addRow(new Object[] {
                    usuario.getId(),
                    usuario.getNombreUsuario(),
                    usuario.getRol(),
                    usuario.isActivo() ? "Activo" : "Inactivo"});
        }
    }

    private void registrar() {
        Rol rol = (Rol) cbRol.getSelectedItem();
        if (Componentes.informar(this, controladores.sesion().registrarUsuario(
                txtId.getText(), txtUsuario.getText(), new String(txtContrasena.getPassword()),
                rol == null ? null : rol.name()))) {
            txtId.setText("");
            txtUsuario.setText("");
            txtContrasena.setText("");
            refrescar();
        }
    }

    private void desactivar() {
        String id = Componentes.valorSeleccionado(tabla, 0);
        if (id == null) {
            Componentes.error(this, "Selecciona un usuario en la tabla.");
            return;
        }
        if (id.equals(usuarioActual.getId())) {
            Componentes.error(this, "No puedes desactivar tu propia cuenta.");
            return;
        }
        int respuesta = JOptionPane.showConfirmDialog(this, "Desactivar el usuario " + id + "?",
                "BkenS ERP", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION && Componentes.informar(this, controladores.sesion().desactivar(id))) {
            refrescar();
        }
    }
}
