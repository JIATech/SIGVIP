package com.sigvip.vista;

import com.sigvip.controlador.ControladorInternos;
import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.EstadoInterno;
import com.sigvip.modelo.enums.SituacionProcesal;
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
 * Vista para gestión de internos.
 * Implementa RF006: Gestionar Internos.
 *
 * Funcionalidades:
 * - Registrar nuevos internos
 * - Buscar internos por legajo o DNI
 * - Actualizar datos e información de ubicación
 * - Gestionar estados (ACTIVO, TRASLADADO, EGRESADO)
 * - Listar internos con filtros
 *
 * Especificación: PDF Sección 7.1 (RF006)
 */
public class VistaGestionInternos extends JFrame {

    private final Usuario usuarioActual;
    private final ControladorInternos controlador;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    // Componentes del formulario
    private JTextField txtLegajo;
    private JTextField txtApellido;
    private JTextField txtNombre;
    private JTextField txtDNI;
    private JDatePicker datePickerFechaNacimiento;
    private JDatePicker datePickerFechaIngreso;
    private JComboBox<SituacionProcesal> cmbSituacionProcesal;
    private JTextField txtPabellon;
    private JTextField txtPiso;
    private JComboBox<Establecimiento> cmbEstablecimiento;
    private JTextArea txtObservaciones;
    private JButton btnRegistrar;
    private JButton btnLimpiar;

    // Componentes de búsqueda
    private JTextField txtBuscarLegajo;
    private JButton btnBuscarLegajo;
    private JTextField txtBuscarDNI;
    private JButton btnBuscarDNI;

    // Componentes de listado
    private JTable tblInternos;
    private DefaultTableModel modeloTabla;
    private JComboBox<EstadoInterno> cmbFiltroEstado;
    private JComboBox<SituacionProcesal> cmbFiltroSituacion;
    private JButton btnActualizarListado;
    private JButton btnActualizarUbicacion;
    private JButton btnCambiarEstado;
    private JButton btnRegistrarTraslado;

    /**
     * Constructor de la vista.
     *
     * @param usuario usuario autenticado
     */
    public VistaGestionInternos(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorInternos(usuario);

        inicializarComponentes();
        configurarVentana();
        cargarInternos();
    }

    /**
     * Configura la ventana principal.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Gestión de Internos (RF006)");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: Título
        JPanel panelTitulo = crearPanelTitulo();
        panelPrincipal.add(panelTitulo, BorderLayout.NORTH);

        // Panel central: Dividido en formulario y listado
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setLeftComponent(crearPanelFormularioYBusqueda());
        splitPane.setRightComponent(crearPanelListado());
        panelPrincipal.add(splitPane, BorderLayout.CENTER);

        add(panelPrincipal);
    }

    /**
     * Crea el panel de título.
     */
    private JPanel crearPanelTitulo() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setBackground(TemaColors.FONDO_ENCABEZADO);

