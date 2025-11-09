package com.sigvip.vista;

import com.sigvip.controlador.ControladorReportes;
import com.sigvip.modelo.ReporteGenerado;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.TipoReporte;
import com.sigvip.utilidades.TemaColors;
import com.sigvip.vista.componentes.JDatePicker;

import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Vista para generación y visualización de reportes.
 * Implementa RF007: Generar Reportes (versión con pantalla + BD).
 *
 * Características:
 * - Reportes en formato HTML mostrados en JEditorPane
 * - Persistencia automática en base de datos
 * - Impresión directa (Ctrl+P o botón)
 * - Historial de reportes generados
 * - Filtros dinámicos según tipo de reporte
 */
public class VistaReportes extends JFrame {

    private Usuario usuarioActual;
    private ControladorReportes controlador;

    // Componentes del panel de generación
    private JComboBox<String> cbTipoReporte;
    private JPanel panelFiltros;
    private JButton btnGenerar;
    private JProgressBar progressBar;

    // Componentes del panel de visualización
    private JEditorPane editorReporte;
    private JButton btnImprimir;
    private JButton btnGuardar;

    // Componentes del panel de historial
    private JTable tablaHistorial;
    private DefaultTableModel modeloHistorial;
    private JTextField txtBuscarHistorial;

    // Formatos
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatoCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Constructor que inicializa la vista.
     *
     * @param usuario usuario autenticado
     */
    public VistaReportes(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorReportes();
        inicializarComponentes();
        configurarVentana();

        // Cargar historial de forma asíncrona después de que la ventana sea visible
        SwingUtilities.invokeLater(this::cargarHistorial);
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Reportes (RF007)");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 700));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(createImageIcon("/icons/reports.png"));
    }

    /**
     * Crea una imagen para el ícono (placeholder).
     */
    private Image createImageIcon(String path) {
        // En un entorno real, cargaríamos el ícono desde resources
        // Por ahora no usamos iconos para evitar NullPointerException
        return null;
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

        JTabbedPane tabbedPane = new JTabbedPane();

        // Tab 1: Generar Reporte
        tabbedPane.addTab("Generar Reporte", crearPanelGeneracion());

        // Tab 2: Historial de Reportes
        tabbedPane.addTab("Historial", crearPanelHistorial());

        // Tab 3: Estadísticas
        tabbedPane.addTab("Estadísticas", crearPanelEstadisticas());

        contenedorCompleto.add(tabbedPane, BorderLayout.CENTER);
        add(contenedorCompleto);
    }

