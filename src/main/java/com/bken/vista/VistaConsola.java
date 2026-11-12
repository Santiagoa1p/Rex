package com.bken.vista;

import com.bken.controlador.CajaControlador;
import com.bken.controlador.CatalogoControlador;
import com.bken.controlador.ClienteControlador;
import com.bken.controlador.EspecialistaControlador;
import com.bken.controlador.Respuesta;
import com.bken.controlador.SesionControlador;
import com.bken.controlador.TurnoControlador;
import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.HorarioDisponibilidad;
import com.bken.model.Rol;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import com.bken.model.Turno;
import com.bken.model.Usuario;

import java.io.Console;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

// Interfaz de consola. Solo lee lo que escribe el usuario, llama al controlador y muestra la respuesta:
// no tiene reglas de negocio ni acceso a la base de datos.
public class VistaConsola {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Formato de miles con punto (85.000) y sin tildes, para que se vea bien en cualquier terminal de Windows.
    private static final DecimalFormat PESOS =
            new DecimalFormat("$#,##0.##", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
    private static final String[] NOMBRES_DIAS =
            {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};

    private final Scanner entrada;
    private final SesionControlador sesion;
    private final ClienteControlador clientes;
    private final EspecialistaControlador especialistas;
    private final CatalogoControlador catalogo;
    private final TurnoControlador turnos;
    private final CajaControlador caja;

    private Usuario usuarioActual;

    // Una opcion de menu: el texto que se muestra y lo que hace al elegirla.
    private record Opcion(String texto, Runnable accion) {
    }

    // Se lanza cuando se cierra la entrada (por ejemplo, Ctrl+Z en Windows) para salir del programa.
    private static class EntradaCerrada extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }

    public VistaConsola(Scanner entrada) {
        this(entrada, new SesionControlador(), new ClienteControlador(), new EspecialistaControlador(),
                new CatalogoControlador(), new TurnoControlador(), new CajaControlador());
    }

    public VistaConsola(Scanner entrada, SesionControlador sesion, ClienteControlador clientes,
                        EspecialistaControlador especialistas, CatalogoControlador catalogo,
                        TurnoControlador turnos, CajaControlador caja) {
        this.entrada = entrada;
        this.sesion = sesion;
        this.clientes = clientes;
        this.especialistas = especialistas;
        this.catalogo = catalogo;
        this.turnos = turnos;
        this.caja = caja;
    }

    public void iniciar() {
        titulo("BkenS ERP");
        try {
            Respuesta<Boolean> hayUsuarios = sesion.hayUsuarios();
            if (!hayUsuarios.isExito()) {
                System.out.println("No se pudo conectar con la base de datos: " + hayUsuarios.getMensaje());
                return;
            }
            if (!hayUsuarios.getDato()) {
                crearAdministradorInicial();
            }
            while (iniciarSesion()) {
                menuPrincipal();
                usuarioActual = null;
            }
        } catch (EntradaCerrada e) {
            System.out.println();
        }
        System.out.println("Hasta pronto.");
    }

    // ---------- Sesion ----------

    private void crearAdministradorInicial() {
        System.out.println("Es la primera vez que se usa el sistema. Crea la cuenta del administrador.");
        Respuesta<Usuario> respuesta;
        do {
            String nombre = leer("Nombre de usuario");
            String contrasena = leerContrasena();
            respuesta = sesion.crearAdministradorInicial(nombre, contrasena);
            mostrar(respuesta);
        } while (!respuesta.isExito());
    }

    // Devuelve false si el usuario decide salir del programa.
    private boolean iniciarSesion() {
        System.out.println("\nInicia sesion (deja el usuario vacio para salir).");
        while (true) {
            String nombre = leer("Usuario");
            if (nombre.isBlank()) {
                return false;
            }
            Respuesta<Usuario> respuesta = sesion.iniciarSesion(nombre, leerContrasena());
            if (respuesta.isExito()) {
                usuarioActual = respuesta.getDato();
                System.out.println("Bienvenido, " + usuarioActual.getNombreUsuario()
                        + " (" + usuarioActual.getRol() + ").");
                return true;
            }
            System.out.println(respuesta.getMensaje());
        }
    }

