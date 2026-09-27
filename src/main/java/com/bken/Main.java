package com.bken;

import com.bken.model.Cliente;
import com.bken.model.Especialista;
import com.bken.model.Servicio;
import com.bken.model.Transaccion;
import com.bken.model.Turno;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Cliente cliente = new Cliente(
                "C001", "Ana Torres", "3001234567", "ana@correo.com", LocalDate.now());
        Especialista especialista = new Especialista(
                "E001", "Laura Gomez", "3007654321", "laura@correo.com",
                "Estetica facial", "Lunes a viernes, 9:00 a 17:00");
        Servicio servicio = new Servicio(
                "S001", "Limpieza facial", new BigDecimal("85000"), 60);
        Turno turno = new Turno(
                "T001", cliente, especialista, servicio,
                LocalDate.now(), LocalTime.of(10, 0), "Confirmado");
        Transaccion transaccion = new Transaccion(
                "F001", turno, servicio.getPrecio(), "Efectivo", LocalDate.now());

        List<Turno> agenda = List.of(turno);
        List<Servicio> servicios = List.of(servicio);
        List<Transaccion> transacciones = List.of(transaccion);

        try (Scanner scanner = new Scanner(System.in)) {
            int opcion;
            do {
                mostrarMenu();
                opcion = leerOpcion(scanner);

                switch (opcion) {
                    case 1:
                        mostrarAgenda(agenda);
                        break;
                    case 2:
                        mostrarServicios(servicios);
                        break;
                    case 3:
                        mostrarCaja(transacciones);
                        break;
                    case 0:
                        System.out.println("Hasta pronto.");
                        break;
                    default:
                        System.out.println("Opcion no valida.");
                }
            } while (opcion != 0);
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n=== BkenS ERP ===");
        System.out.println("1. Consultar agenda");
        System.out.println("2. Consultar servicios");
        System.out.println("3. Consultar caja");
        System.out.println("0. Salir");
        System.out.print("Selecciona una opcion: ");
    }

    private static int leerOpcion(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException excepcion) {
            return -1;
        }
    }

    private static void mostrarAgenda(List<Turno> agenda) {
        System.out.println("\n--- Agenda ---");
        for (Turno turno : agenda) {
            System.out.println("Turno: " + turno.getIdTurno());
            System.out.println("Fecha y hora: " + turno.getFecha() + " " + turno.getHora());
            System.out.println("Cliente: " + turno.getCliente().getNombre());
            System.out.println("Especialista: " + turno.getEspecialista().getNombre());
            System.out.println("Servicio: " + turno.getServicio().getNombreServicio());
            System.out.println("Estado: " + turno.getEstado());
        }
    }

    private static void mostrarServicios(List<Servicio> servicios) {
        System.out.println("\n--- Servicios ---");
        for (Servicio servicio : servicios) {
            System.out.println(servicio.getNombreServicio()
                    + " | Precio: " + servicio.getPrecio().toPlainString()
                    + " | Duracion: " + servicio.getDuracionMinutos() + " minutos");
        }
    }

    private static void mostrarCaja(List<Transaccion> transacciones) {
        System.out.println("\n--- Caja ---");
        BigDecimal total = BigDecimal.ZERO;
        for (Transaccion transaccion : transacciones) {
            System.out.println("Factura: " + transaccion.getIdFactura()
                    + " | Fecha: " + transaccion.getFechaPago()
                    + " | Metodo: " + transaccion.getMetodoPago()
                    + " | Total: " + transaccion.getMontoTotal().toPlainString());
            total = total.add(transaccion.getMontoTotal());
        }
        System.out.println("Total registrado: " + total.toPlainString());
    }
}