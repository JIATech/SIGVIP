package com.sigvip.persistencia;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor de conexión a base de datos MySQL usando patrón Singleton.
 * Lee la configuración desde resources/config.properties.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Patrón: Singleton (único permitido según restricciones académicas)
 * Seguridad: Connection pooling manual, PreparedStatements obligatorios en DAOs
 */
public class ConexionBD {

    private static ConexionBD instancia;
    private Connection conexion;

    // Configuración cargada desde archivo
    private String url;
    private String usuario;
    private String contrasena;
    private String driver;

    /**
     * Constructor privado para patrón Singleton.
     * Carga la configuración desde resources/config.properties.
     *
     * @throws RuntimeException si no puede cargar la configuración
     */
    private ConexionBD() {
        cargarConfiguracion();
        cargarDriver();
    }

    /**
     * Obtiene la instancia única del gestor de conexión.
     *
     * @return instancia singleton
     */
    public static synchronized ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /**
     * Carga la configuración desde resources/config.properties.
     *
     * @throws RuntimeException si el archivo no existe o está mal formado
     */
    private void cargarConfiguracion() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("resources/config.properties")) {
            props.load(fis);

            this.url = props.getProperty("db.url");
            this.usuario = props.getProperty("db.usuario");
            this.contrasena = props.getProperty("db.contrasena");
            this.driver = props.getProperty("db.driver");

            // Validar que todas las propiedades estén presentes
            if (url == null || usuario == null || contrasena == null || driver == null) {
                throw new RuntimeException(
                    "Archivo config.properties incompleto. " +
                    "Debe contener: db.url, db.usuario, db.contrasena, db.driver");
            }

        } catch (IOException e) {
            throw new RuntimeException(
                "Error al cargar config.properties: " + e.getMessage() +
                "\nVerifique que el archivo exista en resources/config.properties", e);
        }
    }

    /**
     * Carga el driver JDBC de MySQL.
     *
     * @throws RuntimeException si el driver no está disponible
     */
    private void cargarDriver() {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "Driver MySQL no encontrado: " + driver +
                "\nVerifique que mysql-connector-j esté en lib/ y agregado al classpath", e);
        }
    }

    /**
     * Obtiene una conexión activa a la base de datos.
     * Si la conexión actual está cerrada o es null, crea una nueva.
     *
     * @return conexión JDBC activa
     * @throws SQLException si no puede establecer la conexión
     */
    public Connection getConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = DriverManager.getConnection(url, usuario, contrasena);
        }
        return conexion;
    }

    /**
     * Cierra la conexión actual si está abierta.
     * Útil para cerrar la aplicación limpiamente.
     */
    public void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Prueba la conexión a la base de datos.
     * Útil para verificar la configuración en el arranque.
     *
     * @return true si la conexión es exitosa
     */
    public boolean probarConexion() {
        try {
            Connection conn = getConexion();
            boolean valida = conn != null && !conn.isClosed();
            return valida;

        } catch (SQLException e) {
            System.err.println("✗ Error al conectar a la base de datos:");
            System.err.println("  URL: " + url);
            System.err.println("  Usuario: " + usuario);
            System.err.println("  Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inicia una transacción desactivando auto-commit.
     *
     * @throws SQLException si no puede iniciar la transacción
     */
    public void iniciarTransaccion() throws SQLException {
        Connection conn = getConexion();
        conn.setAutoCommit(false);
    }

    /**
     * Confirma los cambios de la transacción actual.
     *
     * @throws SQLException si no puede hacer commit
     */
    public void commit() throws SQLException {
        Connection conn = getConexion();
        conn.commit();
        conn.setAutoCommit(true);
    }

    /**
     * Revierte los cambios de la transacción actual.
     *
     * @throws SQLException si no puede hacer rollback
     */
    public void rollback() throws SQLException {
        Connection conn = getConexion();
        conn.rollback();
        conn.setAutoCommit(true);
    }
}