    // Cada rol ve solo las opciones que le corresponden.
    private void menuPrincipal() {
        List<Opcion> opciones = new ArrayList<>();
        Rol rol = usuarioActual.getRol();
        if (rol == Rol.ESPECIALISTA) {
            opciones.add(new Opcion("Agenda de hoy", () -> mostrarTurnos(turnos.agendaDeHoy())));
            opciones.add(new Opcion("Agenda de otro dia",
                    () -> mostrarTurnos(turnos.agendaDelDia(leer("Fecha (dd/mm/aaaa)")))));
        } else {
            opciones.add(new Opcion("Clientes", this::menuClientes));
            if (rol == Rol.ADMINISTRADOR) {
                opciones.add(new Opcion("Especialistas", this::menuEspecialistas));
                opciones.add(new Opcion("Servicios", this::menuServicios));
            }
            opciones.add(new Opcion("Turnos", this::menuTurnos));
            opciones.add(new Opcion("Caja", this::menuCaja));
            if (rol == Rol.ADMINISTRADOR) {
                opciones.add(new Opcion("Usuarios", this::menuUsuarios));
            }
        }
        menu("Menu principal", opciones, "Cerrar sesion");
    }

    // ---------- Clientes ----------

    private void menuClientes() {
        menu("Clientes", List.of(
                new Opcion("Registrar cliente", this::registrarCliente),
                new Opcion("Ver clientes", this::listarClientes),
                new Opcion("Actualizar cliente", this::actualizarCliente),
                new Opcion("Eliminar cliente",
                        () -> mostrar(clientes.eliminar(leer("Id del cliente a eliminar"))))), "Volver");
    }

    private void registrarCliente() {
        mostrar(clientes.registrar(
                leer("Id (ej: C001)"),
                leer("Nombre"),
                leer("Telefono (opcional)"),
                leer("Correo (opcional)")));
    }

    private void listarClientes() {
        Respuesta<List<Cliente>> respuesta = clientes.listar();
        if (!respuesta.isExito()) {
            System.out.println(respuesta.getMensaje());
            return;
        }
        subtitulo("Clientes");
        if (respuesta.getDato().isEmpty()) {
            System.out.println("No hay clientes registrados.");
        }
        for (Cliente cliente : respuesta.getDato()) {
            System.out.println(cliente.getId() + " | " + cliente.getNombre()
                    + " | Tel: " + valorOGuion(cliente.getTelefono())
                    + " | Correo: " + valorOGuion(cliente.getCorreo())
                    + " | Registro: " + cliente.getFechaRegistro().format(FECHA));
        }
    }

    // Muestra los datos actuales; si el usuario deja un campo vacio, se conserva el valor anterior.
    private void actualizarCliente() {
        Respuesta<Cliente> buscado = clientes.buscar(leer("Id del cliente"));
        if (!buscado.isExito()) {
            System.out.println(buscado.getMensaje());
            return;
        }
        Cliente actual = buscado.getDato();
        System.out.println("Deja vacio un campo para conservar su valor actual.");
        mostrar(clientes.actualizar(actual.getId(),
                leerConValorActual("Nombre", actual.getNombre()),
                leerConValorActual("Telefono", actual.getTelefono()),
                leerConValorActual("Correo", actual.getCorreo())));
    }

    // ---------- Especialistas ----------

