package com.sigvip;

import com.sigvip.persistencia.ConexionBD;
import com.sigvip.persistencia.GestorModo;
import com.sigvip.persistencia.RepositorioMemoria;
import com.sigvip.utilidades.TemaColors;
import com.sigvip.vista.DialogoConexionBD;
import com.sigvip.vista.DialogoConfigManualBD;
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
        // Configurar Look and Feel nativo para mejor apariencia
        try {
            String systemLF = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(systemLF);
        } catch (Exception e) {
            System.err.println("✗ Error configurando Look and Feel: " + e.getMessage());
            // Continuar con el Look and Feel por defecto
        }

        // Configurar propiedades UI para consistencia
        System.setProperty("swing.boldMetal", "false");

        // Verificar conexión a base de datos
        ConexionBD conexionBD = ConexionBD.getInstancia();

        if (!conexionBD.probarConexion()) {
            // Mostrar diálogo con opciones si falla la conexión
            manejarFallaConexion();
        }

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
        } catch (Exception e) {
            try {
                // Si falla, intentar usar Nimbus (más moderno que Metal)
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        return;
                    }
                }
            } catch (Exception ex) {
                // Si todo falla, usar el default (Metal)
            }
        }
    }

    /**
     * Registra un hook para cerrar la conexión a base de datos al salir.
     * Garantiza que los recursos se liberen correctamente.
     */
    private static void registrarHookCierre() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ConexionBD.getInstancia().cerrarConexion();
        }));
    }

    /**
     * Maneja la falla de conexión a base de datos.
     * Muestra diálogo con opciones: Reintentar, Configurar o Modo Offline.
     */
    private static void manejarFallaConexion() {
        boolean conectado = false;
        int intentos = 0;
        final int MAX_INTENTOS = 3;

        while (!conectado && intentos < MAX_INTENTOS) {
            int opcion = DialogoConexionBD.mostrar();

            switch (opcion) {
                case DialogoConexionBD.REINTENTAR:
                    ConexionBD conexionBD = ConexionBD.getInstancia();
                    conectado = conexionBD.probarConexion();

                    if (!conectado) {
                        intentos++;
                    } else {
                        GestorModo.getInstancia().activarModoOnline();
                    }
                    break;

                case DialogoConexionBD.CONFIGURAR:
                    boolean configuradoOK = DialogoConfigManualBD.mostrar();

                    if (configuradoOK) {
                        // Reiniciar conexión con nueva configuración
                        ConexionBD nuevaConexion = ConexionBD.getInstancia();
                        conectado = nuevaConexion.probarConexion();

                        if (conectado) {
                            GestorModo.getInstancia().activarModoOnline();
                        } else {
                            intentos++;
                        }
                    }
                    break;

                case DialogoConexionBD.MODO_OFFLINE:
                    GestorModo.getInstancia().activarModoOffline();
                    RepositorioMemoria.getInstancia().inicializarDatosPrueba();
                    return; // Salir del bucle

                case DialogoConexionBD.CANCELAR:
                default:
                    System.exit(0);
                    return;
            }
        }

        if (!conectado && !GestorModo.getInstancia().isModoOffline()) {
            System.err.println("\n✗ Número máximo de intentos alcanzado");
            System.exit(1);
        }
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
