package com.sigvip.vista;

import com.sigvip.controlador.ControladorUsuarios;
import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.Rol;
import com.sigvip.utilidades.TemaColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Vista para gestión de usuarios del sistema.
 * Implementa RF008: Gestionar Usuarios.
 *
 * Funcionalidades:
 * - Crear nuevos usuarios con hash SHA-256
 * - Modificar datos de usuarios existentes
 * - Activar/Inactivar usuarios
 * - Restablecer contraseñas
 * - Búsqueda por nombre de usuario
 * - Filtros por rol y estado
 *
 * Restricción: Solo usuarios con rol ADMINISTRADOR pueden acceder.
 */
public class VistaGestionUsuarios extends JFrame {

    private Usuario usuarioActual;
    private ControladorUsuarios controlador;

    // Componentes de búsqueda
    private JTextField txtBuscarNombreUsuario;
    private JButton btnBuscar;

    // Componentes del formulario
    private JTextField txtNombreUsuario;
    private JPasswordField txtContrasena;
    private JPasswordField txtConfirmarContrasena;
    private JTextField txtNombreCompleto;
    private JComboBox<Rol> cmbRol;
    private JComboBox<Establecimiento> cmbEstablecimiento;
    private JCheckBox chkActivo;

    // Botones de acción
    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnRestablecerContrasena;

    // Tabla de usuarios
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    // Filtros
    private JComboBox<String> cmbFiltroRol;
    private JComboBox<String> cmbFiltroEstado;
    private JButton btnAplicarFiltros;

    // Usuario seleccionado para edición
    private Usuario usuarioSeleccionado;

    /**
     * Constructor que inicializa la vista.
     *
     * @param usuario usuario autenticado (debe ser ADMINISTRADOR)
     */
    public VistaGestionUsuarios(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorUsuarios(usuario);

        // Validar que el usuario sea ADMINISTRADOR
        if (usuario.getRol() != Rol.ADMINISTRADOR) {
            JOptionPane.showMessageDialog(
                null,
                "Acceso denegado.\n\nSolo usuarios con rol ADMINISTRADOR pueden gestionar usuarios.\n" +
                "Su rol actual: " + usuario.getRol(),
                "Acceso Denegado",
                JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }

        inicializarComponentes();
        configurarVentana();
        cargarEstablecimientos();
        cargarUsuarios();
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Gestión de Usuarios (RF008)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: título y búsqueda
        JPanel panelSuperior = crearPanelSuperior();
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        // Panel central: formulario y tabla
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(450);

        JPanel panelFormulario = crearPanelFormulario();
        JPanel panelTabla = crearPanelTabla();

        splitPane.setLeftComponent(panelFormulario);
        splitPane.setRightComponent(panelTabla);

        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        add(panelPrincipal);
    }

    /**
     * Crea el panel superior con título y búsqueda.
     */
    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Título
        JLabel lblTitulo = new JLabel("Gestión de Usuarios del Sistema");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(lblTitulo, BorderLayout.NORTH);

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Búsqueda"));

        JLabel lblBuscar = new JLabel("Nombre de Usuario:");
        txtBuscarNombreUsuario = new JTextField(20);
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> buscarUsuario());

        panelBusqueda.add(lblBuscar);
        panelBusqueda.add(txtBuscarNombreUsuario);
        panelBusqueda.add(btnBuscar);