    private void menuEspecialistas() {
        menu("Especialistas", List.of(
                new Opcion("Registrar especialista", this::registrarEspecialista),
                new Opcion("Ver especialistas y horarios", this::listarEspecialistas),
                new Opcion("Cambiar horarios", () -> {
                    String id = leer("Id del especialista");
                    mostrar(especialistas.cambiarHorarios(id, leerHorarios()));
                }),
                new Opcion("Eliminar especialista",
                        () -> mostrar(especialistas.eliminar(leer("Id del especialista a eliminar"))))), "Volver");
    }

    private void registrarEspecialista() {
        String id = leer("Id (ej: E001)");
        String nombre = leer("Nombre");
        String telefono = leer("Telefono (opcional)");
        String correo = leer("Correo (opcional)");
        String especialidad = leer("Especialidad");
        mostrar(especialistas.registrar(id, nombre, telefono, correo, especialidad, leerHorarios()));
    }

    // Pide horarios uno por uno hasta que el usuario deje el dia vacio.
    private List<HorarioDisponibilidad> leerHorarios() {
        List<HorarioDisponibilidad> horarios = new ArrayList<>();
        System.out.println("Horarios de atencion (deja el dia vacio para terminar).");
        while (true) {
            String dia = leer("Dia (ej: lunes)");
            if (dia.isBlank()) {
                return horarios;
            }
            Respuesta<HorarioDisponibilidad> horario =
                    especialistas.crearHorario(dia, leer("Desde (HH:mm)"), leer("Hasta (HH:mm)"));
            mostrar(horario);
            if (horario.isExito()) {
                horarios.add(horario.getDato());
            }
        }
    }

    private void listarEspecialistas() {
        Respuesta<List<Especialista>> respuesta = especialistas.listar();
        if (!respuesta.isExito()) {
            System.out.println(respuesta.getMensaje());
            return;
        }
        subtitulo("Especialistas");
        if (respuesta.getDato().isEmpty()) {
            System.out.println("No hay especialistas registrados.");
        }
        for (Especialista especialista : respuesta.getDato()) {
            System.out.println(especialista.getId() + " | " + especialista.getNombre()
                    + " | " + especialista.getEspecialidad()
                    + " | Tel: " + valorOGuion(especialista.getTelefono()));
            for (HorarioDisponibilidad horario : especialista.getHorarios()) {
                System.out.println("    " + NOMBRES_DIAS[horario.getDia().getValue() - 1]
                        + " de " + horario.getHoraInicio() + " a " + horario.getHoraFin());
            }
        }
    }

    // ---------- Servicios ----------

    private void menuServicios() {
        menu("Servicios", List.of(
                new Opcion("Registrar servicio", () -> mostrar(catalogo.registrar(
                        leer("Id (ej: S001)"),
                        leer("Nombre"),
                        leer("Precio (sin puntos, ej: 85000)"),
                        leer("Duracion en minutos")))),
                new Opcion("Ver servicios", this::listarServicios),
                new Opcion("Actualizar servicio", () -> mostrar(catalogo.actualizar(
                        leer("Id del servicio"),
                        leer("Nuevo nombre"),
                        leer("Nuevo precio"),
                        leer("Nueva duracion en minutos")))),
                new Opcion("Eliminar servicio",
                        () -> mostrar(catalogo.eliminar(leer("Id del servicio a eliminar"))))), "Volver");
    }

    private void listarServicios() {
        Respuesta<List<Servicio>> respuesta = catalogo.listar();
        if (!respuesta.isExito()) {
            System.out.println(respuesta.getMensaje());
            return;
        }
        subtitulo("Servicios");
        if (respuesta.getDato().isEmpty()) {
            System.out.println("No hay servicios registrados.");
        }
        for (Servicio servicio : respuesta.getDato()) {
            System.out.println(servicio.getIdServicio() + " | " + servicio.getNombreServicio()
                    + " | " + pesos(servicio.getPrecio())
                    + " | " + servicio.getDuracionMinutos() + " min");
        }
    }

    // ---------- Turnos ----------

