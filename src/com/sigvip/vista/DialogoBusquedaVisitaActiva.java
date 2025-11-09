package com.sigvip.vista;

import com.sigvip.controlador.ControladorAcceso;
import com.sigvip.modelo.Visita;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.Interno;
import com.sigvip.utilidades.TemaColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Diálogo para búsqueda avanzada de visitas activas.
 * Permite filtrar por visitante, interno, DNI, legajo, y tiempo de ingreso.
 * Proporciona selección visual con doble click y botones de acción.
 */
public class DialogoBusquedaVisitaActiva extends JDialog {

    private static final long serialVersionUID = 1L;

    private ControladorAcceso controlador;
    private JTable tablaVisitas;
    private DefaultTableModel modeloTabla;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField txtFiltro;
    private Visita visitaSeleccionada;
    private JButton btnSeleccionar;
    private JButton btnCancelar;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatoDuracion = new SimpleDateFormat("HH:mm");

    /**
     * Constructor del diálogo.
     *
     * @param padre ventana padre para modalidad
     */
    public DialogoBusquedaVisitaActiva(Frame padre) {
        super(padre, "Buscar Visita Activa", true);
        this.controlador = new ControladorAcceso();
        this.visitaSeleccionada = null;

        inicializarComponentes();
        configurarVentana();
        cargarVisitasActivas();
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(800, 500));
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrincipal.setBackground(TemaColors.FONDO_PRINCIPAL);

        // Panel de búsqueda
        JPanel panelBusqueda = crearPanelBusqueda();
        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla de visitas
        JPanel panelTabla = crearPanelTabla();
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = crearPanelBotones();
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    /**
     * Crea el panel de búsqueda y filtrado.
     */
    private JPanel crearPanelBusqueda() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(TemaColors.FONDO_PANEL);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Buscar Visitas Activas",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));

        // Panel de filtro
        JPanel panelFiltro = new JPanel(new BorderLayout(5, 5));
        panelFiltro.setBackground(TemaColors.FONDO_PANEL);

        JLabel lblFiltro = new JLabel("Filtrar por (visitante, DNI, interno, legajo):");
        lblFiltro.setFont(new Font("Arial", Font.PLAIN, 11));
        lblFiltro.setForeground(TemaColors.TEXTO_PRIMARIO);
        panelFiltro.add(lblFiltro, BorderLayout.WEST);