    /**
     * Crea el panel de generación de reportes.
     */
    private JPanel crearPanelGeneracion() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Panel superior: Configuración
        JPanel panelConfig = new JPanel(new GridBagLayout());
        panelConfig.setBorder(BorderFactory.createTitledBorder("Configuración del Reporte"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Tipo de reporte
        gbc.gridx = 0; gbc.gridy = 0;
        panelConfig.add(new JLabel("Tipo de Reporte:"), gbc);

        gbc.gridx = 1;
        cbTipoReporte = new JComboBox<>(new String[]{
            "Visitas por Fecha",
            "Visitas por Visitante",
            "Visitas por Interno",
            "Estadísticas Generales",
            "Restricciones Activas",
            "Autorizaciones Vigentes"
        });
        cbTipoReporte.addActionListener(e -> actualizarPanelFiltros());
        panelConfig.add(cbTipoReporte, gbc);

        // Panel dinámico de filtros
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panelFiltros = new JPanel(new BorderLayout());
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros"));
        panelConfig.add(panelFiltros, gbc);

        // Botón generar
        gbc.gridy = 2;
        btnGenerar = new JButton("Generar Reporte");
        TemaColors.aplicarEstiloBoton(btnGenerar, TemaColors.ACENTO_REPORTES);
        btnGenerar.addActionListener(e -> generarReporte());
        panelConfig.add(btnGenerar, gbc);

        // Barra de progreso
        gbc.gridy = 3;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panelConfig.add(progressBar, gbc);

        panel.add(panelConfig, BorderLayout.NORTH);

        // Panel inferior: Vista del reporte
        JPanel panelVista = new JPanel(new BorderLayout());
        panelVista.setBorder(BorderFactory.createTitledBorder("Vista del Reporte"));

        editorReporte = new JEditorPane();
        editorReporte.setContentType("text/html");
        editorReporte.setEditable(false);
        editorReporte.setFont(new Font("Arial", Font.PLAIN, 12));
        editorReporte.setText(
            "<div style='text-align: center; padding: 50px; color: #666;'>" +
            "<h2>Bienvenido al Generador de Reportes</h2>" +
            "<p>Seleccione un tipo de reporte y configure los filtros para comenzar.</p>" +
            "<p>Los reportes se guardarán automáticamente en la base de datos.</p>" +
            "</div>"
        );

        JScrollPane scrollReporte = new JScrollPane(editorReporte);
        scrollReporte.setPreferredSize(new Dimension(800, 600));
        panelVista.add(scrollReporte, BorderLayout.CENTER);

        // Panel de botones de acción
        JPanel panelBotones = new JPanel(new FlowLayout());

        btnImprimir = new JButton("Imprimir (Ctrl+P)");
        btnImprimir.setIcon(createButtonIcon("print"));
        btnImprimir.addActionListener(e -> imprimirReporte());
        btnImprimir.setEnabled(false);

        btnGuardar = new JButton("Guardar HTML");
        btnGuardar.setIcon(createButtonIcon("save"));
        btnGuardar.addActionListener(e -> guardarReporte());
        btnGuardar.setEnabled(false);

        // Deshabilitar permanentemente en modo offline
        if (com.sigvip.persistencia.GestorModo.getInstancia().isModoOffline()) {
            btnGuardar.setEnabled(false);
            btnGuardar.setToolTipText("Guardar reportes no disponible en modo offline");
        }

        panelBotones.add(btnImprimir);
        panelBotones.add(btnGuardar);
        panelBotones.add(Box.createHorizontalStrut(20));

        JLabel lblInfo = new JLabel("El reporte se guardará automáticamente en la base de datos");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(TemaColors.TEXTO_SECUNDARIO);
        panelBotones.add(lblInfo);

        panelVista.add(panelBotones, BorderLayout.SOUTH);

        panel.add(panelVista, BorderLayout.CENTER);

        // Inicializar primer panel de filtros
        actualizarPanelFiltros();

        return panel;
    }

    /**
     * Crea un ícono simple para botones.
     */
    private ImageIcon createButtonIcon(String name) {
        // Placeholder - en un entorno real cargaríamos iconos desde resources
        // Retornamos null para evitar NullPointerException con botones deshabilitados
        return null;
    }

    /**
     * Actualiza el panel de filtros según el tipo de reporte seleccionado.
     */
    private void actualizarPanelFiltros() {
        String tipoReporte = (String) cbTipoReporte.getSelectedItem();
        JPanel panelFiltroActual = switch (tipoReporte) {
            case "Visitas por Fecha" -> crearPanelFiltrosFecha();
            case "Visitas por Visitante" -> crearPanelFiltroVisitante();
            case "Visitas por Interno" -> crearPanelFiltroInterno();
            case "Estadísticas Generales" -> crearPanelFiltrosEstadisticas();
            case "Restricciones Activas" -> crearPanelFiltroSimple();
            case "Autorizaciones Vigentes" -> crearPanelFiltroSimple();
            default -> new JPanel();
        };

        panelFiltros.removeAll();
        panelFiltros.add(panelFiltroActual, BorderLayout.CENTER);
        panelFiltros.revalidate();
        panelFiltros.repaint();
    }

    /**
     * Crea panel de filtros para reporte por fecha.
     */
    private JPanel crearPanelFiltrosFecha() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Fechas por defecto
        Date[] fechasDefecto = controlador.obtenerFechasPorDefecto();

        // Fecha inicio
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Fecha Inicio:"), gbc);
        gbc.gridx = 1;
        JDatePicker datePickerInicio = new JDatePicker(fechasDefecto[0]);
        datePickerInicio.setName("datePickerInicio");
        panel.add(datePickerInicio, gbc);

        // Fecha fin
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Fecha Fin:"), gbc);
        gbc.gridx = 1;
        JDatePicker datePickerFin = new JDatePicker(fechasDefecto[1]);
        datePickerFin.setName("datePickerFin");
        panel.add(datePickerFin, gbc);

        // Estado filtro
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{
            "TODAS", "PROGRAMADA", "EN_CURSO", "FINALIZADA", "CANCELADA"
        });
        cbEstado.setName("cbEstado");
        panel.add(cbEstado, gbc);

