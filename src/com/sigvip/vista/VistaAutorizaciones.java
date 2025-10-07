package com.sigvip.vista;

import com.sigvip.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Vista para gestión de autorizaciones de visita.
 * Implementa RF002: Autorizar Visita.
 *
 * NOTA: Esta es una implementación stub/placeholder.
 * Pendiente de implementación completa.
 */
public class VistaAutorizaciones extends JFrame {

    private Usuario usuarioActual;

    public VistaAutorizaciones(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarComponentes();
        configurarVentana();
    }

    private void configurarVentana() {
        setTitle("SIGVIP - Autorizaciones de Visita (RF002)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void inicializarComponentes() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel("Gestión de Autorizaciones", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextArea txtInfo = new JTextArea(
            "RF002: Autorizar Visita\n\n" +
            "Esta funcionalidad permite:\n" +
            "• Crear nuevas autorizaciones visitante-interno\n" +
            "• Especificar el tipo de relación\n" +
            "• Establecer fecha de vencimiento\n" +
            "• Gestionar estados de autorizaciones\n\n" +
            "ESTADO: En desarrollo\n\n" +
            "Por implementar:\n" +
            "- Formulario de nueva autorización\n" +
            "- Búsqueda de visitantes e internos\n" +
            "- Listado de autorizaciones existentes\n" +
            "- Modificación de autorizaciones"
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