        panel.add(panelBusqueda, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de formulario para registro/edición.
     */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Usuario"));

        // Formulario
        JPanel formulario = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Nombre de usuario
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Nombre de Usuario:*"), gbc);
        gbc.gridx = 1;
        txtNombreUsuario = new JTextField(20);
        formulario.add(txtNombreUsuario, gbc);
        fila++;

        // Contraseña
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Contraseña:*"), gbc);
        gbc.gridx = 1;
        txtContrasena = new JPasswordField(20);
        formulario.add(txtContrasena, gbc);
        fila++;

        // Confirmar contraseña
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Confirmar Contraseña:*"), gbc);
        gbc.gridx = 1;
        txtConfirmarContrasena = new JPasswordField(20);
        formulario.add(txtConfirmarContrasena, gbc);
        fila++;

        // Nota sobre contraseña
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        JLabel lblNotaContrasena = new JLabel("<html><i>Mínimo 8 caracteres. En edición, dejar vacío para no cambiar.</i></html>");
        lblNotaContrasena.setFont(new Font("Arial", Font.PLAIN, 10));
        lblNotaContrasena.setForeground(TemaColors.TEXTO_SECUNDARIO);
        formulario.add(lblNotaContrasena, gbc);
        gbc.gridwidth = 1;
        fila++;

        // Separador
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.gridwidth = 2;
        formulario.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;
        fila++;

        // Nombre completo
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Nombre Completo:*"), gbc);
        gbc.gridx = 1;
        txtNombreCompleto = new JTextField(20);
        formulario.add(txtNombreCompleto, gbc);
        fila++;

        // Rol
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Rol:*"), gbc);
        gbc.gridx = 1;
        cmbRol = new JComboBox<>(Rol.values());
        formulario.add(cmbRol, gbc);
        fila++;

        // Establecimiento
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Establecimiento:*"), gbc);
        gbc.gridx = 1;
        cmbEstablecimiento = new JComboBox<>();
        formulario.add(cmbEstablecimiento, gbc);
        fila++;

        // Estado activo
        gbc.gridx = 0;
        gbc.gridy = fila;
        formulario.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        chkActivo = new JCheckBox("Activo");
        chkActivo.setSelected(true);
        formulario.add(chkActivo, gbc);
        fila++;

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnGuardar = new JButton("Guardar Usuario");
        btnGuardar.addActionListener(e -> guardarUsuario());

        btnLimpiar = new JButton("Limpiar Formulario");
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        btnRestablecerContrasena = new JButton("Restablecer Contraseña");
        btnRestablecerContrasena.setEnabled(false);
        btnRestablecerContrasena.addActionListener(e -> restablecerContrasena());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnLimpiar);
        panelBotones.add(btnRestablecerContrasena);

