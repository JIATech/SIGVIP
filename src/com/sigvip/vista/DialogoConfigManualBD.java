package com.sigvip.vista;

import com.sigvip.persistencia.ConexionBD;
import com.sigvip.utilidades.TemaColors;

import javax.swing.*;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Diálogo para configurar manualmente las credenciales de conexión a MySQL.
 * Permite al usuario introducir URL, usuario y contraseña, probar la conexión
 * y opcionalmente guardar en config.properties.
 *
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class DialogoConfigManualBD extends JDialog {

    private JTextField txtUrl;
    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JCheckBox chkGuardar;

    private boolean conexionExitosa = false;

    /**
     * Constructor del diálogo.
     *
     * @param parent ventana padre
     */
    public DialogoConfigManualBD(Frame parent) {
        super(parent, "Configuración Manual de Base de Datos", true);
        inicializarComponentes();
    }

    /**
     * Inicializa y configura los componentes del diálogo.
     */
    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Panel superior con instrucciones
        JPanel panelInstrucciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInstrucciones.setBorder(BorderFactory.createEmptyBorder(15, 15, 5, 15));
        JLabel lblInstrucciones = new JLabel(
            "<html><b>Introduzca las credenciales de conexión a MySQL:</b></html>"
        );
        panelInstrucciones.add(lblInstrucciones);
        add(panelInstrucciones, BorderLayout.NORTH);

        // Panel central con formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // URL
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("URL JDBC:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtUrl = new JTextField("jdbc:mysql://localhost:3306/sigvip_db?useUnicode=true&characterEncoding=utf8mb4", 30);
        panelFormulario.add(txtUrl, gbc);

        // Usuario
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtUsuario = new JTextField("root", 30);
        panelFormulario.add(txtUsuario, gbc);

        // Contraseña
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        panelFormulario.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtContrasena = new JPasswordField(30);
        panelFormulario.add(txtContrasena, gbc);

        // Checkbox para guardar
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        chkGuardar = new JCheckBox("Guardar configuración en config.properties");
        chkGuardar.setSelected(true);
        panelFormulario.add(chkGuardar, gbc);

        add(panelFormulario, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 0, 15, 0));

        JButton btnProbar = new JButton("Probar Conexión");
        btnProbar.setFont(new Font("Arial", Font.BOLD, 12));
        btnProbar.addActionListener(e -> probarConexion());

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.addActionListener(e -> aceptar());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            conexionExitosa = false;
            dispose();
        });

        panelBotones.add(btnProbar);
        panelBotones.add(btnAceptar);
        panelBotones.add(btnCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        // Configuración final
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    /**
     * Prueba la conexión con las credenciales introducidas.
     */
    private void probarConexion() {
        String url = txtUrl.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        if (url.isEmpty() || usuario.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "La URL y el usuario son obligatorios",
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            // Intentar conectar directamente con las credenciales proporcionadas
            Connection conn = java.sql.DriverManager.getConnection(url, usuario, contrasena);
            conn.close();

            JOptionPane.showMessageDialog(
                this,
                "Conexión exitosa\n\nPuede aceptar para aplicar la configuración.",
                "Prueba de Conexión",
                JOptionPane.INFORMATION_MESSAGE
            );
            conexionExitosa = true;

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error al conectar:\n\n" + ex.getMessage() +
                "\n\nVerifique URL, usuario y contraseña.",
                "Error de Conexión",
                JOptionPane.ERROR_MESSAGE
            );
            conexionExitosa = false;
        }
    }

    /**
     * Acepta la configuración y opcionalmente la guarda.
     */
    private void aceptar() {
        if (!conexionExitosa) {
            int respuesta = JOptionPane.showConfirmDialog(
                this,
                "No se ha probado la conexión.\n¿Desea aceptar de todas formas?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
        }

        // Guardar en archivo si está marcado
        if (chkGuardar.isSelected()) {
            guardarConfiguracion();
        }

        dispose();
    }

    /**
     * Guarda la configuración en config.properties.
     */
    private void guardarConfiguracion() {
        Properties props = new Properties();
        props.setProperty("db.url", txtUrl.getText().trim());
        props.setProperty("db.usuario", txtUsuario.getText().trim());
        props.setProperty("db.contrasena", new String(txtContrasena.getPassword()));
        props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try (FileOutputStream fos = new FileOutputStream("resources/config.properties")) {
            props.store(fos, "Configuración de conexión a MySQL - Generado por SIGVIP");
            JOptionPane.showMessageDialog(
                this,
                "Configuración guardada exitosamente en config.properties",
                "Guardar Configuración",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error al guardar configuración:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Verifica si la conexión fue exitosa.
     *
     * @return true si la conexión fue exitosa
     */
    public boolean isConexionExitosa() {
        return conexionExitosa;
    }

    /**
     * Muestra el diálogo y retorna si la configuración fue exitosa.
     * Método estático de conveniencia.
     *
     * @return true si se configuró exitosamente
     */
    public static boolean mostrar() {
        DialogoConfigManualBD dialogo = new DialogoConfigManualBD(null);
        dialogo.setVisible(true);
        return dialogo.isConexionExitosa();
    }
}