        txtFiltro = new JTextField();
        txtFiltro.setBackground(Color.WHITE);
        txtFiltro.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO.darker(), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtFiltro.setFont(new Font("Arial", Font.PLAIN, 12));
        txtFiltro.setToolTipText("Ingrese texto para filtrar visitas activas");
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { aplicarFiltro(); }
        });
        panelFiltro.add(txtFiltro, BorderLayout.CENTER);

        JButton btnLimpiar = new JButton("Limpiar");
        TemaColors.aplicarEstiloBoton(btnLimpiar, Color.WHITE);
        btnLimpiar.addActionListener(e -> {
            txtFiltro.setText("");
            aplicarFiltro();
        });
        panelFiltro.add(btnLimpiar, BorderLayout.EAST);

        panel.add(panelFiltro, BorderLayout.CENTER);

        // Instrucciones
        JTextArea txtInstrucciones = new JTextArea(
            "Doble click en una visita para seleccionarla, o selecciónela y haga clic en 'Seleccionar'"
        );
        txtInstrucciones.setEditable(false);
        txtInstrucciones.setBackground(TemaColors.FONDO_PANEL);
        txtInstrucciones.setForeground(TemaColors.TEXTO_SECUNDARIO);
        txtInstrucciones.setFont(new Font("Arial", Font.ITALIC, 10));
        txtInstrucciones.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        panel.add(txtInstrucciones, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Crea el panel con la tabla de visitas.
     */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(TemaColors.FONDO_PANEL);

        // Columnas de la tabla
        String[] columnas = {
            "ID Visita", "DNI Visitante", "Visitante", "Legajo Interno",
            "Interno", "Hora Ingreso", "Duración", "Fecha Visita"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) { // ID Visita
                    return Long.class;
                }
                return String.class;
            }
        };

        tablaVisitas = new JTable(modeloTabla);
        tablaVisitas.setBackground(Color.WHITE);
        tablaVisitas.setForeground(TemaColors.TEXTO_PRIMARIO);
        tablaVisitas.setSelectionBackground(TemaColors.PRIMARIO);
        tablaVisitas.setSelectionForeground(Color.WHITE);
        tablaVisitas.setFont(new Font("Arial", Font.PLAIN, 11));
        tablaVisitas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaVisitas.getTableHeader().setForeground(TemaColors.TEXTO_CLARO);
        tablaVisitas.getTableHeader().setBackground(TemaColors.PRIMARIO);
        tablaVisitas.setGridColor(TemaColors.PRIMARIO);
        tablaVisitas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVisitas.getTableHeader().setReorderingAllowed(false);
        tablaVisitas.setRowHeight(25);

        // Configurar ordenamiento y filtrado
        sorter = new TableRowSorter<>(modeloTabla);
        tablaVisitas.setRowSorter(sorter);

        // Ajustar anchos de columnas
        tablaVisitas.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID Visita
        tablaVisitas.getColumnModel().getColumn(1).setPreferredWidth(100);  // DNI Visitante
        tablaVisitas.getColumnModel().getColumn(2).setPreferredWidth(180);  // Visitante
        tablaVisitas.getColumnModel().getColumn(3).setPreferredWidth(100);  // Legajo Interno
        tablaVisitas.getColumnModel().getColumn(4).setPreferredWidth(180);  // Interno
        tablaVisitas.getColumnModel().getColumn(5).setPreferredWidth(100);  // Hora Ingreso
        tablaVisitas.getColumnModel().getColumn(6).setPreferredWidth(80);   // Duración
        tablaVisitas.getColumnModel().getColumn(7).setPreferredWidth(120);  // Fecha Visita

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(tablaVisitas);
        scrollPane.setPreferredSize(new Dimension(850, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        // Listener de selección
        tablaVisitas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarBotonSeleccionar();
            }
        });

        // Listener de doble click
        tablaVisitas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarVisitaActual();
                }
            }
        });

        return panel;
    }

    /**
     * Crea el panel de botones de acción.
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panel.setBackground(TemaColors.FONDO_PRINCIPAL);

        btnSeleccionar = new JButton("Seleccionar Visita");
        btnSeleccionar.setPreferredSize(new Dimension(150, 35));
        TemaColors.aplicarEstiloBoton(btnSeleccionar, Color.WHITE);
        btnSeleccionar.addActionListener(e -> seleccionarVisitaActual());
        btnSeleccionar.setEnabled(false);

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setPreferredSize(new Dimension(120, 35));
        TemaColors.aplicarEstiloBotonCancelar(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());

        panel.add(btnSeleccionar);
        panel.add(btnCancelar);

        return panel;
    }

    /**
     * Carga las visitas activas en la tabla.
     */
    private void cargarVisitasActivas() {
        modeloTabla.setRowCount(0);

        List<Visita> visitas = controlador.obtenerVisitasEnCurso();

        for (Visita visita : visitas) {
            Object[] fila = new Object[8];
            fila[0] = visita.getIdVisita();
            fila[1] = visita.getVisitante() != null ? visita.getVisitante().getDni() : "N/A";
            fila[2] = visita.getVisitante() != null ? visita.getVisitante().getNombreCompleto() : "N/A";
            fila[3] = visita.getInterno() != null ? visita.getInterno().getNumeroLegajo() : "N/A";
            fila[4] = visita.getInterno() != null ? visita.getInterno().getNombreCompleto() : "N/A";
            fila[5] = visita.getHoraIngreso() != null ? formatoHora.format(visita.getHoraIngreso()) : "N/A";
            fila[6] = visita.getDuracionFormateada();
            fila[7] = visita.getFechaVisita() != null ? formatoFecha.format(visita.getFechaVisita()) : "N/A";

            modeloTabla.addRow(fila);
        }

        // Actualizar título
        setTitle("Buscar Visita Activa (" + visitas.size() + " visitas en curso)");
    }

    /**
     * Aplica el filtro de búsqueda a la tabla.
     */
    private void aplicarFiltro() {
        String textoFiltro = txtFiltro.getText().trim();

        if (textoFiltro.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            try {
                // Filtrar por todas las columnas relevantes (visitante, DNI, interno, legajo)
                sorter.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + textoFiltro, 1, 2, 3, 4
                ));
            } catch (java.util.regex.PatternSyntaxException e) {
                sorter.setRowFilter(null);
            }
        }
    }

    /**
     * Actualiza el estado del botón seleccionar según la selección actual.
     */
    private void actualizarBotonSeleccionar() {
        int filaSeleccionada = tablaVisitas.getSelectedRow();
        btnSeleccionar.setEnabled(filaSeleccionada >= 0);
    }

    /**
     * Selecciona la visita actualmente seleccionada en la tabla.
     */
    private void seleccionarVisitaActual() {
        int filaSeleccionada = tablaVisitas.getSelectedRow();

        if (filaSeleccionada >= 0) {
            // Convertir fila de vista a modelo (por si hay filtro/ordenamiento)
            int modeloFila = tablaVisitas.convertRowIndexToModel(filaSeleccionada);

            Long idVisita = (Long) modeloTabla.getValueAt(modeloFila, 0);
            String dniVisitante = (String) modeloTabla.getValueAt(modeloFila, 1);
            String nombreVisitante = (String) modeloTabla.getValueAt(modeloFila, 2);
            String legajoInterno = (String) modeloTabla.getValueAt(modeloFila, 3);
            String nombreInterno = (String) modeloTabla.getValueAt(modeloFila, 4);

            // Obtener visita completa del controlador
            visitaSeleccionada = controlador.buscarVisitaPorId(idVisita);

            // Mostrar confirmación
            int resultado = JOptionPane.showConfirmDialog(
                this,
                "¿Seleccionar esta visita para egreso?\n\n" +
                "ID: " + idVisita + "\n" +
                "Visitante: " + nombreVisitante + " (DNI: " + dniVisitante + ")\n" +
                "Interno: " + nombreInterno + " (Legajo: " + legajoInterno + ")",
                "Confirmar Selección",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (resultado == JOptionPane.YES_OPTION) {
                dispose();
            }
        }
    }

    /**
     * Muestra el diálogo y retorna la visita seleccionada.
     *
     * @param padre ventana padre
     * @return visita seleccionada o null si se canceló
     */
    public static Visita mostrarDialogo(Frame padre) {
        DialogoBusquedaVisitaActiva dialogo = new DialogoBusquedaVisitaActiva(padre);
        dialogo.setVisible(true);
        return dialogo.getVisitaSeleccionada();
    }

    /**
     * Retorna la visita seleccionada.
     *
     * @return visita seleccionada o null
     */
    public Visita getVisitaSeleccionada() {
        return visitaSeleccionada;
    }
}