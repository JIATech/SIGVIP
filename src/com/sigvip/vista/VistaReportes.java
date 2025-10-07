package com.sigvip.vista;

import com.sigvip.modelo.Usuario;

import javax.swing.*;
import java.awt.*;

/**
 * Vista para generación de reportes.
 * Implementa RF007: Generar Reportes.
 *
 * NOTA: Esta es una implementación stub/placeholder.
 * Pendiente de implementación completa con exportación PDF.
 */
public class VistaReportes extends JFrame {

    private Usuario usuarioActual;

    public VistaReportes(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarComponentes();
        configurarVentana();
    }

    private void configurarVentana() {
        setTitle("SIGVIP - Reportes (RF007)");
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void inicializarComponentes() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel lblTitulo = new JLabel("Generación de Reportes", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));

        JTextArea txtInfo = new JTextArea(
            "RF007: Generar Reportes\n\n" +
            "Esta funcionalidad permite generar reportes en PDF:\n\n" +
            "Tipos de reportes disponibles:\n" +
            "• Reporte de visitas por fecha\n" +
            "• Reporte de visitas por visitante\n" +
            "• Reporte de visitas por interno\n" +
            "• Estadísticas de visitas\n" +
            "• Reporte de restricciones activas\n" +
            "• Reporte de autorizaciones vigentes\n\n" +
            "ESTADO: En desarrollo\n\n" +
            "Por implementar:\n" +
            "- Selección de tipo de reporte\n" +
            "- Filtros por fecha, visitante, interno\n" +
            "- Generación de PDF con Apache PDFBox\n" +
            "- Vista previa de reportes\n" +
            "- Exportación a archivo"
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