        JLabel lblTitulo = new JLabel("GESTIÓN DE INTERNOS", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(TemaColors.TEXTO_CLARO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel combinado de formulario y búsqueda.
     */
    private JPanel crearPanelFormularioYBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel de búsqueda arriba
        JPanel panelBusqueda = crearPanelBusqueda();
        panel.add(panelBusqueda, BorderLayout.NORTH);

        // Panel de formulario en el centro
        JPanel panelFormulario = crearPanelFormulario();
        panel.add(panelFormulario, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de búsqueda.
     */
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Buscar Interno"));

        // Búsqueda por legajo
        JPanel panelBusquedaLegajo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusquedaLegajo.add(new JLabel("Legajo:"));
        txtBuscarLegajo = new JTextField(15);
        panelBusquedaLegajo.add(txtBuscarLegajo);
        btnBuscarLegajo = new JButton("Buscar");
        btnBuscarLegajo.addActionListener(e -> buscarPorLegajo());
        panelBusquedaLegajo.add(btnBuscarLegajo);

        // Búsqueda por DNI
        JPanel panelBusquedaDNI = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusquedaDNI.add(new JLabel("DNI:"));
        txtBuscarDNI = new JTextField(15);
        panelBusquedaDNI.add(txtBuscarDNI);
        btnBuscarDNI = new JButton("Buscar");
        btnBuscarDNI.addActionListener(e -> buscarPorDNI());
        panelBusquedaDNI.add(btnBuscarDNI);

        panel.add(panelBusquedaLegajo);
        panel.add(panelBusquedaDNI);

        return panel;
    }

    /**
     * Crea el panel del formulario de registro.
     */
    private JPanel crearPanelFormulario() {
        JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Interno"));

        // Panel de campos
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int fila = 0;

        // Legajo
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Legajo: *"), gbc);
        gbc.gridx = 1;
        txtLegajo = new JTextField(20);
        panelCampos.add(txtLegajo, gbc);

        // Apellido
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Apellido: *"), gbc);
        gbc.gridx = 1;
        txtApellido = new JTextField(20);
        panelCampos.add(txtApellido, gbc);

        // Nombre
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Nombre: *"), gbc);
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        panelCampos.add(txtNombre, gbc);

        // DNI
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("DNI: *"), gbc);
        gbc.gridx = 1;
        txtDNI = new JTextField(20);
        panelCampos.add(txtDNI, gbc);

        // Fecha de Nacimiento
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Fecha Nacimiento: *"), gbc);
        gbc.gridx = 1;
        datePickerFechaNacimiento = new JDatePicker();
        panelCampos.add(datePickerFechaNacimiento, gbc);

        // Fecha de Ingreso
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Fecha Ingreso: *"), gbc);
        gbc.gridx = 1;
        datePickerFechaIngreso = new JDatePicker();
        panelCampos.add(datePickerFechaIngreso, gbc);

        // Situación Procesal
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Situación Procesal: *"), gbc);
        gbc.gridx = 1;
        cmbSituacionProcesal = new JComboBox<>(SituacionProcesal.values());
        panelCampos.add(cmbSituacionProcesal, gbc);

        // Pabellón
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Pabellón:"), gbc);
        gbc.gridx = 1;
        txtPabellon = new JTextField(20);
        panelCampos.add(txtPabellon, gbc);

        // Piso
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Piso:"), gbc);
        gbc.gridx = 1;
        txtPiso = new JTextField(20);
        panelCampos.add(txtPiso, gbc);

        // Establecimiento
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        panelCampos.add(new JLabel("Establecimiento:"), gbc);
        gbc.gridx = 1;
        cmbEstablecimiento = new JComboBox<>();
        cargarEstablecimientos();
        panelCampos.add(cmbEstablecimiento, gbc);

        // Observaciones
        fila++;
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.anchor = GridBagConstraints.NORTH;
        panelCampos.add(new JLabel("Observaciones:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtObservaciones = new JTextArea(3, 20);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        JScrollPane scrollObservaciones = new JScrollPane(txtObservaciones);
        panelCampos.add(scrollObservaciones, gbc);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        panelBotones.add(btnLimpiar);

        btnRegistrar = new JButton("Registrar Interno");
        TemaColors.aplicarEstiloBotonAccion(btnRegistrar);
        btnRegistrar.addActionListener(e -> registrarInterno());
        panelBotones.add(btnRegistrar);

        panelFormulario.add(new JScrollPane(panelCampos), BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        return panelFormulario;
    }

    /**
     * Crea el panel de listado de internos.
     */
    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Listado de Internos"));

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panelFiltros.add(new JLabel("Estado:"));
        cmbFiltroEstado = new JComboBox<>();
        cmbFiltroEstado.addItem(null); // "Todos"
        for (EstadoInterno estado : EstadoInterno.values()) {
            cmbFiltroEstado.addItem(estado);
        }
        cmbFiltroEstado.addActionListener(e -> cargarInternos());
        panelFiltros.add(cmbFiltroEstado);

        panelFiltros.add(new JLabel("Situación:"));
        cmbFiltroSituacion = new JComboBox<>();
        cmbFiltroSituacion.addItem(null); // "Todos"
        for (SituacionProcesal situacion : SituacionProcesal.values()) {
            cmbFiltroSituacion.addItem(situacion);
        }
        cmbFiltroSituacion.addActionListener(e -> cargarInternos());
        panelFiltros.add(cmbFiltroSituacion);

        btnActualizarListado = new JButton("Actualizar");
        btnActualizarListado.addActionListener(e -> cargarInternos());
        panelFiltros.add(btnActualizarListado);

        // Tabla de internos
        String[] columnas = {"ID", "Legajo", "Apellido, Nombre", "DNI", "Situación",
                           "Pabellón", "Piso", "Fecha Ingreso", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblInternos = new JTable(modeloTabla);
        tblInternos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblInternos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblInternos.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scrollTabla = new JScrollPane(tblInternos);

        // Panel de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnActualizarUbicacion = new JButton("Actualizar Ubicación");
        TemaColors.aplicarEstiloBoton(btnActualizarUbicacion, TemaColors.BOTON_INFO);
        btnActualizarUbicacion.addActionListener(e -> actualizarUbicacion());
        panelAcciones.add(btnActualizarUbicacion);

        btnCambiarEstado = new JButton("Cambiar Estado");
        TemaColors.aplicarEstiloBoton(btnCambiarEstado, TemaColors.ACENTO_INTERNOS);
        btnCambiarEstado.addActionListener(e -> cambiarEstado());
        panelAcciones.add(btnCambiarEstado);

        btnRegistrarTraslado = new JButton("Registrar Traslado");
        TemaColors.aplicarEstiloBoton(btnRegistrarTraslado, TemaColors.BOTON_EDICION);
        btnRegistrarTraslado.addActionListener(e -> registrarTraslado());
        panelAcciones.add(btnRegistrarTraslado);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Carga los establecimientos en el combo box.
     */
    private void cargarEstablecimientos() {
        try {
            List<Establecimiento> establecimientos = controlador.listarEstablecimientos();

            cmbEstablecimiento.removeAllItems();
            cmbEstablecimiento.addItem(null); // Opción vacía

            for (Establecimiento est : establecimientos) {
                cmbEstablecimiento.addItem(est);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar establecimientos: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Registra un nuevo interno.
     */
    private void registrarInterno() {
        // Obtener datos del formulario
        String legajo = txtLegajo.getText().trim();
        String apellido = txtApellido.getText().trim();
        String nombre = txtNombre.getText().trim();
        String dni = txtDNI.getText().trim();
        Date fechaNac = datePickerFechaNacimiento.getFecha();
        Date fechaIng = datePickerFechaIngreso.getFecha();
        SituacionProcesal situacion = (SituacionProcesal) cmbSituacionProcesal.getSelectedItem();
        String pabellon = txtPabellon.getText().trim();
        String piso = txtPiso.getText().trim();
        Establecimiento establecimiento = (Establecimiento) cmbEstablecimiento.getSelectedItem();
        String observaciones = txtObservaciones.getText().trim();

        // Validar campos obligatorios
        if (legajo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El número de legajo es obligatorio",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            txtLegajo.requestFocus();
            return;
        }

        if (apellido.isEmpty() || nombre.isEmpty() || dni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Apellido, nombre y DNI son obligatorios",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener fechas de los JDatePickers
        Date fechaNacimiento = datePickerFechaNacimiento.getFecha();
        Date fechaIngreso = datePickerFechaIngreso.getFecha();

        // Confirmar registro
        int opcion = JOptionPane.showConfirmDialog(this,
            String.format(
                "¿Confirma el registro de este interno?\n\n" +
                "Legajo: %s\n" +
                "Nombre: %s, %s\n" +
                "DNI: %s\n" +
                "Situación: %s\n",
                legajo, apellido, nombre, dni, situacion),
            "Confirmar Registro",
            JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        // Registrar interno
        try {
            Interno nuevoInterno = controlador.registrarInterno(
                legajo,
                apellido,
                nombre,
                dni,
                fechaNacimiento,
                fechaIngreso,
                situacion,
                pabellon.isEmpty() ? null : pabellon,
                piso.isEmpty() ? null : piso,
                establecimiento != null ? establecimiento.getIdEstablecimiento() : null,
                observaciones.isEmpty() ? null : observaciones
            );

            JOptionPane.showMessageDialog(this,
                "Interno registrado exitosamente\n" +
                "ID: " + nuevoInterno.getIdInterno(),
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarInternos();

        } catch (IllegalStateException ex) {
            // Legajo duplicado
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Legajo Duplicado",
                JOptionPane.WARNING_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al registrar interno: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Datos Inválidos",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Limpia el formulario.
     */
    private void limpiarFormulario() {
        txtLegajo.setText("");
        txtApellido.setText("");
        txtNombre.setText("");
        txtDNI.setText("");
        datePickerFechaNacimiento.limpiar();
        datePickerFechaIngreso.limpiar();
        cmbSituacionProcesal.setSelectedIndex(0);
        txtPabellon.setText("");
        txtPiso.setText("");
        cmbEstablecimiento.setSelectedIndex(0);
        txtObservaciones.setText("");
    }

    /**
     * Busca un interno por legajo.
     */
    private void buscarPorLegajo() {
        String legajo = txtBuscarLegajo.getText().trim();

        if (legajo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar un legajo para buscar",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Interno interno = controlador.buscarPorLegajo(legajo);

            if (interno == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún interno con legajo: " + legajo,
                    "Interno no encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                mostrarDetallesInterno(interno);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar interno: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Busca un interno por DNI.
     */
    private void buscarPorDNI() {
        String dni = txtBuscarDNI.getText().trim();

        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar un DNI para buscar",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Interno interno = controlador.buscarPorDNI(dni);

            if (interno == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún interno con DNI: " + dni,
                    "Interno no encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                mostrarDetallesInterno(interno);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar interno: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Muestra los detalles de un interno en un diálogo.
     */
    private void mostrarDetallesInterno(Interno interno) {
        String detalles = String.format(
            "INFORMACIÓN DEL INTERNO\n\n" +
            "ID: %d\n" +
            "Legajo: %s\n" +
            "Nombre: %s\n" +
            "DNI: %s\n" +
            "Fecha Ingreso: %s\n" +
            "Situación Procesal: %s\n" +
            "Ubicación: Pabellón %s, Piso %s\n" +
            "Estado: %s\n" +
            "Observaciones: %s",
            interno.getIdInterno(),
            interno.getNumeroLegajo(),
            interno.getNombreCompleto(),
            interno.getDni(),
            interno.getFechaIngreso() != null ?
                DATE_FORMAT.format(interno.getFechaIngreso()) : "N/A",
            interno.getSituacionProcesal(),
            interno.getPabellonActual() != null ? interno.getPabellonActual() : "N/A",
            interno.getPisoActual() > 0 ? String.valueOf(interno.getPisoActual()) : "N/A",
            interno.getEstado(),
            interno.getObservaciones() != null ? interno.getObservaciones() : "Sin observaciones"
        );

        JTextArea textArea = new JTextArea(detalles);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this,
            new JScrollPane(textArea),
            "Detalles del Interno",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Carga los internos en la tabla.
     */
    private void cargarInternos() {
        try {
            modeloTabla.setRowCount(0);

            List<Interno> internos;
            EstadoInterno estadoFiltro = (EstadoInterno) cmbFiltroEstado.getSelectedItem();
            SituacionProcesal situacionFiltro = (SituacionProcesal) cmbFiltroSituacion.getSelectedItem();

            // Aplicar filtros
            if (estadoFiltro != null) {
                internos = controlador.listarPorEstado(estadoFiltro);
            } else if (situacionFiltro != null) {
                internos = controlador.listarPorSituacion(situacionFiltro);
            } else {
                internos = controlador.listarTodos();
            }

            for (Interno interno : internos) {
                Object[] fila = {
                    interno.getIdInterno(),
                    interno.getNumeroLegajo(),
                    interno.getApellido() + ", " + interno.getNombre(),
                    interno.getDni(),
                    interno.getSituacionProcesal(),
                    interno.getPabellonActual() != null ? interno.getPabellonActual() : "N/A",
                    interno.getPisoActual() > 0 ? String.valueOf(interno.getPisoActual()) : "N/A",
                    interno.getFechaIngreso() != null ?
                        DATE_FORMAT.format(interno.getFechaIngreso()) : "N/A",
                    interno.getEstado()
                };
                modeloTabla.addRow(fila);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar internos: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Actualiza la ubicación del interno seleccionado.
     */
    private void actualizarUbicacion() {
        int filaSeleccionada = tblInternos.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar un interno de la tabla",
                "Ningún interno seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idInterno = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        String pabellon = JOptionPane.showInputDialog(this,
            "Ingrese el nuevo pabellón:",
            "Actualizar Ubicación",
            JOptionPane.QUESTION_MESSAGE);

        if (pabellon == null) {
            return;
        }

        String piso = JOptionPane.showInputDialog(this,
            "Ingrese el nuevo piso:",
            "Actualizar Ubicación",
            JOptionPane.QUESTION_MESSAGE);

        if (piso == null) {
            return;
        }

        try {
            controlador.actualizarUbicacion(idInterno,
                pabellon.trim().isEmpty() ? null : pabellon.trim(),
                piso.trim().isEmpty() ? null : piso.trim());

            JOptionPane.showMessageDialog(this,
                "Ubicación actualizada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarInternos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al actualizar ubicación: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Cambia el estado del interno seleccionado.
     */
    private void cambiarEstado() {
        int filaSeleccionada = tblInternos.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar un interno de la tabla",
                "Ningún interno seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idInterno = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        EstadoInterno nuevoEstado = (EstadoInterno) JOptionPane.showInputDialog(
            this,
            "Seleccione el nuevo estado:",
            "Cambiar Estado",
            JOptionPane.QUESTION_MESSAGE,
            null,
            EstadoInterno.values(),
            EstadoInterno.ACTIVO
        );

        if (nuevoEstado == null) {
            return;
        }

        String motivo = JOptionPane.showInputDialog(this,
            "Ingrese el motivo del cambio de estado:",
            "Cambiar Estado",
            JOptionPane.QUESTION_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            return;
        }

        try {
            controlador.cambiarEstado(idInterno, nuevoEstado, motivo.trim());

            JOptionPane.showMessageDialog(this,
                "Estado actualizado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarInternos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cambiar estado: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Registra el traslado del interno seleccionado.
     */
    private void registrarTraslado() {
        int filaSeleccionada = tblInternos.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar un interno de la tabla",
                "Ningún interno seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idInterno = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        try {
            List<Establecimiento> establecimientos = controlador.listarEstablecimientos();

            Establecimiento destino = (Establecimiento) JOptionPane.showInputDialog(
                this,
                "Seleccione el establecimiento destino:",
                "Registrar Traslado",
                JOptionPane.QUESTION_MESSAGE,
                null,
                establecimientos.toArray(),
                null
            );

            if (destino == null) {
                return;
            }

            String motivo = JOptionPane.showInputDialog(this,
                "Ingrese el motivo del traslado:",
                "Registrar Traslado",
                JOptionPane.QUESTION_MESSAGE);

            if (motivo == null || motivo.trim().isEmpty()) {
                return;
            }

            controlador.registrarTraslado(idInterno, destino.getIdEstablecimiento(), motivo.trim());

            JOptionPane.showMessageDialog(this,
                "Traslado registrado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarInternos();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al registrar traslado: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
}
