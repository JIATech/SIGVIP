package com.sigvip.vista;

import com.sigvip.controlador.ControladorAcceso;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visita;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.enums.Rol;
import com.sigvip.utilidades.TemaColors;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.RowFilter.Entry;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Vista para control de ingreso y egreso de visitantes.
 * Implementa las funcionalidades más críticas del sistema:
 * - RF003: Control de Ingreso (validación de 6 pasos)
 * - RF004: Control de Egreso
 *
 * Flujo de ingreso (RF003):
 * 1. Ingresa DNI visitante y legajo interno
 * 2. Sistema valida automáticamente:
 *    - Visitante existe y está ACTIVO
 *    - Autorización vigente
 *    - Sin restricciones activas
 *    - Horario permitido
 *    - Interno disponible
 *    - Capacidad no superada
 * 3. Si todo OK, registra ingreso
 *
 * Especificación: PDF Sección 7.3 (RF003) y 7.4 (RF004)
 */
public class VistaControlAcceso extends JFrame {

    private Usuario usuarioActual;
    private ControladorAcceso controlador;

    // Componentes de ingreso
    private JTextField txtDniIngreso;
    private JTextField txtLegajoIngreso;
    private JButton btnBuscarVisitanteIngreso;
    private JButton btnBuscarInternoIngreso;
    private JButton btnRegistrarIngreso;
    private JTextArea txtResultadoIngreso;

    // Componentes de egreso
    private JTextField txtIdVisita;
    private JTextArea txtObservaciones;
    private JButton btnRegistrarEgreso;

    // Tabla de visitas en curso
    private JTable tablaVisitas;
    private DefaultTableModel modeloTabla;
    private JTextField txtFiltroVisitas;
    private TableRowSorter<DefaultTableModel> sorterVisitas;

    // Formatos de fecha y hora
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

