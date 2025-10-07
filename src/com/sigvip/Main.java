package com.sigvip;

import com.sigvip.persistencia.ConexionBD;
import com.sigvip.vista.VistaLogin;

import javax.swing.*;

/**
 * Punto de entrada de la aplicación SIGVIP.
 * Sistema Integral de Gestión de Visitas Penitenciarias.
 *
 * Responsabilidades:
 * - Inicializar la conexión a base de datos
 * - Configurar Look and Feel de la aplicación
 * - Mostrar ventana de login
 * - Cerrar recursos al terminar
 *
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class Main {

    /**
     * Método principal de la aplicación.
     * Inicializa el sistema y muestra la ventana de login.
     *
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        // Imprimir información de inicio
        System.out.println("═══════════════════════════════════════════════════");
        System.out.println("  SIGVIP - Sistema de Gestión de Visitas");
        System.out.println("  Universidad Siglo 21 - INF275");
        System.out.println("  Versión 1.0");
        System.out.println("═══════════════════════════════════════════════════\n");

        // Verificar conexión a base de datos
        System.out.println("Verificando conexión a base de datos...");
        ConexionBD conexionBD = ConexionBD.getInstancia();

        if (!conexionBD.probarConexion()) {
            mostrarErrorConexion();
            return;
        }

        System.out.println();

        // Configurar Look and Feel
        configurarLookAndFeel();

        // Registrar hook de cierre para cerrar conexión limpiamente
        registrarHookCierre();

        // Iniciar interfaz gráfica en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                VistaLogin vistaLogin = new VistaLogin();
                vistaLogin.setVisible(true);
            } catch (Exception e) {
                System.err.println("Error al iniciar la aplicación: " + e.getMessage());
                e.printStackTrace();
                mostrarErrorInicio(e);
            }
        });
    }

    /**
     * Configura el Look and Feel de la aplicación.
     * Intenta usar el L&F del sistema operativo, o Nimbus como alternativa.
     */
    private static void configurarLookAndFeel() {
        try {
            // Intentar usar el Look and Feel del sistema
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("✓ Look and Feel del sistema aplicado");
        } catch (Exception e) {
            try {
                // Si falla, intentar usar Nimbus (más moderno que Metal)
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        System.out.println("✓ Look and Feel Nimbus aplicado");
                        return;
                    }
                }
            } catch (Exception ex) {
                // Si todo falla, usar el default (Metal)
                System.out.println("⚠ Usando Look and Feel por defecto");
            }
        }
    }

    /**
     * Registra un hook para cerrar la conexión a base de datos al salir.
     * Garantiza que los recursos se liberen correctamente.
     */
    private static void registrarHookCierre() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n═══════════════════════════════════════════════════");
            System.out.println("  Cerrando SIGVIP...");
            ConexionBD.getInstancia().cerrarConexion();
            System.out.println("  Aplicación finalizada correctamente");
            System.out.println("═══════════════════════════════════════════════════");
        }));
    }

    /**
     * Muestra un diálogo de error cuando falla la conexión a base de datos.
     * Detiene la ejecución de la aplicación.
     */
    private static void mostrarErrorConexion() {
        String mensaje = "No se pudo conectar a la base de datos.\n\n" +
                        "Verifique:\n" +
                        "1. MySQL está instalado y ejecutándose\n" +
                        "2. La base de datos 'sigvip_db' existe\n" +
                        "3. Las credenciales en resources/config.properties son correctas\n" +
                        "4. MySQL Connector/J está en lib/ y agregado al classpath\n\n" +
                        "Consulte README.md para instrucciones de configuración.";

        JOptionPane.showMessageDialog(
            null,
            mensaje,
            "Error de Conexión - SIGVIP",
            JOptionPane.ERROR_MESSAGE
        );

        System.err.println("\n✗ Aplicación terminada debido a error de conexión");
        System.exit(1);
    }

    /**
     * Muestra un diálogo de error cuando falla la inicialización de la aplicación.
     *
     * @param e excepción que causó el error
     */
    private static void mostrarErrorInicio(Exception e) {
        String mensaje = "Error al iniciar la aplicación:\n\n" +
                        e.getMessage() + "\n\n" +
                        "Consulte la consola para más detalles.";

        JOptionPane.showMessageDialog(
            null,
            mensaje,
            "Error de Inicio - SIGVIP",
            JOptionPane.ERROR_MESSAGE
        );

        System.exit(1);
    }
}