    private void menuTurnos() {
        menu("Turnos", List.of(
                new Opcion("Agendar turno", () -> mostrar(turnos.agendar(
                        leer("Id del turno (ej: T001)"),
                        leer("Id del cliente"),
                        leer("Id del especialista"),
                        leer("Id del servicio"),
                        leer("Fecha (dd/mm/aaaa)"),
                        leer("Hora (HH:mm)")))),
                new Opcion("Agenda de hoy", () -> mostrarTurnos(turnos.agendaDeHoy())),
                new Opcion("Agenda de otro dia",
                        () -> mostrarTurnos(turnos.agendaDelDia(leer("Fecha (dd/mm/aaaa)")))),
                new Opcion("Confirmar turno", () -> mostrar(turnos.confirmar(leer("Id del turno")))),
                new Opcion("Cancelar turno", () -> mostrar(turnos.cancelar(leer("Id del turno")))),
                new Opcion("Cobrar turno", this::cobrarTurno),
                new Opcion("Ver todos los turnos", () -> mostrarTurnos(turnos.listar())),
                new Opcion("Ver clientes", this::listarClientes),
                new Opcion("Ver especialistas y horarios", this::listarEspecialistas),
                new Opcion("Ver servicios", this::listarServicios)), "Volver");
    }

    private void cobrarTurno() {
        Respuesta<Transaccion> respuesta = turnos.cobrar(
                leer("Id del turno"),
                leer("Numero de factura (ej: F001)"),
                leer("Metodo de pago (EFECTIVO, TARJETA o TRANSFERENCIA)"));
        mostrar(respuesta);
        if (respuesta.isExito()) {
            System.out.println("Total cobrado: " + pesos(respuesta.getDato().getMontoTotal()));
        }
    }

    private void mostrarTurnos(Respuesta<List<Turno>> respuesta) {
        if (!respuesta.isExito()) {
            System.out.println(respuesta.getMensaje());
            return;
        }
        subtitulo(respuesta.getMensaje());
        if (respuesta.getDato().isEmpty()) {
            System.out.println("No hay turnos.");
        }
        for (Turno turno : respuesta.getDato()) {
            System.out.println(turno.getIdTurno() + " | " + turno.getFecha().format(FECHA) + " " + turno.getHora()
                    + " | " + turno.getServicio().getNombreServicio()
                    + " | Cliente: " + turno.getCliente().getNombre()
                    + " | Especialista: " + turno.getEspecialista().getNombre()
                    + " | " + turno.getEstado().getEtiqueta());
        }
    }

    // ---------- Caja ----------

    private void menuCaja() {
        menu("Caja", List.of(
                new Opcion("Pagos de un dia", () -> {
                    String fecha = leer("Fecha (dd/mm/aaaa)");
                    if (mostrarPagos(caja.pagosDelDia(fecha))) {
                        mostrarTotal(caja.totalDelDia(fecha));
                    }
                }),
                new Opcion("Todos los pagos", () -> {
                    if (mostrarPagos(caja.todosLosPagos())) {
                        mostrarTotal(caja.totalGeneral());
                    }
                })), "Volver");
    }

    private boolean mostrarPagos(Respuesta<List<Transaccion>> respuesta) {
        if (!respuesta.isExito()) {
            System.out.println(respuesta.getMensaje());
            return false;
        }
        subtitulo(respuesta.getMensaje());
        if (respuesta.getDato().isEmpty()) {
            System.out.println("No hay pagos registrados.");
        }
        for (Transaccion pago : respuesta.getDato()) {
            System.out.println(pago.getIdFactura() + " | " + pago.getFechaPago().format(FECHA)
                    + " | Turno " + pago.getTurno().getIdTurno()
                    + " | " + pago.getMetodoPago().getEtiqueta()
                    + " | " + pesos(pago.getMontoTotal()));
        }
        return true;
    }

    private void mostrarTotal(Respuesta<BigDecimal> total) {
        System.out.println(total.isExito() ? "Total: " + pesos(total.getDato()) : total.getMensaje());
    }