    /**
     * Constructor que inicializa la vista.
     *
     * @param usuario usuario autenticado
     */
    public VistaControlAcceso(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorAcceso();
        inicializarComponentes();
        configurarVentana();
        cargarVisitasEnCurso();
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Control de Acceso");
        setSize(1000, 750); // Aumentado altura para instrucciones adicionales
        setMinimumSize(new Dimension(900, 650)); // Aumentado mínimo para compatibilidad
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Registrar Ingreso (RF003)", crearPanelIngreso());
        tabbedPane.addTab("Registrar Egreso (RF004)", crearPanelEgreso());
        tabbedPane.addTab("Visitas en Curso", crearPanelVisitasEnCurso());

        panelPrincipal.add(tabbedPane, BorderLayout.CENTER);

        // Botón refrescar en la parte inferior
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBackground(TemaColors.FONDO_PRINCIPAL);
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton btnRefrescar = new JButton("Refrescar Visitas");
        TemaColors.aplicarEstiloBotonInfo(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarVisitasEnCurso());

        JButton btnCerrar = new JButton("Cerrar");
        TemaColors.aplicarEstiloBotonCancelar(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());

        panelBotones.add(btnRefrescar);
        panelBotones.add(btnCerrar);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    /**
     * Crea el panel de registro de ingreso (RF003).
     */
    private JPanel crearPanelIngreso() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de instrucciones
        JPanel panelInfo = new JPanel(new BorderLayout());
        panelInfo.setBackground(TemaColors.FONDO_PANEL);
        panelInfo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Información",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));
        String infoBase =
            "RF003: Control de Ingreso con Validación Automática\n\n" +
            "El sistema validará automáticamente:\n" +
            "1. Visitante existe y está ACTIVO\n" +
            "2. Existe autorización vigente para visitar al interno\n" +
            "3. Visitante no tiene restricciones activas\n" +
            "4. Horario dentro del permitido por el establecimiento\n" +
            "5. Interno está disponible para recibir visitas\n" +
            "6. Capacidad del establecimiento no superada";

        // Agregar información sobre autorización inmediata si el usuario tiene permisos
        String infoAdicional = "";
        if (usuarioActual != null && (usuarioActual.getRol() == Rol.ADMINISTRADOR ||
                                     usuarioActual.getRol() == Rol.SUPERVISOR)) {
            infoAdicional = "\n\n" +
                "* AUTORIZACIÓN INMEDIATA (Tu rol: " + usuarioActual.getRol() + ")\n" +
                "• Si el visitante no tiene autorización previa, el sistema te preguntará\n" +
                "• Podrás autorizar inmediatamente la visita (vence al día siguiente)\n" +
                "• Ideal para visitantes espontáneos o situaciones de emergencia";
        }

        JTextArea txtInfo = new JTextArea(infoBase + infoAdicional);
        txtInfo.setEditable(false);
        txtInfo.setBackground(TemaColors.FONDO_PANEL);
        txtInfo.setForeground(TemaColors.TEXTO_PRIMARIO);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        txtInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtInfo.setLineWrap(true);
        txtInfo.setWrapStyleWord(true);

        // Configurar scrollpane con mejor visualización
        JScrollPane scrollInfo = new JScrollPane(txtInfo);
        scrollInfo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollInfo.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollInfo.setPreferredSize(new Dimension(850, 180)); // Altura preferencial para el panel de info
        panelInfo.add(scrollInfo, BorderLayout.CENTER);

        // Panel de formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(TemaColors.FONDO_PANEL);
        panelFormulario.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Datos del Ingreso",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DNI Visitante
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblDniVisitante = new JLabel("DNI Visitante:");
        lblDniVisitante.setFont(new Font("Arial", Font.BOLD, 12));
        lblDniVisitante.setForeground(TemaColors.TEXTO_PRIMARIO);
        panelFormulario.add(lblDniVisitante, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        txtDniIngreso = new JTextField(20);
        txtDniIngreso.setBackground(Color.WHITE);
        txtDniIngreso.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO.darker(), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtDniIngreso.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDniIngreso.setToolTipText("Ingrese DNI sin puntos (ej: 12345678)");
        panelFormulario.add(txtDniIngreso, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        btnBuscarVisitanteIngreso = new JButton("Buscar Visitante");
        btnBuscarVisitanteIngreso.setPreferredSize(new Dimension(150, 30));
        TemaColors.aplicarEstiloBoton(btnBuscarVisitanteIngreso, Color.WHITE);
        btnBuscarVisitanteIngreso.addActionListener(e -> buscarVisitanteParaIngreso());
        panelFormulario.add(btnBuscarVisitanteIngreso, gbc);

        // Legajo Interno
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblLegajoInterno = new JLabel("Legajo Interno:");
        lblLegajoInterno.setFont(new Font("Arial", Font.BOLD, 12));
        lblLegajoInterno.setForeground(TemaColors.TEXTO_PRIMARIO);
        panelFormulario.add(lblLegajoInterno, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        txtLegajoIngreso = new JTextField(20);
        txtLegajoIngreso.setBackground(Color.WHITE);
        txtLegajoIngreso.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO.darker(), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtLegajoIngreso.setFont(new Font("Arial", Font.PLAIN, 12));
        txtLegajoIngreso.setToolTipText("Ingrese número de legajo del interno");
        panelFormulario.add(txtLegajoIngreso, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        btnBuscarInternoIngreso = new JButton("Buscar Interno");
        btnBuscarInternoIngreso.setPreferredSize(new Dimension(150, 30));
        TemaColors.aplicarEstiloBoton(btnBuscarInternoIngreso, Color.WHITE);
        btnBuscarInternoIngreso.addActionListener(e -> buscarInternoParaIngreso());
        panelFormulario.add(btnBuscarInternoIngreso, gbc);

        // Botón Registrar Ingreso
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        btnRegistrarIngreso = new JButton("Registrar Ingreso");
        btnRegistrarIngreso.setPreferredSize(new Dimension(200, 35));
        TemaColors.aplicarEstiloBoton(btnRegistrarIngreso, Color.WHITE);
        btnRegistrarIngreso.addActionListener(e -> registrarIngreso());
        panelFormulario.add(btnRegistrarIngreso, gbc);

        // Panel de resultado
        JPanel panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBackground(TemaColors.FONDO_PANEL);
        panelResultado.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Resultado de Validación",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));
        txtResultadoIngreso = new JTextArea(8, 40);
        txtResultadoIngreso.setEditable(false);
        txtResultadoIngreso.setBackground(Color.WHITE);
        txtResultadoIngreso.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtResultadoIngreso.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultadoIngreso.setForeground(TemaColors.TEXTO_PRIMARIO);
        panelResultado.add(new JScrollPane(txtResultadoIngreso), BorderLayout.CENTER);

        // Ensamblar panel
        JPanel panelSuperior = new JPanel(new GridLayout(2, 1, 10, 10));
        panelSuperior.add(panelInfo);
        panelSuperior.add(panelFormulario);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(panelResultado, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel de registro de egreso (RF004).
     */
    private JPanel crearPanelEgreso() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBackground(TemaColors.FONDO_PANEL);
        panelFormulario.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Registrar Egreso",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID Visita
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblIdVisita = new JLabel("ID Visita:");
        lblIdVisita.setFont(new Font("Arial", Font.BOLD, 12));
        lblIdVisita.setForeground(TemaColors.TEXTO_PRIMARIO);
        panelFormulario.add(lblIdVisita, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtIdVisita = new JTextField(20);
        txtIdVisita.setBackground(Color.WHITE);
        txtIdVisita.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO.darker(), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtIdVisita.setFont(new Font("Arial", Font.PLAIN, 12));
        txtIdVisita.setToolTipText("Ingrese el ID de la visita a finalizar");
        panelFormulario.add(txtIdVisita, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JLabel lblObservaciones = new JLabel("Observaciones (opcional):");
        lblObservaciones.setFont(new Font("Arial", Font.BOLD, 12));
        lblObservaciones.setForeground(TemaColors.TEXTO_PRIMARIO);
        panelFormulario.add(lblObservaciones, gbc);

        gbc.gridy = 2;
        txtObservaciones = new JTextArea(4, 30);
        txtObservaciones.setBackground(Color.WHITE);
        txtObservaciones.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO.darker(), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtObservaciones.setFont(new Font("Arial", Font.PLAIN, 12));
        txtObservaciones.setForeground(TemaColors.TEXTO_PRIMARIO);
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setToolTipText("Ingrese observaciones sobre el egreso");
        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        panelFormulario.add(scrollObs, gbc);

        // Botón Registrar Egreso
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        btnRegistrarEgreso = new JButton("Registrar Egreso");
        btnRegistrarEgreso.setPreferredSize(new Dimension(200, 35));
        TemaColors.aplicarEstiloBotonPeligro(btnRegistrarEgreso); // Ahora será blanco con borde negro
        btnRegistrarEgreso.addActionListener(e -> registrarEgreso());
        panelFormulario.add(btnRegistrarEgreso, gbc);

        panel.add(panelFormulario, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Crea el panel con la tabla de visitas en curso.
     */
    private JPanel crearPanelVisitasEnCurso() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(TemaColors.FONDO_PANEL);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Visitas En Curso",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));

        // Panel de búsqueda y filtrado
        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));
        panelBusqueda.setBackground(TemaColors.FONDO_PANEL);
        panelBusqueda.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            "Filtrar Visitas",
            0,
            0,
            new Font("Arial", Font.BOLD, 12),
            TemaColors.PRIMARIO
        ));

        JPanel panelFiltro = new JPanel(new BorderLayout(5, 5));
        panelFiltro.setBackground(TemaColors.FONDO_PANEL);
        panelFiltro.add(new JLabel("Buscar (DNI, visitante, legajo, interno):"), BorderLayout.WEST);

        txtFiltroVisitas = new JTextField();
        txtFiltroVisitas.setBackground(Color.WHITE);
        txtFiltroVisitas.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO.darker(), 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        txtFiltroVisitas.setToolTipText("Ingrese texto para filtrar visitas activas");
        txtFiltroVisitas.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarVisitas(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarVisitas(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarVisitas(); }
        });
        panelFiltro.add(txtFiltroVisitas, BorderLayout.CENTER);

        JButton btnLimpiarFiltro = new JButton("Limpiar");
        TemaColors.aplicarEstiloBoton(btnLimpiarFiltro, Color.WHITE);
        btnLimpiarFiltro.addActionListener(e -> {
            txtFiltroVisitas.setText("");
            cargarVisitasEnCurso();
        });
        panelFiltro.add(btnLimpiarFiltro, BorderLayout.EAST);

        panelBusqueda.add(panelFiltro, BorderLayout.CENTER);

        // Panel de botones de búsqueda
        JPanel panelBotonesBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotonesBusqueda.setBackground(TemaColors.FONDO_PANEL);

        JButton btnVerTodosVisitantes = new JButton("Ver Todos los Visitantes");
        TemaColors.aplicarEstiloBoton(btnVerTodosVisitantes, Color.WHITE);
        btnVerTodosVisitantes.addActionListener(e -> verTodosLosVisitantes());
        panelBotonesBusqueda.add(btnVerTodosVisitantes);

        JButton btnVerTodosInternos = new JButton("Ver Todos los Internos");
        TemaColors.aplicarEstiloBoton(btnVerTodosInternos, Color.WHITE);
        btnVerTodosInternos.addActionListener(e -> verTodosLosInternos());
        panelBotonesBusqueda.add(btnVerTodosInternos);

        panelBusqueda.add(panelBotonesBusqueda, BorderLayout.SOUTH);
        panel.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla de visitas
        String[] columnas = {"ID", "DNI Visitante", "Visitante", "Legajo Interno",
                            "Interno", "Hora Ingreso", "Duración"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabla no editable
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

        // Configurar ordenamiento y filtrado
        sorterVisitas = new TableRowSorter<>(modeloTabla);
        tablaVisitas.setRowSorter(sorterVisitas);

        // Ajustar anchos de columnas
        tablaVisitas.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaVisitas.getColumnModel().getColumn(1).setPreferredWidth(100); // DNI
        tablaVisitas.getColumnModel().getColumn(2).setPreferredWidth(150); // Visitante
        tablaVisitas.getColumnModel().getColumn(3).setPreferredWidth(100); // Legajo
        tablaVisitas.getColumnModel().getColumn(4).setPreferredWidth(150); // Interno
        tablaVisitas.getColumnModel().getColumn(5).setPreferredWidth(120); // Hora
        tablaVisitas.getColumnModel().getColumn(6).setPreferredWidth(80);  // Duración

        JScrollPane scrollPane = new JScrollPane(tablaVisitas);
        scrollPane.setBackground(TemaColors.FONDO_PANEL);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setBackground(TemaColors.FONDO_PANEL);
        JLabel lblInfo = new JLabel("Doble click en una fila para usar su ID en el egreso");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(TemaColors.TEXTO_SECUNDARIO);
        panelInfo.add(lblInfo);
        panel.add(panelInfo, BorderLayout.SOUTH);

        // Listener para doble click
        tablaVisitas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = tablaVisitas.getSelectedRow();
                    if (row >= 0) {
                        String id = modeloTabla.getValueAt(row, 0).toString();
                        txtIdVisita.setText(id);
                        // Cambiar a la pestaña de egreso
                        JPanel panelPrincipal = (JPanel) VistaControlAcceso.this
                            .getContentPane().getComponent(0);
                        JTabbedPane tabbedPane = (JTabbedPane) panelPrincipal.getComponent(0);
                        tabbedPane.setSelectedIndex(1);
                    }
                }
            }
        });

        return panel;
    }

