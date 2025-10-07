package com.sigvip.vista;

import com.sigvip.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Vista para gestión de internos.
 * Implementa RF006: Gestionar Internos.
 *
 * NOTA: Esta es una implementación stub/placeholder.
 * Pendiente de implementación completa.
 */
public class VistaGestionInternos extends JFrame {

    private Usuario usuarioActual;

    public VistaGestionInternos(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarComponentes();
        configurarVentana();
    }

    private void configurarVentana() {
        setTitle("SIGVIP - Gestión de Internos (RF006)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void inicializarComponentes() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel("Gestión de Internos", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextArea txtInfo = new JTextArea(
            "RF006: Gestionar Internos\n\n" +
            "Esta funcionalidad permite:\n" +
            "• Registrar nuevos internos\n" +
            "• Actualizar datos de internos\n" +
            "• Gestionar ubicación (pabellón, piso)\n" +
            "• Controlar situación procesal\n" +
            "• Gestionar traslados y egresos\n\n" +
            "ESTADO: En desarrollo\n\n" +
            "Por implementar:\n" +
            "- Formulario de registro de internos\n" +
            "- Búsqueda y edición\n" +
            "- Tabla de internos activos\n" +
            "- Control de ubicaciones"
        );
        txtInfo.setEditable(false);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(txtInfo), BorderLayout.CENTER);
        panel.add(btnCerrar, BorderLayout.SOUTH);

        add(panel);
    }
}
