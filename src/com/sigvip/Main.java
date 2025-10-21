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
            System.out.println("✓ Look and Feel configurado: " + UIManager.getLookAndFeel().getName());
        } catch (Exception e) {
            System.err.println("✗ Error configurando Look and Feel: " + e.getMessage());
            // Continuar con el Look and Feel por defecto
        }

        // Configurar propiedades UI para consistencia
        System.setProperty("swing.boldMetal", "false");

        // Imprimir información de inicio
        System.out.println("=================================================");
        System.out.println("  SIGVIP - Sistema de Gestión de Visitas");
        System.out.println("  Universidad Siglo 21 - INF275");
        System.out.println("  Version 1.0");
        System.out.println("=================================================\n");

        // Verificar conexión a base de datos
        System.out.println("Verificando conexión a base de datos...");
        ConexionBD conexionBD = ConexionBD.getInstancia();

        if (!conexionBD.probarConexion()) {
            // Mostrar diálogo con opciones si falla la conexión
            manejarFallaConexion();
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
            System.out.println("\n=================================================");
            System.out.println("  Cerrando SIGVIP...");
            ConexionBD.getInstancia().cerrarConexion();
            System.out.println("  Aplicacion finalizada correctamente");
            System.out.println("=================================================");
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
                    System.out.println("\n🔄 Reintentando conexión a MySQL...");
                    ConexionBD conexionBD = ConexionBD.getInstancia();
                    conectado = conexionBD.probarConexion();

                    if (!conectado) {
                        intentos++;
                        System.out.println("✗ Reintento fallido (" + intentos + "/" + MAX_INTENTOS + ")");
                    } else {
                        System.out.println("✓ Conexión exitosa");
                        GestorModo.getInstancia().activarModoOnline();
                    }
                    break;

                case DialogoConexionBD.CONFIGURAR:
                    System.out.println("\n⚙ Abriendo configuración manual...");
                    boolean configuradoOK = DialogoConfigManualBD.mostrar();

                    if (configuradoOK) {
                        // Reiniciar conexión con nueva configuración
                        ConexionBD nuevaConexion = ConexionBD.getInstancia();
                        conectado = nuevaConexion.probarConexion();

                        if (conectado) {
                            System.out.println("✓ Conexión exitosa con nueva configuración");
                            GestorModo.getInstancia().activarModoOnline();
                        } else {
                            System.out.println("✗ Configuración manual falló");
                            intentos++;
                        }
                    }
                    break;

                case DialogoConexionBD.MODO_OFFLINE:
                    System.out.println("\n🔴 Activando MODO OFFLINE...");
                    GestorModo.getInstancia().activarModoOffline();
                    RepositorioMemoria.getInstancia().inicializarDatosPrueba();
                    return; // Salir del bucle

                case DialogoConexionBD.CANCELAR:
                default:
                    System.out.println("\n[x] Usuario cancelo. Cerrando aplicacion...");
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
