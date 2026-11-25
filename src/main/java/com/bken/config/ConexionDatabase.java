package com.bken.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Entrega conexiones JDBC a la base de datos configurada en config.properties.
// Cada llamada abre una conexion nueva: el DAO la usa en un try-with-resources y la cierra al terminar.
public final class ConexionDatabase {

    // Permite apuntar a otro archivo, por ejemplo: -Dbken.config=config-test.properties
    private static final String RUTA_CONFIG = System.getProperty("bken.config", "config.properties");

    private static Properties propiedades;

    private ConexionDatabase() {
    }

    public static Connection getConnection() throws SQLException {
        Properties config = cargarPropiedades();
        return DriverManager.getConnection(
                config.getProperty("URL"),
                config.getProperty("USERNAME"),
                config.getProperty("PASSWORD"));
    }

    // Lee el archivo de configuracion una sola vez.
    private static synchronized Properties cargarPropiedades() {
        if (propiedades == null) {
            Properties cargadas = new Properties();
            try (InputStream entrada = new FileInputStream(RUTA_CONFIG)) {
                cargadas.load(entrada);
            } catch (IOException e) {
                throw new IllegalStateException(
                        "No se pudo leer " + RUTA_CONFIG + ". Copia config.properties.example y llena tus datos.", e);
            }
            for (String clave : new String[] {"URL", "USERNAME", "PASSWORD"}) {
                if (cargadas.getProperty(clave) == null) {
                    throw new IllegalStateException("Falta la clave " + clave + " en " + RUTA_CONFIG);
                }
            }
            propiedades = cargadas;
        }
        return propiedades;
    }
}