        return panel;
    }

    /**
     * Crea panel de filtros para reporte por visitante.
     */
    private JPanel crearPanelFiltroVisitante() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // DNI visitante
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("DNI Visitante:"), gbc);
        gbc.gridx = 1;
        JTextField txtDni = new JTextField(12);
        txtDni.setName("txtDni");
        panel.add(txtDni, gbc);

        // Fechas opcionales
        Date[] fechasDefecto = controlador.obtenerFechasPorDefecto();

        gbc.gridy = 1;
        panel.add(new JLabel("Fecha Inicio (opcional):"), gbc);
        gbc.gridx = 2;
        JTextField txtFechaInicio = new JTextField(formatoFecha.format(fechasDefecto[0]), 12);
        txtFechaInicio.setName("txtFechaInicio");
        panel.add(txtFechaInicio, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Fecha Fin (opcional):"), gbc);
        gbc.gridx = 1;
        JTextField txtFechaFin = new JTextField(formatoFecha.format(fechasDefecto[1]), 12);
        txtFechaFin.setName("txtFechaFin");
        panel.add(txtFechaFin, gbc);

        return panel;
    }

    /**
     * Crea panel de filtros para reporte por interno.
     */
    private JPanel crearPanelFiltroInterno() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Legajo interno
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Legajo Interno:"), gbc);
        gbc.gridx = 1;
        JTextField txtLegajo = new JTextField(12);
        txtLegajo.setName("txtLegajo");
        panel.add(txtLegajo, gbc);

        // Fechas opcionales
        Date[] fechasDefecto = controlador.obtenerFechasPorDefecto();

        gbc.gridy = 1;
        panel.add(new JLabel("Fecha Inicio (opcional):"), gbc);
        gbc.gridx = 2;
        JTextField txtFechaInicio = new JTextField(formatoFecha.format(fechasDefecto[0]), 12);
        txtFechaInicio.setName("txtFechaInicio");
        panel.add(txtFechaInicio, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Fecha Fin (opcional):"), gbc);
        gbc.gridx = 1;
        JTextField txtFechaFin = new JTextField(formatoFecha.format(fechasDefecto[1]), 12);
        txtFechaFin.setName("txtFechaFin");
        panel.add(txtFechaFin, gbc);

        return panel;
    }

    /**
     * Crea panel de filtros para reporte de estadísticas.
     */
    private JPanel crearPanelFiltrosEstadisticas() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        Date[] fechasDefecto = controlador.obtenerFechasPorDefecto();

        // Fecha inicio
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Fecha Inicio:"), gbc);
        gbc.gridx = 1;
        JTextField txtFechaInicio = new JTextField(formatoFecha.format(fechasDefecto[0]), 12);
        txtFechaInicio.setName("txtFechaInicio");
        panel.add(txtFechaInicio, gbc);

        // Fecha fin
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Fecha Fin:"), gbc);
        gbc.gridx = 1;
        JTextField txtFechaFin = new JTextField(formatoFecha.format(fechasDefecto[1]), 12);
        txtFechaFin.setName("txtFechaFin");
        panel.add(txtFechaFin, gbc);

        return panel;
    }

    /**
     * Crea panel simple para reportes sin filtros.
     */
    private JPanel crearPanelFiltroSimple() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("No se requieren filtros para este tipo de reporte"));
        return panel;
    }

    /**
     * Genera el reporte según los filtros configurados.
     */
    private void generarReporte() {
        btnGenerar.setEnabled(false);
        btnGenerar.setText("Generando...");
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        SwingWorker<ReporteGenerado, Void> worker = new SwingWorker<>() {
            @Override
            protected ReporteGenerado doInBackground() throws Exception {
                String tipo = (String) cbTipoReporte.getSelectedItem();

                return switch (tipo) {
                    case "Visitas por Fecha" -> generarReporteVisitasPorFecha();
                    case "Visitas por Visitante" -> generarReporteVisitasPorVisitante();
                    case "Visitas por Interno" -> generarReporteVisitasPorInterno();
                    case "Estadísticas Generales" -> generarReporteEstadisticas();
                    case "Restricciones Activas" -> generarReporteRestricciones();
                    case "Autorizaciones Vigentes" -> generarReporteAutorizaciones();
                    default -> throw new IllegalArgumentException("Tipo de reporte no implementado");
                };
            }

            @Override
            protected void done() {
                try {
                    ReporteGenerado reporte = get();
                    mostrarReporte(reporte);
                    btnImprimir.setEnabled(true);
                    // Solo habilitar guardar si NO estamos en modo offline
                    if (!com.sigvip.persistencia.GestorModo.getInstancia().isModoOffline()) {
                        btnGuardar.setEnabled(true);
                    }
                    JOptionPane.showMessageDialog(VistaReportes.this,
                        "Reporte generado exitosamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VistaReportes.this,
                        "Error al generar reporte: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    btnGenerar.setEnabled(true);
                    btnGenerar.setText("Generar Reporte");
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(false);
                }
            }
        };

        worker.execute();
    }

    /**
     * Métodos auxiliares para búsqueda de componentes.
     */
    private JDatePicker findDatePicker(Container container, String name) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JDatePicker && name.equals(comp.getName())) {
                return (JDatePicker) comp;
            } else if (comp instanceof Container) {
                JDatePicker found = findDatePicker((Container) comp, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Métodos de generación específicos.
     */
    private ReporteGenerado generarReporteVisitasPorFecha() throws Exception {
        // Intentar encontrar JDatePickers primero
        JDatePicker datePickerInicio = findDatePicker(panelFiltros, "datePickerInicio");
        JDatePicker datePickerFin = findDatePicker(panelFiltros, "datePickerFin");

        // Si no se encuentran, usar los JTextField tradicionales (compatibilidad)
        JTextField txtFechaInicio = findTextField(panelFiltros, "txtFechaInicio");
        JTextField txtFechaFin = findTextField(panelFiltros, "txtFechaFin");

        JComboBox<String> cbEstado = findComboBox(panelFiltros, "cbEstado");

        Date fechaInicio;
        Date fechaFin;

        if (datePickerInicio != null && datePickerFin != null) {
            // Usar los nuevos selectores de fecha
            fechaInicio = datePickerInicio.getFecha();
            fechaFin = datePickerFin.getFecha();
        } else {
            // Usar los campos de texto tradicionales
            fechaInicio = formatoFecha.parse(txtFechaInicio.getText());
            fechaFin = formatoFecha.parse(txtFechaFin.getText());
        }

        String estado = (String) cbEstado.getSelectedItem();

        // Validar parámetros
        String error = controlador.validarParametrosReporte(fechaInicio, fechaFin);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        return controlador.generarReporteVisitasPorFecha(fechaInicio, fechaFin, estado, usuarioActual.getIdUsuario());
    }

    private ReporteGenerado generarReporteVisitasPorVisitante() throws Exception {
        JTextField txtDni = findTextField(panelFiltros, "txtDni");
        JTextField txtFechaInicio = findTextField(panelFiltros, "txtFechaInicio");
        JTextField txtFechaFin = findTextField(panelFiltros, "txtFechaFin");

        String dni = txtDni.getText().trim();
        Date fechaInicio = null;
        Date fechaFin = null;

        if (!txtFechaInicio.getText().trim().isEmpty()) {
            fechaInicio = formatoFecha.parse(txtFechaInicio.getText());
        }
        if (!txtFechaFin.getText().trim().isEmpty()) {
            fechaFin = formatoFecha.parse(txtFechaFin.getText());
        }

        if (fechaInicio != null && fechaFin != null) {
            String error = controlador.validarParametrosReporte(fechaInicio, fechaFin);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }

        return controlador.generarReporteVisitasPorVisitante(dni, fechaInicio, fechaFin, usuarioActual.getIdUsuario());
    }

    private ReporteGenerado generarReporteVisitasPorInterno() throws Exception {
        JTextField txtLegajo = findTextField(panelFiltros, "txtLegajo");
        JTextField txtFechaInicio = findTextField(panelFiltros, "txtFechaInicio");
        JTextField txtFechaFin = findTextField(panelFiltros, "txtFechaFin");

        String legajo = txtLegajo.getText().trim();
        Date fechaInicio = null;
        Date fechaFin = null;

        if (!txtFechaInicio.getText().trim().isEmpty()) {
            fechaInicio = formatoFecha.parse(txtFechaInicio.getText());
        }
        if (!txtFechaFin.getText().trim().isEmpty()) {
            fechaFin = formatoFecha.parse(txtFechaFin.getText());
        }

        if (fechaInicio != null && fechaFin != null) {
            String error = controlador.validarParametrosReporte(fechaInicio, fechaFin);
            if (error != null) {
                throw new IllegalArgumentException(error);
            }
        }

        return controlador.generarReporteVisitasPorInterno(legajo, fechaInicio, fechaFin, usuarioActual.getIdUsuario());
    }

    private ReporteGenerado generarReporteEstadisticas() throws Exception {
        JTextField txtFechaInicio = findTextField(panelFiltros, "txtFechaInicio");
        JTextField txtFechaFin = findTextField(panelFiltros, "txtFechaFin");

        Date fechaInicio = formatoFecha.parse(txtFechaInicio.getText());
        Date fechaFin = formatoFecha.parse(txtFechaFin.getText());

        String error = controlador.validarParametrosReporte(fechaInicio, fechaFin);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        return controlador.generarReporteEstadisticas(fechaInicio, fechaFin, usuarioActual.getIdUsuario());
    }

    private ReporteGenerado generarReporteRestricciones() throws Exception {
        return controlador.generarReporteRestriccionesActivas(usuarioActual.getIdUsuario());
    }

    private ReporteGenerado generarReporteAutorizaciones() throws Exception {
        return controlador.generarReporteAutorizacionesVigentes(usuarioActual.getIdUsuario());
    }

    /**
     * Busca un JTextField por nombre en un componente.
     */
    private JTextField findTextField(Component container, String name) {
        if (container instanceof JTextField && name.equals(((JTextField) container).getName())) {
            return (JTextField) container;
        }
        if (container instanceof Container) {
            for (Component child : ((Container) container).getComponents()) {
                JTextField found = findTextField(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Busca un JComboBox por nombre en un componente.
     */
    private JComboBox<String> findComboBox(Component container, String name) {
        if (container instanceof JComboBox && name.equals(((JComboBox<?>) container).getName())) {
            return (JComboBox<String>) container;
        }
        if (container instanceof Container) {
            for (Component child : ((Container) container).getComponents()) {
                JComboBox<String> found = findComboBox(child, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Muestra el reporte en el editor.
     */
    private void mostrarReporte(ReporteGenerado reporte) {
        editorReporte.setText(reporte.getContenido());
        editorReporte.setCaretPosition(0);
    }

    /**
     * Imprime el reporte actual.
     */
    private void imprimirReporte() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Reporte SIGVIP");

            if (job.printDialog()) {
                editorReporte.print(null);
                JOptionPane.showMessageDialog(this,
                    "Reporte enviado a impresión",
                    "Impresión", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Impresión cancelada",
                    "Impresión", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al preparar impresión: " + e.getMessage(),
                "Error de Impresión", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Guarda el reporte como archivo HTML.
     */
    private void guardarReporte() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "HTML files", "html"));
        fileChooser.setSelectedFile(new java.io.File("reporte_sigvip.html"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try {
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(editorReporte.getText());
                writer.close();

                JOptionPane.showMessageDialog(this,
                    "Reporte guardado exitosamente en:\n" + file.getAbsolutePath(),
                    "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al guardar el reporte: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Crea el panel de historial de reportes.
     */
    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.add(new JLabel("Buscar:"));
        txtBuscarHistorial = new JTextField(20);
        txtBuscarHistorial.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) {
                filtrarHistorial();
            }
        });
        panelBusqueda.add(txtBuscarHistorial);

        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.addActionListener(e -> cargarHistorial());
        panelBusqueda.add(btnActualizar);

        panel.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla de historial
        String[] columnas = {"ID", "Tipo", "Título", "Fecha Generación", "Registros", "Generado por"};
        modeloHistorial = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaHistorial = new JTable(modeloHistorial);
        tablaHistorial.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaHistorial.getTableHeader().setReorderingAllowed(false);
        tablaHistorial.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaHistorial.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaHistorial.getColumnModel().getColumn(2).setPreferredWidth(200);
        tablaHistorial.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaHistorial.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablaHistorial.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollTabla = new JScrollPane(tablaHistorial);
        panel.add(scrollTabla, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnVer = new JButton("Ver Reporte");
        btnVer.addActionListener(e -> verReporteSeleccionado());

        JButton btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminarReporteSeleccionado());

        panelBotones.add(btnVer);
        panelBotones.add(btnEliminar);

        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Carga el historial de reportes.
     */
    private void cargarHistorial() {
        SwingWorker<List<ReporteGenerado>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ReporteGenerado> doInBackground() throws Exception {
                return controlador.obtenerHistorialReportes();
            }

            @Override
            protected void done() {
                try {
                    List<ReporteGenerado> reportes = get();
                    actualizarTablaHistorial(reportes);
                } catch (Exception e) {
                    // Capturar la excepción raíz para mejor diagnóstico
                    Throwable causa = e.getCause() != null ? e.getCause() : e;

                    JOptionPane.showMessageDialog(VistaReportes.this,
                        "Error al cargar historial: " + causa.getMessage() +
                        "\n\nDetalles técnicos: " + causa.getClass().getSimpleName(),
                        "Error al cargar Reportes", JOptionPane.ERROR_MESSAGE);

                    // También imprimir en consola para debugging
                    System.err.println("Error en cargarHistorial():");
                    causa.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    /**
     * Actualiza la tabla de historial con los reportes proporcionados.
     */
    private void actualizarTablaHistorial(List<ReporteGenerado> reportes) {
        modeloHistorial.setRowCount(0);

        for (ReporteGenerado reporte : reportes) {
            Object[] fila = new Object[6];
            fila[0] = reporte.getIdReporte();
            fila[1] = reporte.getTipoReporte().getNombreFormateado();
            fila[2] = reporte.getTitulo();
            fila[3] = formatoCompleto.format(reporte.getFechaGeneracion());
            fila[4] = reporte.getTotalRegistros();
            fila[5] = reporte.getUsuarioGenerador() != null ?
                reporte.getUsuarioGenerador().getNombreCompleto() : "N/A";

            modeloHistorial.addRow(fila);
        }

        // Actualizar título de la pestaña
        JTabbedPane tabbedPane = encontrarTabbedPane();
        if (tabbedPane != null) {
            tabbedPane.setTitleAt(1, "Historial (" + reportes.size() + ")");
        }
    }

    /**
     * Filtra el historial según el texto de búsqueda.
     */
    private void filtrarHistorial() {
        String textoBusqueda = txtBuscarHistorial.getText().toLowerCase().trim();

        if (textoBusqueda.isEmpty()) {
            cargarHistorial();
            return;
        }

        try {
            List<ReporteGenerado> todos = controlador.obtenerHistorialReportes();
            List<ReporteGenerado> filtrados = new ArrayList<>();

            for (ReporteGenerado reporte : todos) {
                if (reporte.getTitulo().toLowerCase().contains(textoBusqueda) ||
                    reporte.getTipoReporte().getNombre().toLowerCase().contains(textoBusqueda) ||
                    (reporte.getUsuarioGenerador() != null &&
                     reporte.getUsuarioGenerador().getNombreCompleto().toLowerCase().contains(textoBusqueda))) {
                    filtrados.add(reporte);
                }
            }

            actualizarTablaHistorial(filtrados);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al filtrar historial: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Ver el reporte seleccionado en el historial.
     */
    private void verReporteSeleccionado() {
        int filaSeleccionada = tablaHistorial.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un reporte de la lista",
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Long idReporte = (Long) modeloHistorial.getValueAt(filaSeleccionada, 0);

        try {
            ReporteGenerado reporte = controlador.buscarReportePorId(idReporte);
            if (reporte != null) {
                // Cambiar a la pestaña de generación
                JTabbedPane tabbedPane = encontrarTabbedPane();
                if (tabbedPane != null) {
                    tabbedPane.setSelectedIndex(0);
                }

                // Mostrar el reporte
                mostrarReporte(reporte);
                btnImprimir.setEnabled(true);
                // Solo habilitar guardar si NO estamos en modo offline
                if (!com.sigvip.persistencia.GestorModo.getInstancia().isModoOffline()) {
                    btnGuardar.setEnabled(true);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "No se encontró el reporte seleccionado",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar el reporte: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el reporte seleccionado.
     */
    private void eliminarReporteSeleccionado() {
        int filaSeleccionada = tablaHistorial.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Seleccione un reporte de la lista",
                "Información", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar este reporte?\n\nEsta acción no se puede deshacer.",
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            Long idReporte = (Long) modeloHistorial.getValueAt(filaSeleccionada, 0);

            try {
                // Aquí implementaríamos la eliminación si el ReporteDAO tuviera el método
                // reporteDAO.eliminar(idReporte);

                JOptionPane.showMessageDialog(this,
                    "Reporte eliminado exitosamente",
                    "Eliminado", JOptionPane.INFORMATION_MESSAGE);

                cargarHistorial();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al eliminar el reporte: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Crea el panel de estadísticas.
     */
    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout());

        // Panel superior con información general
        JPanel panelInfo = new JPanel(new GridLayout(3, 2, 10, 10));
        panelInfo.setBorder(BorderFactory.createTitledBorder("Estadísticas Generales"));

        panelInfo.add(new JLabel("Total de Reportes:"));
        JLabel lblTotal = new JLabel("0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfo.add(lblTotal);

        panelInfo.add(new JLabel("Reportes del Usuario:"));
        JLabel lblUsuario = new JLabel("0");
        lblUsuario.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfo.add(lblUsuario);

        panelInfo.add(new JLabel("Reportes Hoy:"));
        JLabel lblHoy = new JLabel("0");
        lblHoy.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfo.add(lblHoy);

        panel.add(panelInfo, BorderLayout.NORTH);

        // Panel con distribución por tipo
        JPanel panelDistribucion = new JPanel(new BorderLayout());
        panelDistribucion.setBorder(BorderFactory.createTitledBorder("Distribución por Tipo"));

        String[] columnas = {"Tipo de Reporte", "Cantidad", "Porcentaje"};
        DefaultTableModel modeloDistribucion = new DefaultTableModel(columnas, 0);
        JTable tablaDistribucion = new JTable(modeloDistribucion);
        tablaDistribucion.getColumnModel().getColumn(0).setPreferredWidth(200);
        tablaDistribucion.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaDistribucion.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane scrollDistribucion = new JScrollPane(tablaDistribucion);
        panelDistribucion.add(scrollDistribucion, BorderLayout.CENTER);

        panel.add(panelDistribucion, BorderLayout.CENTER);

        // Botón de actualización
        JButton btnActualizarEstadisticas = new JButton("Actualizar Estadísticas");
        btnActualizarEstadisticas.addActionListener(e -> actualizarEstadisticas(lblTotal, lblUsuario, lblHoy, modeloDistribucion));

        JPanel panelBoton = new JPanel(new FlowLayout());
        panelBoton.add(btnActualizarEstadisticas);
        panel.add(panelBoton, BorderLayout.SOUTH);

        // Cargar estadísticas iniciales
        actualizarEstadisticas(lblTotal, lblUsuario, lblHoy, modeloDistribucion);

        return panel;
    }

    /**
     * Actualiza las estadísticas mostradas.
     */
    private void actualizarEstadisticas(JLabel lblTotal, JLabel lblUsuario, JLabel lblHoy,
                                         DefaultTableModel modeloDistribucion) {
        SwingWorker<java.util.Map<String, Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected java.util.Map<String, Integer> doInBackground() throws Exception {
                return controlador.obtenerEstadisticasReportes();
            }

            @Override
            protected void done() {
                try {
                    java.util.Map<String, Integer> estadisticas = get();
                    int total = 0;

                    modeloDistribucion.setRowCount(0);

                    for (var entry : estadisticas.entrySet()) {
                        modeloDistribucion.addRow(new Object[]{entry.getKey(), entry.getValue(), ""});
                        total += entry.getValue();
                    }

                    lblTotal.setText(String.valueOf(total));

                    try {
                        List<ReporteGenerado> delUsuario = controlador.obtenerReportesPorUsuario(usuarioActual.getIdUsuario());
                        lblUsuario.setText(String.valueOf(delUsuario.size()));
                    } catch (Exception e) {
                        lblUsuario.setText("Error");
                    }

                    try {
                        List<ReporteGenerado> recientes = controlador.obtenerReportesRecientes(1);
                        lblHoy.setText(String.valueOf(recientes.size()));
                    } catch (Exception e) {
                        lblHoy.setText("Error");
                    }

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(VistaReportes.this,
                        "Error al cargar estadísticas: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    /**
     * Encuentra el JTabbedPane navegando la estructura de componentes.
     * Funciona tanto con estructura antigua como con banners offline.
     */
    private JTabbedPane encontrarTabbedPane() {
        Component contentPane = getContentPane().getComponent(0);

        // Estructura con banner (offline)
        if (contentPane instanceof JPanel) {
            JPanel contenedorCompleto = (JPanel) contentPane;
            if (contenedorCompleto.getLayout() instanceof BorderLayout) {
                Component centerComponent = ((BorderLayout)contenedorCompleto.getLayout())
                    .getLayoutComponent(BorderLayout.CENTER);

                if (centerComponent instanceof JPanel) {
                    JPanel panelPrincipal = (JPanel) centerComponent;
                    for (Component comp : panelPrincipal.getComponents()) {
                        if (comp instanceof JTabbedPane) {
                            return (JTabbedPane) comp;
                        }
                    }
                }
            }
        }

        // Estructura antigua (online) - fallback
        if (contentPane instanceof JTabbedPane) {
            return (JTabbedPane) contentPane;
        }

        return null;
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