    /**
     * Registra el ingreso de un visitante (RF003).
     * Implementa validación de 6 pasos.
     */
    private void registrarIngreso() {
        String dni = txtDniIngreso.getText().trim();
        String legajo = txtLegajoIngreso.getText().trim();

        // Validar campos
        if (dni.isEmpty() || legajo.isEmpty()) {
            mostrarError("Complete todos los campos");
            return;
        }

        btnRegistrarIngreso.setEnabled(false);
        btnRegistrarIngreso.setText("Validando...");
        txtResultadoIngreso.setText("Validando permisos de acceso...\n");

        // Ejecutar en hilo separado
        SwingWorker<Long, Void> worker = new SwingWorker<>() {
            @Override
            protected Long doInBackground() {
                return controlador.registrarIngreso(
                    dni,
                    legajo,
                    usuarioActual.getNombreUsuario()
                );
            }

            @Override
            protected void done() {
                try {
                    Long idVisita = get();

                    if (idVisita != null) {
                        txtResultadoIngreso.append("\n✓ INGRESO AUTORIZADO\n");
                        txtResultadoIngreso.append("ID Visita: " + idVisita + "\n");
                        txtResultadoIngreso.setForeground(TemaColors.ESTADO_EXITO);

                        // Limpiar campos
                        txtDniIngreso.setText("");
                        txtLegajoIngreso.setText("");

                        // Actualizar tabla
                        cargarVisitasEnCurso();

                        JOptionPane.showMessageDialog(
                            VistaControlAcceso.this,
                            "Ingreso registrado exitosamente\nID Visita: " + idVisita,
                            "Ingreso Autorizado",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    } else {
                        txtResultadoIngreso.append("\n✗ INGRESO DENEGADO\n");
                        txtResultadoIngreso.append("Verifique la consola para más detalles\n");
                        txtResultadoIngreso.setForeground(TemaColors.ESTADO_ERROR);
                    }
                } catch (Exception e) {
                    txtResultadoIngreso.append("\n✗ ERROR: " + e.getMessage());
                    txtResultadoIngreso.setForeground(TemaColors.ESTADO_ERROR);
                } finally {
                    btnRegistrarIngreso.setEnabled(true);
                    btnRegistrarIngreso.setText("Registrar Ingreso");
                }
            }
        };

        worker.execute();
    }

    /**
     * Registra el egreso de un visitante (RF004).
     */
    private void registrarEgreso() {
        String idVisitaStr = txtIdVisita.getText().trim();
        String observaciones = txtObservaciones.getText().trim();

        // Validar ID
        if (idVisitaStr.isEmpty()) {
            mostrarError("Ingrese el ID de la visita");
            return;
        }

        Long idVisita;
        try {
            idVisita = Long.parseLong(idVisitaStr);
        } catch (NumberFormatException e) {
            mostrarError("ID de visita inválido");
            return;
        }

        btnRegistrarEgreso.setEnabled(false);
        btnRegistrarEgreso.setText("Registrando...");

        // Ejecutar en hilo separado
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return controlador.registrarEgreso(
                    idVisita,
                    usuarioActual.getNombreUsuario(),
                    observaciones.isEmpty() ? null : observaciones
                );
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                            VistaControlAcceso.this,
                            "Egreso registrado exitosamente",
                            "Egreso Registrado",
                            JOptionPane.INFORMATION_MESSAGE
                        );

                        // Limpiar campos
                        txtIdVisita.setText("");
                        txtObservaciones.setText("");

                        // Actualizar tabla
                        cargarVisitasEnCurso();
                    } else {
                        mostrarError("No se pudo registrar el egreso\nVerifique la consola");
                    }
                } catch (Exception e) {
                    mostrarError("Error: " + e.getMessage());
                } finally {
                    btnRegistrarEgreso.setEnabled(true);
                    btnRegistrarEgreso.setText("Registrar Egreso");
                }
            }
        };

        worker.execute();
    }

    /**
     * Carga las visitas en curso en la tabla.
     */
    private void cargarVisitasEnCurso() {
        // Limpiar tabla
        modeloTabla.setRowCount(0);

        // Cargar datos
        List<Visita> visitas = controlador.obtenerVisitasEnCurso();

        for (Visita visita : visitas) {
            Object[] fila = new Object[7];
            fila[0] = visita.getIdVisita();
            fila[1] = visita.getVisitante() != null ? visita.getVisitante().getDni() : "N/A";
            fila[2] = visita.getVisitante() != null ? visita.getVisitante().getNombreCompleto() : "N/A";
            fila[3] = visita.getInterno() != null ? visita.getInterno().getNumeroLegajo() : "N/A";
            fila[4] = visita.getInterno() != null ? visita.getInterno().getNombreCompleto() : "N/A";
            fila[5] = visita.getHoraIngreso() != null ? formatoHora.format(visita.getHoraIngreso()) : "N/A";
            fila[6] = visita.getDuracionFormateada();

            modeloTabla.addRow(fila);
        }

        // Actualizar título de la pestaña
        JPanel panelPrincipal = (JPanel) getContentPane().getComponent(0);
        JTabbedPane tabbedPane = (JTabbedPane) panelPrincipal.getComponent(0);
        tabbedPane.setTitleAt(2, "Visitas en Curso (" + visitas.size() + ")");
    }

    /**
     * Busca un visitante mediante el diálogo avanzado y lo selecciona para ingreso.
     */
    private void buscarVisitanteParaIngreso() {
        Visitante visitante = DialogoBusquedaVisitante.mostrarDialogo(this);
        if (visitante != null) {
            txtDniIngreso.setText(visitante.getDni());
            JOptionPane.showMessageDialog(this,
                "Visitante seleccionado: " + visitante.getNombreCompleto() +
                "\nDNI: " + visitante.getDni(),
                "Visitante Seleccionado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Busca un interno mediante el diálogo avanzado y lo selecciona para ingreso.
     */
    private void buscarInternoParaIngreso() {
        Interno interno = DialogoBusquedaInterno.mostrarDialogo(this);
        if (interno != null) {
            txtLegajoIngreso.setText(interno.getNumeroLegajo());
            JOptionPane.showMessageDialog(this,
                "Interno seleccionado: " + interno.getNombreCompleto() +
                "\nLegajo: " + interno.getNumeroLegajo() +
                "\nPabellón: " + interno.getPabellonActual() +
                "\nPiso: " + interno.getPisoActual(),
                "Interno Seleccionado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Muestra la lista completa de todos los visitantes registrados.
     */
    private void verTodosLosVisitantes() {
        DialogoBusquedaVisitante dialogo = new DialogoBusquedaVisitante(this);
        dialogo.setTitle("Lista Completa de Visitantes Registrados");
        dialogo.setSize(900, 600);
        dialogo.setVisible(true);
    }

    /**
     * Muestra la lista completa de todos los internos registrados.
     */
    private void verTodosLosInternos() {
        DialogoBusquedaInterno dialogo = new DialogoBusquedaInterno(this);
        dialogo.setTitle("Lista Completa de Internos Registrados");
        dialogo.setSize(1000, 700);
        dialogo.setVisible(true);
    }

    /**
     * Filtra las visitas en curso según el texto ingresado.
     */
    private void filtrarVisitas() {
        String textoFiltro = txtFiltroVisitas.getText().trim().toLowerCase();

        if (textoFiltro.isEmpty()) {
            // Si no hay filtro, mostrar todas las visitas
            sorterVisitas.setRowFilter(null);
        } else {
            // Crear filtro simple usando regex para buscar en todas las columnas
            try {
                sorterVisitas.setRowFilter(RowFilter.regexFilter("(?i)" + textoFiltro, 1, 2, 3, 4));
            } catch (java.util.regex.PatternSyntaxException e) {
                // Si hay error en el patrón, no aplicar filtro
                sorterVisitas.setRowFilter(null);
            }
        }

        // Actualizar contador en el título
        int totalFilas = tablaVisitas.getRowCount();
        JPanel panelPrincipal = (JPanel) getContentPane().getComponent(0);
        JTabbedPane tabbedPane = (JTabbedPane) panelPrincipal.getComponent(0);
        tabbedPane.setTitleAt(2, "Visitas en Curso (" + totalFilas + ")");
    }

    /**
     * Muestra un diálogo de error.
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
