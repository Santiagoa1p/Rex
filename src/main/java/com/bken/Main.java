package com.bken;

import com.bken.vista.VistaConsola;

import java.util.Scanner;

// Punto de entrada: abre la interfaz de consola conectada a la base de datos de config.properties.
public class Main {
    public static void main(String[] args) {
        try (Scanner entrada = new Scanner(System.in)) {
            new VistaConsola(entrada).iniciar();
        }
    }
}
