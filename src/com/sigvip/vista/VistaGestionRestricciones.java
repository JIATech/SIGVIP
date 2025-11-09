package com.sigvip.vista;

import com.sigvip.controlador.ControladorRestricciones;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Restriccion;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.AplicableA;
import com.sigvip.modelo.enums.Rol;
import com.sigvip.modelo.enums.TipoRestriccion;
import com.sigvip.utilidades.TemaColors;
import com.sigvip.vista.componentes.JDatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Vista para gestión de restricciones de acceso.
 * Implementa RF009: Registrar Restricciones.
 *
 * Funcionalidades:
 * - Crear restricciones con alcance TODOS o INTERNO_ESPECIFICO
 * - Listar restricciones con filtros
 * - Levantar restricción con motivo
 * - Extender restricción con nueva fecha
 * - Eliminar restricción (solo ADMINISTRADOR)
 * - Alertas de restricciones próximas a vencer
 *
 * Especificación: PDF Sección 7.1 (RF009)
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class VistaGestionRestricciones extends JFrame {

    private final Usuario usuarioActual;
    private final ControladorRestricciones controlador;

    // Componentes de búsqueda de visitante
    private JTextField txtDniVisitante;
    private JButton btnBuscarVisitante;
    private JLabel lblVisitanteSeleccionado;
    private Visitante visitanteSeleccionado;

    // Componentes de formulario de restricción
    private JComboBox<TipoRestriccion> cmbTipoRestriccion;
    private JTextArea txtMotivo;
    private JDatePicker datePickerFechaInicio;
    private JDatePicker datePickerFechaFin;
    private JCheckBox chkIndefinida;

    // Componentes de alcance
    private ButtonGroup groupAlcance;
    private JRadioButton rbTodos;
    private JRadioButton rbInternoEspecifico;
    private JTextField txtLegajoInterno;
    private JButton btnBuscarInterno;
    private JLabel lblInternoSeleccionado;
    private Interno internoSeleccionado;
    private JPanel panelInternoEspecifico;

    // Botones de formulario
    private JButton btnCrearRestriccion;
    private JButton btnLimpiarFormulario;

    // Componentes de listado
    private JTable tblRestricciones;
    private DefaultTableModel modeloTabla;
    private JComboBox<String> cmbFiltroEstado;
    private JComboBox<String> cmbFiltroTipo;
    private JButton btnAplicarFiltros;
    private JButton btnLevantar;
    private JButton btnExtender;
    private JButton btnEliminar;

    // Panel de estadísticas
    private JLabel lblTotalActivas;
    private JLabel lblProximasVencer;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor de la vista.
     *
     * @param usuario usuario autenticado
     */
    public VistaGestionRestricciones(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorRestricciones(usuario);

        // Validar que el usuario sea ADMINISTRADOR o SUPERVISOR
        if (usuario.getRol() != Rol.ADMINISTRADOR && usuario.getRol() != Rol.SUPERVISOR) {
            JOptionPane.showMessageDialog(
                null,
                "Acceso denegado.\n\nSolo usuarios con rol ADMINISTRADOR o SUPERVISOR pueden gestionar restricciones.\n" +
                "Su rol actual: " + usuario.getRol(),
                "Acceso Denegado",
                JOptionPane.ERROR_MESSAGE
            );
            dispose();
            return;
        }

        inicializarComponentes();
        configurarVentana();
        cargarRestricciones();
        actualizarEstadisticas();
    }

    /**
     * Configura la ventana principal.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Gestión de Restricciones de Acceso (RF009)");
        setSize(1400, 850);
        setMinimumSize(new Dimension(1200, 750));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        // Contenedor principal
        JPanel contenedorCompleto = new JPanel(new BorderLayout());

        // Banner de modo offline (si aplica)
        if (com.sigvip.persistencia.GestorModo.getInstancia().isModoOffline()) {
            JPanel bannerOffline = crearBannerModoOffline();
            contenedorCompleto.add(bannerOffline, BorderLayout.NORTH);
        }

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: Título
        JPanel panelTitulo = crearPanelTitulo();
        panelPrincipal.add(panelTitulo, BorderLayout.NORTH);

        // Panel central: Dividido en formulario y listado
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(550);
        splitPane.setLeftComponent(crearPanelFormulario());
        splitPane.setRightComponent(crearPanelListado());
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        contenedorCompleto.add(panelPrincipal, BorderLayout.CENTER);
        add(contenedorCompleto);
    }

    /**
     * Crea el panel de título.
     */
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(TemaColors.FONDO_ENCABEZADO);

        JLabel lblTitulo = new JLabel("GESTIÓN DE RESTRICCIONES DE ACCESO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(TemaColors.TEXTO_CLARO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de formulario de nueva restricción.
     */
    private JPanel crearPanelFormulario() {
        JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Nueva Restricción"));

        // Panel de contenido con scroll
        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BoxLayout(panelContenido, BoxLayout.Y_AXIS));

        // Panel de búsqueda de visitante
        panelContenido.add(crearPanelBusquedaVisitante());
        panelContenido.add(Box.createVerticalStrut(10));

        // Panel de datos de restricción
        panelContenido.add(crearPanelDatosRestriccion());
        panelContenido.add(Box.createVerticalStrut(10));

        // Panel de alcance
        panelContenido.add(crearPanelAlcance());
        panelContenido.add(Box.createVerticalStrut(10));

        // Panel de botones
        panelContenido.add(crearPanelBotonesFormulario());

        JScrollPane scrollPane = new JScrollPane(panelContenido);
        scrollPane.setBorder(null);
        panelFormulario.add(scrollPane, BorderLayout.CENTER);

        return panelFormulario;
    }

    /**
     * Crea el panel de búsqueda de visitante.
     */
    private JPanel crearPanelBusquedaVisitante() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Visitante"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DNI
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblDni = new JLabel("DNI:*");
        panel.add(lblDni, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtDniVisitante = new JTextField(15);
        panel.add(txtDniVisitante, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        btnBuscarVisitante = new JButton("Buscar");
        btnBuscarVisitante.addActionListener(e -> buscarVisitante());
        panel.add(btnBuscarVisitante, gbc);

        // Visitante seleccionado
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        lblVisitanteSeleccionado = new JLabel("Ningún visitante seleccionado");
        lblVisitanteSeleccionado.setFont(new Font("Arial", Font.ITALIC, 11));
        lblVisitanteSeleccionado.setForeground(Color.GRAY);
        panel.add(lblVisitanteSeleccionado, gbc);

        return panel;
    }

    /**
     * Crea el panel de datos de la restricción.
     */
    private JPanel crearPanelDatosRestriccion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos de Restricción"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Tipo de restricción
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Tipo:*"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbTipoRestriccion = new JComboBox<>(TipoRestriccion.values());
        panel.add(cmbTipoRestriccion, gbc);
        fila++;

        // Motivo
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(new JLabel("Motivo:* (mín 10 caracteres)"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtMotivo = new JTextArea(5, 20);
        txtMotivo.setLineWrap(true);
        txtMotivo.setWrapStyleWord(true);
        JScrollPane scrollMotivo = new JScrollPane(txtMotivo);
        panel.add(scrollMotivo, gbc);
        fila++;

        // Fecha inicio
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Fecha Inicio:*"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        datePickerFechaInicio = new JDatePicker(new Date());
        panel.add(datePickerFechaInicio, gbc);
        fila++;

        // Fecha fin
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Fecha Fin: (opcional)"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        datePickerFechaFin = new JDatePicker(null);
        panel.add(datePickerFechaFin, gbc);
        fila++;

        // Checkbox indefinida
        gbc.gridx = 1;
        gbc.gridy = fila;
        chkIndefinida = new JCheckBox("Restricción indefinida (sin fecha de fin)");
        chkIndefinida.addItemListener(e -> {
            datePickerFechaFin.setEnabled(!chkIndefinida.isSelected());
            if (chkIndefinida.isSelected()) {
                datePickerFechaFin.setFecha(null);
            }
        });
        panel.add(chkIndefinida, gbc);

        return panel;
    }

    /**
     * Crea el panel de alcance de la restricción.
     */
    private JPanel crearPanelAlcance() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Alcance de la Restricción"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        groupAlcance = new ButtonGroup();

        // Radio: Todos los internos
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        rbTodos = new JRadioButton("Todos los internos (visitante bloqueado totalmente)");
        rbTodos.setSelected(true);
        rbTodos.addActionListener(e -> actualizarEstadoPanelInterno());
        groupAlcance.add(rbTodos);
        panel.add(rbTodos, gbc);

        // Radio: Interno específico
        gbc.gridy = 1;
        rbInternoEspecifico = new JRadioButton("Interno específico solamente");
        rbInternoEspecifico.addActionListener(e -> actualizarEstadoPanelInterno());
        groupAlcance.add(rbInternoEspecifico);
        panel.add(rbInternoEspecifico, gbc);

        // Panel de búsqueda de interno (inicialmente deshabilitado)
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panelInternoEspecifico = crearPanelBusquedaInterno();
        panel.add(panelInternoEspecifico, gbc);

        actualizarEstadoPanelInterno();

        return panel;
    }

    /**
     * Crea el panel de búsqueda de interno.
     */
    private JPanel crearPanelBusquedaInterno() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Legajo
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Legajo:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtLegajoInterno = new JTextField(15);
        panel.add(txtLegajoInterno, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        btnBuscarInterno = new JButton("Buscar");
        btnBuscarInterno.addActionListener(e -> buscarInterno());
        panel.add(btnBuscarInterno, gbc);

        // Interno seleccionado
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        lblInternoSeleccionado = new JLabel("Ningún interno seleccionado");
        lblInternoSeleccionado.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInternoSeleccionado.setForeground(Color.GRAY);
        panel.add(lblInternoSeleccionado, gbc);

        return panel;
    }

    /**
     * Actualiza el estado del panel de interno según el alcance seleccionado.
     */
    private void actualizarEstadoPanelInterno() {
        boolean habilitar = rbInternoEspecifico.isSelected();
        txtLegajoInterno.setEnabled(habilitar);
        btnBuscarInterno.setEnabled(habilitar);
        lblInternoSeleccionado.setEnabled(habilitar);

        if (!habilitar) {
            txtLegajoInterno.setText("");
            lblInternoSeleccionado.setText("Ningún interno seleccionado");
            lblInternoSeleccionado.setForeground(Color.GRAY);
            internoSeleccionado = null;
        }
    }

    /**
     * Crea el panel de botones del formulario.
     */
    private JPanel crearPanelBotonesFormulario() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnCrearRestriccion = new JButton("Crear Restricción");
        btnCrearRestriccion.setFont(new Font("Arial", Font.BOLD, 12));
        btnCrearRestriccion.setBackground(Color.WHITE);
        btnCrearRestriccion.setForeground(Color.BLACK);
        btnCrearRestriccion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        btnCrearRestriccion.addActionListener(e -> crearRestriccion());

        btnLimpiarFormulario = new JButton("Limpiar Formulario");
        btnLimpiarFormulario.setBackground(Color.WHITE);
        btnLimpiarFormulario.setForeground(Color.BLACK);
        btnLimpiarFormulario.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        btnLimpiarFormulario.addActionListener(e -> limpiarFormulario());

        panel.add(btnCrearRestriccion);
        panel.add(btnLimpiarFormulario);

        return panel;
    }

    /**
     * Crea el panel de listado de restricciones.
     */
    private JPanel crearPanelListado() {
        JPanel panelListado = new JPanel(new BorderLayout(10, 10));
        panelListado.setBorder(BorderFactory.createTitledBorder("Listado de Restricciones"));

        // Panel de filtros
        JPanel panelFiltros = crearPanelFiltros();
        panelListado.add(panelFiltros, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"ID", "DNI", "Visitante", "Tipo", "Alcance", "Desde", "Hasta", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblRestricciones = new JTable(modeloTabla);
        tblRestricciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblRestricciones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonesAccion();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tblRestricciones);
        panelListado.add(scrollTabla, BorderLayout.CENTER);

        // Panel de acciones
        JPanel panelAcciones = crearPanelAcciones();
        panelListado.add(panelAcciones, BorderLayout.SOUTH);

        return panelListado;
    }

    /**
     * Crea el panel de filtros.
     */
    private JPanel crearPanelFiltros() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(new JLabel("Estado:"));
        cmbFiltroEstado = new JComboBox<>(new String[]{"Todas", "Activas", "Inactivas", "Próximas a vencer (7 días)"});
        panel.add(cmbFiltroEstado);

        panel.add(new JLabel("Tipo:"));
        cmbFiltroTipo = new JComboBox<>(new String[]{"Todas", "CONDUCTA", "JUDICIAL", "ADMINISTRATIVA", "SEGURIDAD"});
        panel.add(cmbFiltroTipo);

        btnAplicarFiltros = new JButton("Aplicar Filtros");
        btnAplicarFiltros.addActionListener(e -> cargarRestricciones());
        panel.add(btnAplicarFiltros);

        return panel;
    }

    /**
     * Crea el panel de acciones y estadísticas.
     */
    private JPanel crearPanelAcciones() {
        JPanel panel = new JPanel(new BorderLayout());

        // Botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnLevantar = new JButton("Levantar");
        btnLevantar.setEnabled(false);
        btnLevantar.addActionListener(e -> levantarRestriccion());
        panelBotones.add(btnLevantar);

        btnExtender = new JButton("Extender");
        btnExtender.setEnabled(false);
        btnExtender.addActionListener(e -> extenderRestriccion());
        panelBotones.add(btnExtender);

        btnEliminar = new JButton("Eliminar");
        btnEliminar.setEnabled(false);
        btnEliminar.addActionListener(e -> eliminarRestriccion());
        // Solo ADMINISTRADOR puede eliminar
        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            btnEliminar.setVisible(false);
        }
        panelBotones.add(btnEliminar);

        panel.add(panelBotones, BorderLayout.WEST);

        // Estadísticas
        JPanel panelEstadisticas = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("Estadísticas"));

        lblTotalActivas = new JLabel("Total activas: -");
        lblProximasVencer = new JLabel("Próximas a vencer (7 días): -");

        panelEstadisticas.add(lblTotalActivas);
        panelEstadisticas.add(Box.createHorizontalStrut(20));
        panelEstadisticas.add(lblProximasVencer);

        panel.add(panelEstadisticas, BorderLayout.EAST);

        return panel;
    }

    // ===== MÉTODOS DE LÓGICA DE NEGOCIO =====

    /**
     * Busca un visitante por DNI.
     */
    private void buscarVisitante() {
        String dni = txtDniVisitante.getText().trim();

        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar un DNI",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Visitante visitante = controlador.buscarVisitantePorDNI(dni);

            if (visitante == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún visitante con DNI: " + dni,
                    "Visitante No Encontrado",
                    JOptionPane.WARNING_MESSAGE);
                lblVisitanteSeleccionado.setText("Ningún visitante seleccionado");
                lblVisitanteSeleccionado.setForeground(Color.GRAY);
                visitanteSeleccionado = null;
                return;
            }

            // Visitante encontrado
            visitanteSeleccionado = visitante;
            lblVisitanteSeleccionado.setText("Seleccionado: " + visitante.getNombreCompleto() + " (DNI: " + visitante.getDni() + ")");
            lblVisitanteSeleccionado.setForeground(Color.BLACK);
            lblVisitanteSeleccionado.setFont(new Font("Arial", Font.BOLD, 11));

        } catch (SQLException ex) {
            mostrarError("Error al buscar visitante", ex);
        }
    }

    /**
     * Busca un interno por legajo.
     */
    private void buscarInterno() {
        String legajo = txtLegajoInterno.getText().trim();

        if (legajo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar un número de legajo",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Interno interno = controlador.buscarInternoPorLegajo(legajo);

            if (interno == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún interno con legajo: " + legajo,
                    "Interno No Encontrado",
                    JOptionPane.WARNING_MESSAGE);
                lblInternoSeleccionado.setText("Ningún interno seleccionado");
                lblInternoSeleccionado.setForeground(Color.GRAY);
                internoSeleccionado = null;
                return;
            }

            // Interno encontrado
            internoSeleccionado = interno;
            lblInternoSeleccionado.setText("Seleccionado: " + interno.getNombreCompleto() +
                                           " (Legajo: " + interno.getNumeroLegajo() + ")");
            lblInternoSeleccionado.setForeground(Color.BLACK);
            lblInternoSeleccionado.setFont(new Font("Arial", Font.BOLD, 11));

        } catch (SQLException ex) {
            mostrarError("Error al buscar interno", ex);
        }
    }

    /**
     * Crea una nueva restricción.
     */
    private void crearRestriccion() {
        try {
            // Validar visitante
            if (visitanteSeleccionado == null) {
                JOptionPane.showMessageDialog(this,
                    "Debe buscar y seleccionar un visitante",
                    "Datos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validar tipo
            TipoRestriccion tipo = (TipoRestriccion) cmbTipoRestriccion.getSelectedItem();
            if (tipo == null) {
                JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un tipo de restricción",
                    "Datos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Validar motivo
            String motivo = txtMotivo.getText().trim();
            if (motivo.length() < 10) {
                JOptionPane.showMessageDialog(this,
                    "El motivo debe tener al menos 10 caracteres",
                    "Datos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
                txtMotivo.requestFocus();
                return;
            }

            // Validar fecha inicio
            Date fechaInicio = datePickerFechaInicio.getFecha();
            if (fechaInicio == null) {
                JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una fecha de inicio",
                    "Datos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Fecha fin
            Date fechaFin = chkIndefinida.isSelected() ? null : datePickerFechaFin.getFecha();

            // Validar alcance
            AplicableA alcance = rbTodos.isSelected() ? AplicableA.TODOS : AplicableA.INTERNO_ESPECIFICO;
            Long idInterno = null;

            if (alcance == AplicableA.INTERNO_ESPECIFICO) {
                if (internoSeleccionado == null) {
                    JOptionPane.showMessageDialog(this,
                        "Debe buscar y seleccionar un interno para restricción específica",
                        "Datos Incompletos",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                idInterno = internoSeleccionado.getIdInterno();
            }

            // Crear restricción
            Long id = controlador.crearRestriccion(
                visitanteSeleccionado.getIdVisitante(),
                tipo,
                motivo,
                fechaInicio,
                fechaFin,
                alcance,
                idInterno
            );

            JOptionPane.showMessageDialog(this,
                "Restricción creada exitosamente.\n\nID: " + id +
                "\nVisitante: " + visitanteSeleccionado.getNombreCompleto() +
                "\nTipo: " + tipo +
                "\nAlcance: " + (alcance == AplicableA.TODOS ? "Todos los internos" : "Interno específico"),
                "Restricción Creada",
                JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarRestricciones();
            actualizarEstadisticas();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            mostrarError("Error al crear restricción", ex);
        }
    }

    /**
     * Levanta una restricción seleccionada.
     */
    private void levantarRestriccion() {
        int filaSeleccionada = tblRestricciones.getSelectedRow();
        if (filaSeleccionada == -1) {
            return;
        }

        Long idRestriccion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        String motivo = JOptionPane.showInputDialog(this,
            "Ingrese el motivo del levantamiento de la restricción:\n(mínimo 10 caracteres)",
            "Levantar Restricción",
            JOptionPane.QUESTION_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            return; // Usuario canceló
        }

        if (motivo.trim().length() < 10) {
            JOptionPane.showMessageDialog(this,
                "El motivo debe tener al menos 10 caracteres",
                "Motivo Insuficiente",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            boolean exito = controlador.levantarRestriccion(idRestriccion, motivo.trim());

            if (exito) {
                JOptionPane.showMessageDialog(this,
                    "Restricción levantada exitosamente",
                    "Operación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

                cargarRestricciones();
                actualizarEstadisticas();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo levantar la restricción",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            mostrarError("Error al levantar restricción", ex);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error de Validación",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Extiende una restricción seleccionada.
     */
    private void extenderRestriccion() {
        int filaSeleccionada = tblRestricciones.getSelectedRow();
        if (filaSeleccionada == -1) {
            return;
        }

        Long idRestriccion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        // Dialog personalizado con JDatePicker
        JDialog dialog = new JDialog(this, "Extender Restricción", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new FlowLayout());
        panelFormulario.add(new JLabel("Nueva fecha de finalización:"));
        JDatePicker datePicker = new JDatePicker(null);
        panelFormulario.add(datePicker);

        JPanel panelBotones = new JPanel(new FlowLayout());
        JButton btnConfirmar = new JButton("Confirmar");
        JButton btnCancelar = new JButton("Cancelar");

        btnConfirmar.addActionListener(e -> {
            Date nuevaFecha = datePicker.getFecha();
            if (nuevaFecha != null) {
                try {
                    boolean exito = controlador.extenderRestriccion(idRestriccion, nuevaFecha);

                    if (exito) {
                        JOptionPane.showMessageDialog(this,
                            "Restricción extendida exitosamente hasta: " + DATE_FORMAT.format(nuevaFecha),
                            "Operación Exitosa",
                            JOptionPane.INFORMATION_MESSAGE);

                        cargarRestricciones();
                        actualizarEstadisticas();
                        dialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "No se pudo extender la restricción",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }

                } catch (SQLException ex) {
                    mostrarError("Error al extender restricción", ex);
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this,
                        ex.getMessage(),
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnCancelar.addActionListener(e -> dialog.dispose());

        panelBotones.add(btnConfirmar);
        panelBotones.add(btnCancelar);

        dialog.add(panelFormulario, BorderLayout.CENTER);
        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Elimina una restricción seleccionada (solo ADMINISTRADOR).
     */
    private void eliminarRestriccion() {
        int filaSeleccionada = tblRestricciones.getSelectedRow();
        if (filaSeleccionada == -1) {
            return;
        }

        Long idRestriccion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro que desea eliminar esta restricción?\n\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            boolean exito = controlador.eliminarRestriccion(idRestriccion);

            if (exito) {
                JOptionPane.showMessageDialog(this,
                    "Restricción eliminada exitosamente",
                    "Operación Exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

                cargarRestricciones();
                actualizarEstadisticas();
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se pudo eliminar la restricción",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            mostrarError("Error al eliminar restricción", ex);
        }
    }

    /**
     * Carga las restricciones en la tabla según los filtros aplicados.
     */
    private void cargarRestricciones() {
        try {
            modeloTabla.setRowCount(0);

            String filtroEstado = (String) cmbFiltroEstado.getSelectedItem();
            List<Restriccion> restricciones;

            // Aplicar filtro de estado
            if ("Activas".equals(filtroEstado)) {
                restricciones = controlador.listarRestriccionesActivas();
            } else if ("Próximas a vencer (7 días)".equals(filtroEstado)) {
                restricciones = controlador.obtenerProximasAVencer(7);
            } else {
                // Todas
                restricciones = controlador.listarTodasRestricciones();
            }

            // Aplicar filtro de tipo si no es "Todas"
            String filtroTipo = (String) cmbFiltroTipo.getSelectedItem();
            if (!"Todas".equals(filtroTipo)) {
                TipoRestriccion tipo = TipoRestriccion.valueOf(filtroTipo);
                restricciones.removeIf(r -> r.getTipoRestriccion() != tipo);
            }

            // Filtrar inactivas si filtro es "Activas"
            if ("Inactivas".equals(filtroEstado)) {
                restricciones.removeIf(r -> r.isActivo());
            }

            // Agregar a la tabla
            for (Restriccion r : restricciones) {
                String dni = r.getVisitante() != null ? r.getVisitante().getDni() : "N/A";
                String nombreVisitante = r.getVisitante() != null ? r.getVisitante().getNombreCompleto() : "N/A";
                String tipo = r.getTipoRestriccion() != null ? r.getTipoRestriccion().name() : "N/A";
                String alcance = r.getAplicableA() != null ? r.getAplicableA().name() : "N/A";
                String desde = r.getFechaInicio() != null ? DATE_FORMAT.format(r.getFechaInicio()) : "N/A";
                String hasta = r.getFechaFin() != null ? DATE_FORMAT.format(r.getFechaFin()) : "Indefinida";
                String estado = r.estaActiva() ? "ACTIVA" : "INACTIVA";

                modeloTabla.addRow(new Object[]{
                    r.getIdRestriccion(),
                    dni,
                    nombreVisitante,
                    tipo,
                    alcance,
                    desde,
                    hasta,
                    estado
                });
            }

        } catch (SQLException ex) {
            mostrarError("Error al cargar restricciones", ex);
        }
    }

    /**
     * Actualiza las estadísticas mostradas.
     */
    private void actualizarEstadisticas() {
        try {
            int totalActivas = controlador.contarRestriccionesActivas();
            int proximasVencer = controlador.obtenerProximasAVencer(7).size();

            lblTotalActivas.setText("Total activas: " + totalActivas);

            // Formato con énfasis si hay próximas a vencer
            if (proximasVencer > 0) {
                lblProximasVencer.setText("ALERTA - Proximas a vencer (7 dias): " + proximasVencer);
                lblProximasVencer.setFont(new Font("Arial", Font.BOLD, 12));
            } else {
                lblProximasVencer.setText("Proximas a vencer (7 dias): " + proximasVencer);
                lblProximasVencer.setFont(new Font("Arial", Font.PLAIN, 12));
            }
            lblProximasVencer.setForeground(Color.BLACK);

        } catch (SQLException ex) {
            lblTotalActivas.setText("Total activas: Error");
            lblProximasVencer.setText("Próximas a vencer: Error");
        }
    }

    /**
     * Actualiza el estado de los botones de acción según la selección.
     */
    private void actualizarBotonesAccion() {
        int filaSeleccionada = tblRestricciones.getSelectedRow();
        boolean haySeleccion = filaSeleccionada != -1;

        btnLevantar.setEnabled(haySeleccion);
        btnExtender.setEnabled(haySeleccion);
        btnEliminar.setEnabled(haySeleccion && usuarioActual.getRol() == Rol.ADMINISTRADOR);
    }

    /**
     * Limpia el formulario de restricción.
     */
    private void limpiarFormulario() {
        txtDniVisitante.setText("");
        lblVisitanteSeleccionado.setText("Ningún visitante seleccionado");
        lblVisitanteSeleccionado.setForeground(Color.GRAY);
        lblVisitanteSeleccionado.setFont(new Font("Arial", Font.ITALIC, 11));
        visitanteSeleccionado = null;

        cmbTipoRestriccion.setSelectedIndex(0);
        txtMotivo.setText("");
        datePickerFechaInicio.setFecha(new Date());
        datePickerFechaFin.setFecha(null);
        chkIndefinida.setSelected(false);

        rbTodos.setSelected(true);
        txtLegajoInterno.setText("");
        lblInternoSeleccionado.setText("Ningún interno seleccionado");
        lblInternoSeleccionado.setForeground(Color.GRAY);
        lblInternoSeleccionado.setFont(new Font("Arial", Font.ITALIC, 11));
        internoSeleccionado = null;

        actualizarEstadoPanelInterno();
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje, Exception ex) {
        JOptionPane.showMessageDialog(this,
            mensaje + ":\n" + ex.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }

    /**
     * Crea el banner de advertencia para modo offline.
     */
    private JPanel crearBannerModoOffline() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        panel.setBackground(new java.awt.Color(255, 152, 0));

        JLabel lblAdvertencia = new JLabel(
            "⚠ MODO OFFLINE - Los datos se almacenan solo en memoria y se perderán al cerrar la aplicación"
        );
        lblAdvertencia.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        lblAdvertencia.setForeground(java.awt.Color.WHITE);

        panel.add(lblAdvertencia);
        return panel;
    }
}
