package com.sigvip.vista;

import com.sigvip.utilidades.TemaColors;
import javax.swing.*;
import java.awt.*;

/**
 * Diálogo que se muestra cuando falla la conexión a la base de datos MySQL.
 * Ofrece 3 opciones al usuario:
 * - Reintentar conexión
 * - Configurar manualmente credenciales de BD
 * - Activar modo offline (datos solo en memoria)
 *
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class DialogoConexionBD extends JDialog {

    // Constantes para identificar la opción seleccionada
    public static final int REINTENTAR = 1;
    public static final int CONFIGURAR = 2;
    public static final int MODO_OFFLINE = 3;
    public static final int CANCELAR = 0;

    private int opcionSeleccionada = CANCELAR;

    /**
     * Constructor del diálogo.
     *
     * @param parent ventana padre (puede ser null)
     */
    public DialogoConexionBD(Frame parent) {
        super(parent, "Error de Conexión - SIGVIP", true);
        inicializarComponentes();
    }

    /**
     * Inicializa y configura los componentes del diálogo.
     */
    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel superior con mensaje de error
        JPanel panelMensaje = new JPanel(new BorderLayout());
        panelMensaje.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel iconoError = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
        panelMensaje.add(iconoError, BorderLayout.WEST);

        JTextArea textoError = new JTextArea(
            "No se pudo conectar a la base de datos MySQL.\n\n" +
            "Verifique:\n" +
            "  • MySQL está instalado y ejecutándose\n" +
            "  • La base de datos 'sigvip_db' existe\n" +
            "  • Las credenciales en resources/config.properties son correctas\n" +
            "  • MySQL Connector/J está en lib/ y en el classpath\n\n" +
            "Elija una opción para continuar:"
        );
        textoError.setEditable(false);
        textoError.setOpaque(false);
        textoError.setFont(new Font("Arial", Font.PLAIN, 12));
        textoError.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        panelMensaje.add(textoError, BorderLayout.CENTER);

        add(panelMensaje, BorderLayout.NORTH);

        // Panel central con botones de opciones
        JPanel panelOpciones = new JPanel(new GridLayout(3, 1, 10, 10));
        panelOpciones.setBorder(BorderFactory.createEmptyBorder(10, 30, 20, 30));

        JButton btnReintentar = new JButton("Reintentar Conexión");
        btnReintentar.setFont(new Font("Arial", Font.BOLD, 13));
        btnReintentar.setToolTipText("Volver a intentar con la configuración actual");
        btnReintentar.addActionListener(e -> {
            opcionSeleccionada = REINTENTAR;
            dispose();
        });

        JButton btnConfigurar = new JButton("Configurar Manualmente");
        btnConfigurar.setFont(new Font("Arial", Font.BOLD, 13));
        btnConfigurar.setToolTipText("Introducir credenciales de base de datos manualmente");
        btnConfigurar.addActionListener(e -> {
            opcionSeleccionada = CONFIGURAR;
            dispose();
        });

        JButton btnModoOffline = new JButton("Modo Offline (Limitado)");
        btnModoOffline.setFont(new Font("Arial", Font.BOLD, 13));
        btnModoOffline.setForeground(TemaColors.ESTADO_ERROR);
        btnModoOffline.setToolTipText("Funcionar sin base de datos (datos solo en memoria)");
        btnModoOffline.addActionListener(e -> {
            // Mostrar advertencia antes de activar modo offline
            int confirmacion = JOptionPane.showConfirmDialog(
                this,
                "El modo offline es LIMITADO:\n\n" +
                "  • Los datos se almacenan solo en memoria RAM\n" +
                "  • Se pierden al cerrar la aplicación\n" +
                "  • Solo para demostración sin MySQL\n" +
                "  • Funcionalidad reducida\n\n" +
                "¿Desea continuar en modo offline?",
                "Confirmar Modo Offline",
                JOptionPane.WARNING_MESSAGE
            );

            if (confirmacion == JOptionPane.YES_OPTION) {
                opcionSeleccionada = MODO_OFFLINE;
                dispose();
            }
        });

        panelOpciones.add(btnReintentar);
        panelOpciones.add(btnConfigurar);
        panelOpciones.add(btnModoOffline);

        add(panelOpciones, BorderLayout.CENTER);

        // Panel inferior con botón cancelar
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setToolTipText("Cerrar la aplicación");
        btnCancelar.addActionListener(e -> {
            opcionSeleccionada = CANCELAR;
            dispose();
        });

        panelInferior.add(btnCancelar);
        add(panelInferior, BorderLayout.SOUTH);

        // Configuración final de la ventana
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * Obtiene la opción seleccionada por el usuario.
     *
     * @return REINTENTAR, CONFIGURAR, MODO_OFFLINE o CANCELAR
     */
    public int getOpcionSeleccionada() {
        return opcionSeleccionada;
    }

    /**
     * Muestra el diálogo y retorna la opción seleccionada.
     * Método estático de conveniencia.
     *
     * @return opción seleccionada por el usuario
     */
    public static int mostrar() {
        DialogoConexionBD dialogo = new DialogoConexionBD(null);
        dialogo.setVisible(true);
        return dialogo.getOpcionSeleccionada();
    }
}
