package com.sigvip.vista;

import com.sigvip.controlador.ControladorAutorizaciones;
import com.sigvip.modelo.Autorizacion;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.EstadoAutorizacion;
import com.sigvip.modelo.enums.TipoRelacion;
import com.sigvip.utilidades.TemaColors;
import com.sigvip.vista.componentes.JDatePicker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Vista para gestión de autorizaciones de visita.
 * Implementa RF002: Autorizar Visita.
 *
 * Funcionalidades:
 * - Buscar visitantes e internos
 * - Crear nuevas autorizaciones
 * - Listar autorizaciones existentes
 * - Gestionar estados (suspender, revocar, renovar, reactivar)
 *
 * Especificación: PDF Sección 7.1 (RF002), Sección 8.3
 */
public class VistaAutorizaciones extends JFrame {

    private final Usuario usuarioActual;
    private final ControladorAutorizaciones controlador;

    // Componentes de búsqueda de visitante
    private JTextField txtDniVisitante;
    private JButton btnBuscarVisitante;
    private JLabel lblVisitanteSeleccionado;
    private Visitante visitanteSeleccionado;

    // Componentes de búsqueda de interno
    private JTextField txtLegajoInterno;
    private JButton btnBuscarInterno;
    private JLabel lblInternoSeleccionado;
    private Interno internoSeleccionado;

    // Componentes de formulario de autorización
    private JComboBox<TipoRelacion> cmbTipoRelacion;
    private JDatePicker datePickerFechaVencimiento;
    private JTextArea txtObservaciones;
    private JButton btnCrearAutorizacion;
    private JButton btnLimpiarFormulario;

    // Componentes de listado
    private JTable tblAutorizaciones;
    private DefaultTableModel modeloTabla;
    private JComboBox<EstadoAutorizacion> cmbFiltroEstado;
    private JButton btnActualizarListado;
    private JButton btnSuspender;
    private JButton btnRevocar;
    private JButton btnReactivar;
    private JButton btnRenovar;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor de la vista.
     *
     * @param usuario usuario autenticado
     */
    public VistaAutorizaciones(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorAutorizaciones(usuario);

        inicializarComponentes();
        configurarVentana();
        cargarAutorizaciones();
    }

    /**
     * Configura la ventana principal.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Gestión de Autorizaciones (RF002)");
        setSize(1200, 800);
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
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(350);
        splitPane.setTopComponent(crearPanelFormulario());
        splitPane.setBottomComponent(crearPanelListado());
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

        JLabel lblTitulo = new JLabel("GESTIÓN DE AUTORIZACIONES DE VISITA", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(TemaColors.TEXTO_CLARO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(lblTitulo, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de formulario de nueva autorización.
     */
    private JPanel crearPanelFormulario() {
        JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Nueva Autorización"));

        // Panel de búsquedas
        JPanel panelBusquedas = new JPanel(new GridLayout(2, 1, 5, 5));
        panelBusquedas.add(crearPanelBusquedaVisitante());
        panelBusquedas.add(crearPanelBusquedaInterno());

        // Panel de datos de autorización
        JPanel panelDatos = crearPanelDatosAutorizacion();

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCrearAutorizacion = new JButton("Crear Autorización");
        TemaColors.aplicarEstiloBotonAccion(btnCrearAutorizacion);
        btnCrearAutorizacion.addActionListener(e -> crearAutorizacion());

        btnLimpiarFormulario = new JButton("Limpiar");
        btnLimpiarFormulario.addActionListener(e -> limpiarFormulario());

        panelBotones.add(btnLimpiarFormulario);
        panelBotones.add(btnCrearAutorizacion);

        panelFormulario.add(panelBusquedas, BorderLayout.NORTH);
        panelFormulario.add(panelDatos, BorderLayout.CENTER);
        panelFormulario.add(panelBotones, BorderLayout.SOUTH);

        return panelFormulario;
    }

