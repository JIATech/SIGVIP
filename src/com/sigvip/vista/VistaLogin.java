package com.sigvip.vista;

import com.sigvip.modelo.Usuario;
import com.sigvip.persistencia.UsuarioDAO;
import com.sigvip.utilidades.TemaColors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.security.MessageDigest;
import java.sql.SQLException;

/**
 * Vista de autenticación de usuarios del sistema.
 * Implementa RF008: Gestión de Usuarios - Autenticación.
 *
 * Funcionalidades:
 * - Login con usuario y contraseña
 * - Validación de credenciales con hash SHA-256
 * - Apertura de ventana principal tras autenticación exitosa
 * - Registro de eventos de login en auditoría
 *
 * Especificación: RNF003 - Seguridad
 */
public class VistaLogin extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtContrasena;
    private JButton btnIngresar;
    private JButton btnCancelar;
    private UsuarioDAO usuarioDAO;
    private Usuario usuarioAutenticado;

    /**
     * Constructor que inicializa la ventana de login.
     */
    public VistaLogin() {
        this.usuarioDAO = new UsuarioDAO();
        inicializarComponentes();
        configurarVentana();
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null); // Centrar en pantalla
    }

    /**
     * Inicializa y configura todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        // Contenedor principal
        JPanel contenedorCompleto = new JPanel(new BorderLayout());

        // Banner de modo offline (si aplica)
        if (com.sigvip.persistencia.GestorModo.getInstancia().isModoOffline()) {
            JPanel bannerOffline = crearBannerModoOffline();
            contenedorCompleto.add(bannerOffline, BorderLayout.NORTH);
        }

        // Panel principal con padding
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel de encabezado
        JPanel panelEncabezado = crearPanelEncabezado();
        panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);

        // Panel de formulario
        JPanel panelFormulario = crearPanelFormulario();
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = crearPanelBotones();
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        contenedorCompleto.add(panelPrincipal, BorderLayout.CENTER);
        add(contenedorCompleto);
    }

    /**
     * Crea el panel de encabezado con título y logo.
     */
    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Título
        JLabel lblTitulo = new JLabel("SIGVIP", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(TemaColors.PRIMARIO);

        // Subtítulo
        JLabel lblSubtitulo = new JLabel(
            "Sistema de Gestión de Visitas Penitenciarias",
            SwingConstants.CENTER
        );
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubtitulo.setForeground(TemaColors.TEXTO_SECUNDARIO);

        // Panel de títulos
        JPanel panelTitulos = new JPanel(new GridLayout(2, 1, 0, 5));
        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSubtitulo);

        panel.add(panelTitulos, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de formulario con campos de usuario y contraseña.
     */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Credenciales de Acceso"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Etiqueta y campo de usuario
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblUsuario = new JLabel("Usuario:");
        panel.add(lblUsuario, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtUsuario = new JTextField(20);
        txtUsuario.setToolTipText("Ingrese su nombre de usuario");
        panel.add(txtUsuario, gbc);

        // Etiqueta y campo de contraseña
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblContrasena = new JLabel("Contraseña:");
        panel.add(lblContrasena, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtContrasena = new JPasswordField(20);
        txtContrasena.setToolTipText("Ingrese su contraseña");
        panel.add(txtContrasena, gbc);

        // Permitir login con Enter
        txtContrasena.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    intentarLogin();
                }
            }
        });

        return panel;
    }

    /**
     * Crea el panel de botones de acción.
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(100, 30));
        TemaColors.aplicarEstiloBotonCancelar(btnCancelar);
        btnCancelar.addActionListener(e -> System.exit(0));

        btnIngresar = new JButton("Ingresar");
        btnIngresar.setPreferredSize(new Dimension(100, 30));
        TemaColors.aplicarEstiloBotonAccion(btnIngresar);
        btnIngresar.addActionListener(e -> intentarLogin());

        panel.add(btnCancelar);
        panel.add(btnIngresar);

        // Hacer que Ingresar sea el botón por defecto
        getRootPane().setDefaultButton(btnIngresar);

        return panel;
    }

    /**
     * Intenta autenticar al usuario con las credenciales ingresadas.
     */
    private void intentarLogin() {
        String nombreUsuario = txtUsuario.getText().trim();
        String contrasena = new String(txtContrasena.getPassword());

        // Validar campos no vacíos
        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            mostrarError("Por favor complete todos los campos");
            return;
        }

        // Deshabilitar botones durante autenticación
        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        // Ejecutar autenticación en hilo separado para no bloquear UI
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return autenticar(nombreUsuario, contrasena);
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Login exitoso
                        abrirVentanaPrincipal();
                    } else {
                        // Credenciales inválidas
                        mostrarError("Usuario o contraseña incorrectos");
                        txtContrasena.setText("");
                        txtContrasena.requestFocus();
                    }
                } catch (Exception e) {
                    mostrarError("Error al autenticar: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("Ingresar");
                }
            }
        };

        worker.execute();
    }

    /**
     * Autentica un usuario contra la base de datos.
     * Implementa hashing SHA-256 de contraseñas (RNF003).
     *
     * @param nombreUsuario nombre de usuario
     * @param contrasena contraseña en texto plano
     * @return true si las credenciales son válidas
     */
    private boolean autenticar(String nombreUsuario, String contrasena) {
        try {
            // Buscar usuario en base de datos
            Usuario usuario = usuarioDAO.buscarPorNombreUsuario(nombreUsuario);

            if (usuario == null) {
                return false;
            }

            // Verificar que el usuario esté activo
            if (!usuario.isActivo()) {
                SwingUtilities.invokeLater(() ->
                    mostrarError("El usuario está inactivo. Contacte al administrador.")
                );
                return false;
            }

            // Hashear contraseña ingresada y comparar
            String contrasenaHash = hashearContrasena(contrasena);

            if (contrasenaHash.equals(usuario.getContrasena())) {
                this.usuarioAutenticado = usuario;

                // Registrar último acceso
                usuarioDAO.actualizarUltimoAcceso(usuario.getIdUsuario());

                // TODO: Registrar evento de LOGIN en tabla auditoria

                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Genera hash SHA-256 de una contraseña.
     * Implementa RNF003: Seguridad - Hashing de contraseñas.
     *
     * @param contrasena contraseña en texto plano
     * @return hash SHA-256 en hexadecimal
     */
    private String hashearContrasena(String contrasena) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contrasena.getBytes("UTF-8"));

            // Convertir a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    /**
     * Abre la ventana principal tras autenticación exitosa.
     */
    private void abrirVentanaPrincipal() {
        // Cerrar ventana de login
        dispose();

        // Abrir ventana principal
        SwingUtilities.invokeLater(() -> {
            VistaMenuPrincipal ventanaPrincipal = new VistaMenuPrincipal(usuarioAutenticado);
            ventanaPrincipal.setVisible(true);
        });
    }

    /**
     * Muestra un diálogo de error.
     *
     * @param mensaje mensaje a mostrar
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error de Autenticación",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Crea el banner de advertencia para modo offline.
     *
     * @return panel con el banner de modo offline
     */
    private JPanel crearBannerModoOffline() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panel.setBackground(new java.awt.Color(255, 152, 0)); // Naranja

        JLabel lblAdvertencia = new JLabel(
            "⚠ MODO OFFLINE - Los datos se almacenan solo en memoria y se perderán al cerrar la aplicación"
        );
        lblAdvertencia.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        lblAdvertencia.setForeground(java.awt.Color.WHITE);

        panel.add(lblAdvertencia);
        return panel;
    }

    /**
     * Obtiene el usuario autenticado.
     *
     * @return usuario autenticado o null si no hay sesión
     */
    public Usuario getUsuarioAutenticado() {
        return usuarioAutenticado;
    }
}