    // ---------- Usuarios ----------

    private void menuUsuarios() {
        menu("Usuarios", List.of(
                new Opcion("Registrar usuario", () -> {
                    String id = leer("Id (ej: U002)");
                    String nombre = leer("Nombre de usuario");
                    String contrasena = leerContrasena();
                    String rol = leer("Rol (ADMINISTRADOR, RECEPCIONISTA o ESPECIALISTA)");
                    mostrar(sesion.registrarUsuario(id, nombre, contrasena, rol));
                }),
                new Opcion("Ver usuarios", this::listarUsuarios),
                new Opcion("Desactivar usuario", () -> {
                    String id = leer("Id del usuario a desactivar");
                    if (id.equals(usuarioActual.getId())) {
                        System.out.println("No puedes desactivar tu propia cuenta.");
                        return;
                    }
                    mostrar(sesion.desactivar(id));
                })), "Volver");
    }

    private void listarUsuarios() {
        Respuesta<List<Usuario>> respuesta = sesion.listarUsuarios();
        if (!respuesta.isExito()) {
            System.out.println(respuesta.getMensaje());
            return;
        }
        subtitulo("Usuarios");
        for (Usuario usuario : respuesta.getDato()) {
            System.out.println(usuario.getId() + " | " + usuario.getNombreUsuario() + " | " + usuario.getRol()
                    + " | " + (usuario.isActivo() ? "Activo" : "Inactivo"));
        }
    }

    // ---------- Utilidades de entrada y salida ----------

    // Muestra las opciones numeradas hasta que el usuario elija 0.
    private void menu(String nombre, List<Opcion> opciones, String textoSalir) {
        while (true) {
            titulo(nombre);
            for (int i = 0; i < opciones.size(); i++) {
                System.out.println((i + 1) + ". " + opciones.get(i).texto());
            }
            System.out.println("0. " + textoSalir);
            String texto = leer("Selecciona una opcion");
            int opcion;
            try {
                opcion = Integer.parseInt(texto.trim());
            } catch (NumberFormatException e) {
                opcion = -1;
            }
            if (opcion == 0) {
                return;
            }
            if (opcion < 1 || opcion > opciones.size()) {
                System.out.println("Opcion no valida.");
                continue;
            }
            opciones.get(opcion - 1).accion().run();
        }
    }

    private String leer(String etiqueta) {
        System.out.print(etiqueta + ": ");
        if (!entrada.hasNextLine()) {
            throw new EntradaCerrada();
        }
        return entrada.nextLine();
    }

    private String leerConValorActual(String etiqueta, String actual) {
        String nuevo = leer(etiqueta + " [" + valorOGuion(actual) + "]");
        return nuevo.isBlank() ? actual : nuevo;
    }

    // En una terminal real la contrasena no se ve al escribirla.
    private String leerContrasena() {
        Console consola = System.console();
        if (consola != null) {
            char[] contrasena = consola.readPassword("Contrasena: ");
            if (contrasena == null) {
                throw new EntradaCerrada();
            }
            return new String(contrasena);
        }
        return leer("Contrasena");
    }

    private void mostrar(Respuesta<?> respuesta) {
        System.out.println(respuesta.isExito() ? "OK: " + respuesta.getMensaje() : "Error: " + respuesta.getMensaje());
    }

    private void titulo(String texto) {
        System.out.println("\n=== " + texto + " ===");
    }

    // Los mensajes del controlador terminan en punto; en un titulo se quita.
    private void subtitulo(String texto) {
        String sinPunto = texto.endsWith(".") ? texto.substring(0, texto.length() - 1) : texto;
        System.out.println("\n--- " + sinPunto + " ---");
    }

    private String pesos(BigDecimal valor) {
        return PESOS.format(valor);
    }

    private String valorOGuion(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }
}