        panel.add(formulario, BorderLayout.CENTER);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel de tabla con usuarios.
     */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Usuarios Registrados"));

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));

        JLabel lblFiltroRol = new JLabel("Rol:");
        cmbFiltroRol = new JComboBox<>(new String[]{"Todos", "OPERADOR", "SUPERVISOR", "ADMINISTRADOR"});

        JLabel lblFiltroEstado = new JLabel("Estado:");
        cmbFiltroEstado = new JComboBox<>(new String[]{"Todos", "Activos", "Inactivos"});

        btnAplicarFiltros = new JButton("Aplicar Filtros");
        btnAplicarFiltros.addActionListener(e -> aplicarFiltros());

        panelFiltros.add(lblFiltroRol);
        panelFiltros.add(cmbFiltroRol);
        panelFiltros.add(lblFiltroEstado);
        panelFiltros.add(cmbFiltroEstado);
        panelFiltros.add(btnAplicarFiltros);

        // Tabla
        String[] columnas = {"ID", "Usuario", "Nombre Completo", "Rol", "Establecimiento", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarUsuarioSeleccionado();
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);

        // Panel de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton btnActivar = new JButton("Activar Usuario");
        btnActivar.addActionListener(e -> activarUsuario());

        JButton btnInactivar = new JButton("Inactivar Usuario");
        btnInactivar.addActionListener(e -> inactivarUsuario());

        JButton btnActualizar = new JButton("Actualizar Tabla");
        btnActualizar.addActionListener(e -> cargarUsuarios());

        panelAcciones.add(btnActivar);
        panelAcciones.add(btnInactivar);
        panelAcciones.add(btnActualizar);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        return panel;
    }

    // ===== MÉTODOS DE NEGOCIO =====

    /**
     * Carga los establecimientos en el combo box.
     */
    private void cargarEstablecimientos() {
        try {
            List<Establecimiento> establecimientos = controlador.listarEstablecimientos();
            cmbEstablecimiento.removeAllItems();

            for (Establecimiento est : establecimientos) {
                cmbEstablecimiento.addItem(est);
            }

        } catch (SQLException e) {
            mostrarError("Error al cargar establecimientos", e);
        }
    }

    /**
     * Carga los usuarios en la tabla.
     */
    private void cargarUsuarios() {
        try {
            List<Usuario> usuarios = controlador.listarTodos();
            actualizarTabla(usuarios);

        } catch (SQLException e) {
            mostrarError("Error al cargar usuarios", e);
        }
    }

    /**
     * Actualiza la tabla con la lista de usuarios proporcionada.
     */
    private void actualizarTabla(List<Usuario> usuarios) {
        modeloTabla.setRowCount(0);

        for (Usuario usuario : usuarios) {
            Object[] fila = {
                usuario.getIdUsuario(),
                usuario.getNombreUsuario(),
                usuario.getNombreCompleto(),
                usuario.getRol(),
                usuario.getEstablecimiento() != null ? usuario.getEstablecimiento().getNombre() : "N/A",
                usuario.isActivo() ? "Activo" : "Inactivo"
            };
            modeloTabla.addRow(fila);
        }
    }

    /**
     * Busca un usuario por nombre de usuario.
     */
    private void buscarUsuario() {
        String nombreUsuario = txtBuscarNombreUsuario.getText().trim();

        if (nombreUsuario.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Debe ingresar un nombre de usuario para buscar",
                "Validación",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            Usuario usuario = controlador.buscarPorNombreUsuario(nombreUsuario);

            if (usuario == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se encontró ningún usuario con nombre de usuario: " + nombreUsuario,
                    "Búsqueda",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // Cargar usuario en el formulario
            usuarioSeleccionado = usuario;
            txtNombreUsuario.setText(usuario.getNombreUsuario());
            txtNombreUsuario.setEditable(false); // No permitir cambiar nombre de usuario
            txtNombreCompleto.setText(usuario.getNombreCompleto());
            cmbRol.setSelectedItem(usuario.getRol());
            chkActivo.setSelected(usuario.isActivo());

            if (usuario.getEstablecimiento() != null) {
                for (int i = 0; i < cmbEstablecimiento.getItemCount(); i++) {
                    Establecimiento est = cmbEstablecimiento.getItemAt(i);
                    if (est.getIdEstablecimiento().equals(usuario.getEstablecimiento().getIdEstablecimiento())) {
                        cmbEstablecimiento.setSelectedIndex(i);
                        break;
                    }
                }
            }

            // Limpiar campos de contraseña
            txtContrasena.setText("");
            txtConfirmarContrasena.setText("");

            // Habilitar botón de restablecer contraseña
            btnRestablecerContrasena.setEnabled(true);
            btnGuardar.setText("Actualizar Usuario");

        } catch (SQLException e) {
            mostrarError("Error al buscar usuario", e);
        }
    }

    /**
     * Guarda un nuevo usuario o actualiza uno existente.
     */
    private void guardarUsuario() {
        try {
            // Validar datos obligatorios
            String nombreUsuario = txtNombreUsuario.getText().trim();
            String nombreCompleto = txtNombreCompleto.getText().trim();
            Rol rol = (Rol) cmbRol.getSelectedItem();
            Establecimiento establecimiento = (Establecimiento) cmbEstablecimiento.getSelectedItem();

            if (nombreUsuario.isEmpty() || nombreCompleto.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this,
                    "Debe completar todos los campos obligatorios (*)",
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (usuarioSeleccionado == null) {
                // Crear nuevo usuario
                String contrasena = new String(txtContrasena.getPassword());
                String confirmarContrasena = new String(txtConfirmarContrasena.getPassword());

                if (contrasena.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Debe ingresar una contraseña para el nuevo usuario",
                        "Validación",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (!contrasena.equals(confirmarContrasena)) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Las contraseñas no coinciden",
                        "Validación",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                Usuario nuevoUsuario = controlador.crearUsuario(
                    nombreUsuario,
                    contrasena,
                    nombreCompleto,
                    rol,
                    establecimiento != null ? establecimiento.getIdEstablecimiento() : null
                );

                JOptionPane.showMessageDialog(
                    this,
                    "Usuario creado exitosamente.\n\n" +
                    "Nombre de Usuario: " + nuevoUsuario.getNombreUsuario() + "\n" +
                    "Nombre Completo: " + nuevoUsuario.getNombreCompleto() + "\n" +
                    "Rol: " + nuevoUsuario.getRol(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                // Actualizar usuario existente
                usuarioSeleccionado.setNombreCompleto(nombreCompleto);
                usuarioSeleccionado.setRol(rol);
                usuarioSeleccionado.setEstablecimiento(establecimiento);
                usuarioSeleccionado.setActivo(chkActivo.isSelected());

                controlador.actualizarUsuario(usuarioSeleccionado);

                JOptionPane.showMessageDialog(
                    this,
                    "Usuario actualizado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

            limpiarFormulario();
            cargarUsuarios();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error de validación:\n\n" + e.getMessage(),
                "Validación",
                JOptionPane.WARNING_MESSAGE
            );

        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(
                this,
                "Error:\n\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );

        } catch (SecurityException e) {
            JOptionPane.showMessageDialog(
                this,
                "Acceso denegado:\n\n" + e.getMessage(),
                "Seguridad",
                JOptionPane.ERROR_MESSAGE
            );

        } catch (SQLException e) {
            mostrarError("Error al guardar usuario", e);
        }
    }

    /**
     * Restablece la contraseña de un usuario (solo ADMINISTRADOR).
     */
    private void restablecerContrasena() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(
                this,
                "Debe seleccionar un usuario primero",
                "Validación",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Solicitar nueva contraseña
        JPasswordField txtNuevaContrasena = new JPasswordField(20);
        JPasswordField txtConfirmarNuevaContrasena = new JPasswordField(20);

        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.add(new JLabel("Nueva Contraseña (mínimo 8 caracteres):"));
        panel.add(txtNuevaContrasena);
        panel.add(new JLabel("Confirmar Nueva Contraseña:"));
        panel.add(txtConfirmarNuevaContrasena);

        int resultado = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Restablecer Contraseña - " + usuarioSeleccionado.getNombreUsuario(),
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (resultado == JOptionPane.OK_OPTION) {
            String nuevaContrasena = new String(txtNuevaContrasena.getPassword());
            String confirmarContrasena = new String(txtConfirmarNuevaContrasena.getPassword());

            if (!nuevaContrasena.equals(confirmarContrasena)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Las contraseñas no coinciden",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            try {
                controlador.restablecerContrasena(usuarioSeleccionado.getIdUsuario(), nuevaContrasena);

                JOptionPane.showMessageDialog(
                    this,
                    "Contraseña restablecida exitosamente para el usuario:\n" +
                    usuarioSeleccionado.getNombreUsuario(),
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error de validación:\n\n" + e.getMessage(),
                    "Validación",
                    JOptionPane.WARNING_MESSAGE
                );

            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Acceso denegado:\n\n" + e.getMessage(),
                    "Seguridad",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (SQLException e) {
                mostrarError("Error al restablecer contraseña", e);
            }
        }
    }

    /**
     * Carga el usuario seleccionado de la tabla en el formulario.
     */
    private void cargarUsuarioSeleccionado() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();

        if (filaSeleccionada >= 0) {
            Long idUsuario = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

            try {
                usuarioSeleccionado = controlador.buscarPorId(idUsuario);

                if (usuarioSeleccionado != null) {
                    txtNombreUsuario.setText(usuarioSeleccionado.getNombreUsuario());
                    txtNombreUsuario.setEditable(false);
                    txtNombreCompleto.setText(usuarioSeleccionado.getNombreCompleto());
                    cmbRol.setSelectedItem(usuarioSeleccionado.getRol());
                    chkActivo.setSelected(usuarioSeleccionado.isActivo());

                    if (usuarioSeleccionado.getEstablecimiento() != null) {
                        for (int i = 0; i < cmbEstablecimiento.getItemCount(); i++) {
                            Establecimiento est = cmbEstablecimiento.getItemAt(i);
                            if (est.getIdEstablecimiento().equals(
                                    usuarioSeleccionado.getEstablecimiento().getIdEstablecimiento())) {
                                cmbEstablecimiento.setSelectedIndex(i);
                                break;
                            }
                        }
                    }

                    txtContrasena.setText("");
                    txtConfirmarContrasena.setText("");

                    btnRestablecerContrasena.setEnabled(true);
                    btnGuardar.setText("Actualizar Usuario");
                }

            } catch (SQLException e) {
                mostrarError("Error al cargar usuario", e);
            }
        }
    }

    /**
     * Aplica filtros a la tabla de usuarios.
     */
    private void aplicarFiltros() {
        try {
            String filtroRol = (String) cmbFiltroRol.getSelectedItem();
            String filtroEstado = (String) cmbFiltroEstado.getSelectedItem();

            List<Usuario> usuarios;

            // Aplicar filtro de rol
            if ("Todos".equals(filtroRol)) {
                usuarios = controlador.listarTodos();
            } else {
                Rol rol = Rol.valueOf(filtroRol);
                usuarios = controlador.listarPorRol(rol);
            }

            // Aplicar filtro de estado
            if ("Activos".equals(filtroEstado)) {
                usuarios.removeIf(u -> !u.isActivo());
            } else if ("Inactivos".equals(filtroEstado)) {
                usuarios.removeIf(Usuario::isActivo);
            }

            actualizarTabla(usuarios);

        } catch (SQLException e) {
            mostrarError("Error al aplicar filtros", e);
        }
    }

    /**
     * Activa un usuario inactivo.
     */
    private void activarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Debe seleccionar un usuario de la tabla",
                "Validación",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Long idUsuario = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombreUsuario = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de activar el usuario: " + nombreUsuario + "?",
            "Confirmar Activación",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                controlador.activarUsuario(idUsuario);

                JOptionPane.showMessageDialog(
                    this,
                    "Usuario activado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                cargarUsuarios();

            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Acceso denegado:\n\n" + e.getMessage(),
                    "Seguridad",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (SQLException e) {
                mostrarError("Error al activar usuario", e);
            }
        }
    }

    /**
     * Inactiva un usuario.
     */
    private void inactivarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(
                this,
                "Debe seleccionar un usuario de la tabla",
                "Validación",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Long idUsuario = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);
        String nombreUsuario = (String) modeloTabla.getValueAt(filaSeleccionada, 1);

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro de inactivar el usuario: " + nombreUsuario + "?\n\n" +
            "El usuario no podrá iniciar sesión hasta que sea reactivado.",
            "Confirmar Inactivación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                controlador.inactivarUsuario(idUsuario);

                JOptionPane.showMessageDialog(
                    this,
                    "Usuario inactivado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE
                );

                cargarUsuarios();
                limpiarFormulario();

            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error:\n\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (SecurityException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Acceso denegado:\n\n" + e.getMessage(),
                    "Seguridad",
                    JOptionPane.ERROR_MESSAGE
                );

            } catch (SQLException e) {
                mostrarError("Error al inactivar usuario", e);
            }
        }
    }

    /**
     * Limpia el formulario.
     */
    private void limpiarFormulario() {
        usuarioSeleccionado = null;
        txtNombreUsuario.setText("");
        txtNombreUsuario.setEditable(true);
        txtContrasena.setText("");
        txtConfirmarContrasena.setText("");
        txtNombreCompleto.setText("");
        cmbRol.setSelectedIndex(0);
        cmbEstablecimiento.setSelectedIndex(0);
        chkActivo.setSelected(true);
        btnRestablecerContrasena.setEnabled(false);
        btnGuardar.setText("Guardar Usuario");
        tablaUsuarios.clearSelection();
    }

    /**
     * Muestra un mensaje de error con detalles de la excepción.
     */
    private void mostrarError(String mensaje, Exception e) {
        JOptionPane.showMessageDialog(
            this,
            mensaje + ":\n\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
        e.printStackTrace();
    }
}