    /**
     * Crea el panel de búsqueda de visitante.
     */
    private JPanel crearPanelBusquedaVisitante() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("1. Buscar Visitante"));

        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));

        // Panel de búsqueda rápida por DNI
        JPanel panelBusquedaRapida = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusquedaRapida.add(new JLabel("DNI (opcional):"));

        txtDniVisitante = new JTextField(15);
        txtDniVisitante.setToolTipText("Ingrese DNI si lo conoce, o use el botón de búsqueda avanzada");
        panelBusquedaRapida.add(txtDniVisitante);

        JButton btnBuscarPorDni = new JButton("Buscar por DNI");
        btnBuscarPorDni.addActionListener(e -> buscarVisitantePorDni());
        panelBusquedaRapida.add(btnBuscarPorDni);

        JButton btnBusquedaAvanzada = new JButton("Búsqueda Avanzada");
        btnBusquedaAvanzada.addActionListener(e -> buscarVisitanteAvanzada());
        panelBusquedaRapida.add(btnBusquedaAvanzada);

        panelBusqueda.add(panelBusquedaRapida, BorderLayout.NORTH);

        // Panel de información del visitante seleccionado
        lblVisitanteSeleccionado = new JLabel("Ningún visitante seleccionado");
        lblVisitanteSeleccionado.setFont(new Font("Arial", Font.ITALIC, 11));
        lblVisitanteSeleccionado.setForeground(TemaColors.TEXTO_SECUNDARIO);
        lblVisitanteSeleccionado.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        panelBusqueda.add(lblVisitanteSeleccionado, BorderLayout.CENTER);

        panel.add(panelBusqueda, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de búsqueda de interno.
     */
    private JPanel crearPanelBusquedaInterno() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("2. Buscar Interno"));

        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));

        // Panel de búsqueda rápida por legajo
        JPanel panelBusquedaRapida = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusquedaRapida.add(new JLabel("Legajo (opcional):"));

        txtLegajoInterno = new JTextField(15);
        txtLegajoInterno.setToolTipText("Ingrese legajo si lo conoce, o use el botón de búsqueda avanzada");
        panelBusquedaRapida.add(txtLegajoInterno);

        JButton btnBuscarPorLegajo = new JButton("Buscar por Legajo");
        btnBuscarPorLegajo.addActionListener(e -> buscarInternoPorLegajo());
        panelBusquedaRapida.add(btnBuscarPorLegajo);

        JButton btnBusquedaAvanzadaInterno = new JButton("Búsqueda Avanzada");
        btnBusquedaAvanzadaInterno.addActionListener(e -> buscarInternoAvanzado());
        panelBusquedaRapida.add(btnBusquedaAvanzadaInterno);

        panelBusqueda.add(panelBusquedaRapida, BorderLayout.NORTH);

        // Panel de información del interno seleccionado
        lblInternoSeleccionado = new JLabel("Ningún interno seleccionado");
        lblInternoSeleccionado.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInternoSeleccionado.setForeground(TemaColors.TEXTO_SECUNDARIO);
        lblInternoSeleccionado.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        panelBusqueda.add(lblInternoSeleccionado, BorderLayout.CENTER);

        panel.add(panelBusqueda, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de datos de la autorización.
     */
    private JPanel crearPanelDatosAutorizacion() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("3. Datos de Autorización"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tipo de relación
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Tipo de Relación: *"), gbc);

        gbc.gridx = 1;
        cmbTipoRelacion = new JComboBox<>(TipoRelacion.values());
        panel.add(cmbTipoRelacion, gbc);

        // Fecha de vencimiento
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Fecha Vencimiento:"), gbc);

        gbc.gridx = 1;
        datePickerFechaVencimiento = new JDatePicker();
        datePickerFechaVencimiento.setToolTipText("Use el botón 'Cal' para seleccionar fecha (opcional)");
        panel.add(datePickerFechaVencimiento, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(new JLabel("Observaciones:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        txtObservaciones = new JTextArea(3, 30);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        JScrollPane scrollObservaciones = new JScrollPane(txtObservaciones);
        panel.add(scrollObservaciones, gbc);

        return panel;
    }

    /**
     * Crea el panel de listado de autorizaciones.
     */
    private JPanel crearPanelListado() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Autorizaciones Existentes"));

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.add(new JLabel("Filtrar por estado:"));

        cmbFiltroEstado = new JComboBox<>();
        cmbFiltroEstado.addItem(null); // Opción "Todos"
        for (EstadoAutorizacion estado : EstadoAutorizacion.values()) {
            cmbFiltroEstado.addItem(estado);
        }
        cmbFiltroEstado.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                         int index, boolean isSelected,
                                                         boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Todos" : ((EstadoAutorizacion) value).getDescripcion());
                return this;
            }
        });
        cmbFiltroEstado.addActionListener(e -> cargarAutorizaciones());
        panelFiltros.add(cmbFiltroEstado);

        btnActualizarListado = new JButton("Actualizar");
        btnActualizarListado.addActionListener(e -> cargarAutorizaciones());
        panelFiltros.add(btnActualizarListado);

        // Tabla de autorizaciones
        String[] columnas = {"ID", "Visitante", "DNI", "Interno", "Legajo",
                           "Tipo Relación", "Fecha Autorizac.", "Fecha Venc.", "Estado", "Vigente"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }
        };

        tblAutorizaciones = new JTable(modeloTabla);
        tblAutorizaciones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblAutorizaciones.getColumnModel().getColumn(0).setPreferredWidth(50);
        tblAutorizaciones.getColumnModel().getColumn(1).setPreferredWidth(150);
        tblAutorizaciones.getColumnModel().getColumn(3).setPreferredWidth(150);

        JScrollPane scrollTabla = new JScrollPane(tblAutorizaciones);

        // Panel de botones de acciones
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT));

        btnSuspender = new JButton("Suspender");
        TemaColors.aplicarEstiloBoton(btnSuspender, TemaColors.BOTON_EDICION);
        btnSuspender.addActionListener(e -> suspenderAutorizacion());
        panelAcciones.add(btnSuspender);

        btnReactivar = new JButton("Reactivar");
        TemaColors.aplicarEstiloBotonAccion(btnReactivar);
        btnReactivar.addActionListener(e -> reactivarAutorizacion());
        panelAcciones.add(btnReactivar);

        btnRenovar = new JButton("Renovar");
        TemaColors.aplicarEstiloBoton(btnRenovar, TemaColors.ACENTO_AUTORIZACIONES);
        btnRenovar.addActionListener(e -> renovarAutorizacion());
        panelAcciones.add(btnRenovar);

        btnRevocar = new JButton("Revocar");
        TemaColors.aplicarEstiloBotonPeligro(btnRevocar);
        btnRevocar.addActionListener(e -> revocarAutorizacion());
        panelAcciones.add(btnRevocar);

        panel.add(panelFiltros, BorderLayout.NORTH);
        panel.add(scrollTabla, BorderLayout.CENTER);
        panel.add(panelAcciones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Busca un visitante por DNI (búsqueda rápida).
     */
    private void buscarVisitantePorDni() {
        String dni = txtDniVisitante.getText().trim();

        if (dni.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar un DNI para buscar",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            visitanteSeleccionado = controlador.buscarVisitantePorDNI(dni);

            if (visitanteSeleccionado == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún visitante con DNI: " + dni,
                    "Visitante no encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
                actualizarLabelVisitante();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar visitante: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Busca un interno por legajo (búsqueda rápida).
     */
    private void buscarInternoPorLegajo() {
        String legajo = txtLegajoInterno.getText().trim();

        if (legajo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar un legajo para buscar",
                "Datos Incompletos",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            internoSeleccionado = controlador.buscarInternoPorLegajo(legajo);

            if (internoSeleccionado == null) {
                JOptionPane.showMessageDialog(this,
                    "No se encontró ningún interno con legajo: " + legajo,
                    "Interno no encontrado",
                    JOptionPane.INFORMATION_MESSAGE);
                actualizarLabelInterno();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al buscar interno: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Abre el diálogo de búsqueda avanzada de visitantes.
     */
    private void buscarVisitanteAvanzada() {
        Visitante visitante = DialogoBusquedaVisitante.mostrarDialogo(this);
        if (visitante != null) {
            visitanteSeleccionado = visitante;
            txtDniVisitante.setText(visitante.getDni());
            actualizarLabelVisitante();
        }
    }

    /**
     * Abre el diálogo de búsqueda avanzada de internos.
     */
    private void buscarInternoAvanzado() {
        Interno interno = DialogoBusquedaInterno.mostrarDialogo(this);
        if (interno != null) {
            internoSeleccionado = interno;
            txtLegajoInterno.setText(interno.getNumeroLegajo());
            actualizarLabelInterno();
        }
    }

    /**
     * Actualiza el label del visitante seleccionado.
     */
    private void actualizarLabelVisitante() {
        if (visitanteSeleccionado == null) {
            lblVisitanteSeleccionado.setText("Ningún visitante seleccionado");
            lblVisitanteSeleccionado.setForeground(TemaColors.TEXTO_SECUNDARIO);
        } else {
            lblVisitanteSeleccionado.setText(
                String.format("[OK] %s (%s) - %s",
                    visitanteSeleccionado.getNombreCompleto(),
                    visitanteSeleccionado.getDni(),
                    visitanteSeleccionado.getEstado()));
            lblVisitanteSeleccionado.setForeground(TemaColors.ESTADO_EXITO);
        }
    }

    /**
     * Actualiza el label del interno seleccionado.
     */
    private void actualizarLabelInterno() {
        if (internoSeleccionado == null) {
            lblInternoSeleccionado.setText("Ningún interno seleccionado");
            lblInternoSeleccionado.setForeground(TemaColors.TEXTO_SECUNDARIO);
        } else {
            lblInternoSeleccionado.setText(
                String.format("[OK] %s (Legajo: %s) - Ubicación: %s, Piso %s",
                    internoSeleccionado.getNombreCompleto(),
                    internoSeleccionado.getNumeroLegajo(),
                    internoSeleccionado.getPabellonActual() != null ?
                        internoSeleccionado.getPabellonActual() : "N/A",
                    internoSeleccionado.getPisoActual() > 0 ?
                        String.valueOf(internoSeleccionado.getPisoActual()) : "N/A"));
            lblInternoSeleccionado.setForeground(TemaColors.ESTADO_EXITO);
        }
    }

    /**
     * Crea una nueva autorización.
     */
    private void crearAutorizacion() {
        // Validar que visitante e interno estén seleccionados
        if (visitanteSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Debe buscar y seleccionar un visitante primero",
                "Visitante no seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (internoSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Debe buscar y seleccionar un interno primero",
                "Interno no seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Obtener datos del formulario
        TipoRelacion tipoRelacion = (TipoRelacion) cmbTipoRelacion.getSelectedItem();
        Date fechaVencimiento = datePickerFechaVencimiento.getFecha();
        String observaciones = txtObservaciones.getText().trim();

        // Confirmar creación
        int opcion = JOptionPane.showConfirmDialog(this,
            String.format(
                "¿Confirma la creación de esta autorización?\n\n" +
                "Visitante: %s\n" +
                "Interno: %s\n" +
                "Relación: %s\n" +
                "Vencimiento: %s\n",
                visitanteSeleccionado.getNombreCompleto(),
                internoSeleccionado.getNombreCompleto(),
                tipoRelacion.getDescripcion(),
                fechaVencimiento != null ? DATE_FORMAT.format(fechaVencimiento) : "Indefinida"),
            "Confirmar Autorización",
            JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        // Crear autorización
        try {
            Autorizacion nuevaAutorizacion = controlador.crearAutorizacion(
                visitanteSeleccionado.getIdVisitante(),
                internoSeleccionado.getIdInterno(),
                tipoRelacion,
                fechaVencimiento,
                observaciones.isEmpty() ? null : observaciones
            );

            JOptionPane.showMessageDialog(this,
                "Autorización creada exitosamente\n" +
                "ID: " + nuevaAutorizacion.getIdAutorizacion(),
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            limpiarFormulario();
            cargarAutorizaciones();

        } catch (IllegalStateException ex) {
            // Autorización duplicada
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Autorización Duplicada",
                JOptionPane.WARNING_MESSAGE);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al crear autorización: " + ex.getMessage(),
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
     * Limpia el formulario de nueva autorización.
     */
    private void limpiarFormulario() {
        txtDniVisitante.setText("");
        txtLegajoInterno.setText("");
        visitanteSeleccionado = null;
        internoSeleccionado = null;
        actualizarLabelVisitante();
        actualizarLabelInterno();
        cmbTipoRelacion.setSelectedIndex(0);
        datePickerFechaVencimiento.limpiar(); // Restablecer a fecha actual
        txtObservaciones.setText("");
    }

    /**
     * Carga las autorizaciones en la tabla.
     */
    private void cargarAutorizaciones() {
        try {
            // Limpiar tabla
            modeloTabla.setRowCount(0);

            // Obtener autorizaciones según filtro
            List<Autorizacion> autorizaciones;
            EstadoAutorizacion estadoFiltro = (EstadoAutorizacion) cmbFiltroEstado.getSelectedItem();

            if (estadoFiltro == null) {
                autorizaciones = controlador.listarTodasAutorizaciones();
            } else {
                autorizaciones = controlador.listarPorEstado(estadoFiltro);
            }

            // Cargar datos completos de visitantes e internos
            controlador.cargarDatosCompletos(autorizaciones);

            // Agregar a tabla
            for (Autorizacion aut : autorizaciones) {
                Object[] fila = {
                    aut.getIdAutorizacion(),
                    aut.getVisitante() != null ? aut.getVisitante().getNombreCompleto() : "N/A",
                    aut.getVisitante() != null ? aut.getVisitante().getDni() : "N/A",
                    aut.getInterno() != null ? aut.getInterno().getNombreCompleto() : "N/A",
                    aut.getInterno() != null ? aut.getInterno().getNumeroLegajo() : "N/A",
                    aut.getTipoRelacion() != null ? aut.getTipoRelacion().getDescripcion() : "N/A",
                    aut.getFechaAutorizacion() != null ?
                        DATE_FORMAT.format(aut.getFechaAutorizacion()) : "N/A",
                    aut.getFechaVencimiento() != null ?
                        DATE_FORMAT.format(aut.getFechaVencimiento()) : "Indefinida",
                    aut.getEstado() != null ? aut.getEstado().name() : "N/A",
                    aut.estaVigente() ? "SI" : "NO"
                };
                modeloTabla.addRow(fila);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar autorizaciones: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Suspende la autorización seleccionada.
     */
    private void suspenderAutorizacion() {
        int filaSeleccionada = tblAutorizaciones.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar una autorización de la tabla",
                "Ninguna autorización seleccionada",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idAutorizacion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        String motivo = JOptionPane.showInputDialog(this,
            "Ingrese el motivo de la suspensión:",
            "Suspender Autorización",
            JOptionPane.QUESTION_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            return; // Usuario canceló o no ingresó motivo
        }

        try {
            controlador.suspenderAutorizacion(idAutorizacion, motivo.trim());

            JOptionPane.showMessageDialog(this,
                "Autorización suspendida exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarAutorizaciones();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al suspender autorización: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Reactiva la autorización seleccionada.
     */
    private void reactivarAutorizacion() {
        int filaSeleccionada = tblAutorizaciones.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar una autorización de la tabla",
                "Ninguna autorización seleccionada",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idAutorizacion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        int opcion = JOptionPane.showConfirmDialog(this,
            "¿Confirma la reactivación de esta autorización?",
            "Reactivar Autorización",
            JOptionPane.YES_NO_OPTION);

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            controlador.reactivarAutorizacion(idAutorizacion);

            JOptionPane.showMessageDialog(this,
                "Autorización reactivada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarAutorizaciones();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al reactivar autorización: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Renueva la autorización seleccionada.
     */
    private void renovarAutorizacion() {
        int filaSeleccionada = tblAutorizaciones.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar una autorización de la tabla",
                "Ninguna autorización seleccionada",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idAutorizacion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        String fechaStr = JOptionPane.showInputDialog(this,
            "Ingrese la nueva fecha de vencimiento (dd/MM/yyyy):\n" +
            "Dejar vacío para autorización indefinida",
            "Renovar Autorización",
            JOptionPane.QUESTION_MESSAGE);

        if (fechaStr == null) {
            return; // Usuario canceló
        }

        Date nuevaFecha = null;
        if (!fechaStr.trim().isEmpty()) {
            try {
                nuevaFecha = DATE_FORMAT.parse(fechaStr.trim());
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this,
                    "Formato de fecha inválido. Use dd/MM/yyyy",
                    "Error en Fecha",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            controlador.renovarAutorizacion(idAutorizacion, nuevaFecha);

            JOptionPane.showMessageDialog(this,
                "Autorización renovada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarAutorizaciones();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al renovar autorización: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Revoca la autorización seleccionada.
     */
    private void revocarAutorizacion() {
        int filaSeleccionada = tblAutorizaciones.getSelectedRow();

        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar una autorización de la tabla",
                "Ninguna autorización seleccionada",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long idAutorizacion = (Long) modeloTabla.getValueAt(filaSeleccionada, 0);

        String motivo = JOptionPane.showInputDialog(this,
            "ADVERTENCIA: La revocación es PERMANENTE.\n\n" +
            "Ingrese el motivo de la revocación:",
            "Revocar Autorización",
            JOptionPane.WARNING_MESSAGE);

        if (motivo == null || motivo.trim().isEmpty()) {
            return; // Usuario canceló o no ingresó motivo
        }

        try {
            controlador.revocarAutorizacion(idAutorizacion, motivo.trim());

            JOptionPane.showMessageDialog(this,
                "Autorización revocada exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);

            cargarAutorizaciones();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Error al revocar autorización: " + ex.getMessage(),
                "Error de Base de Datos",
                JOptionPane.ERROR_MESSAGE);

        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Error",
                JOptionPane.WARNING_MESSAGE);
        }
    }
}
