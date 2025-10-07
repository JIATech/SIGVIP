package com.sigvip.vista;

import com.sigvip.controlador.ControladorAcceso;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visita;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
    private JButton btnRegistrarIngreso;
    private JTextArea txtResultadoIngreso;

    // Componentes de egreso
    private JTextField txtIdVisita;
    private JTextArea txtObservaciones;
    private JButton btnRegistrarEgreso;

    // Tabla de visitas en curso
    private JTable tablaVisitas;
    private DefaultTableModel modeloTabla;

    // Formato de fecha
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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
        setSize(900, 700);
        setMinimumSize(new Dimension(800, 600));
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
        JButton btnRefrescar = new JButton("Refrescar Visitas");
        btnRefrescar.addActionListener(e -> cargarVisitasEnCurso());
        JButton btnCerrar = new JButton("Cerrar");
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
        panelInfo.setBorder(BorderFactory.createTitledBorder("Información"));
        JTextArea txtInfo = new JTextArea(
            "RF003: Control de Ingreso con Validación Automática\n\n" +
            "El sistema validará automáticamente:\n" +
            "1. Visitante existe y está ACTIVO\n" +
            "2. Existe autorización vigente para visitar al interno\n" +
            "3. Visitante no tiene restricciones activas\n" +
            "4. Horario dentro del permitido por el establecimiento\n" +
            "5. Interno está disponible para recibir visitas\n" +
            "6. Capacidad del establecimiento no superada"
        );
        txtInfo.setEditable(false);
        txtInfo.setBackground(new Color(240, 248, 255));
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        panelInfo.add(new JScrollPane(txtInfo), BorderLayout.CENTER);

        // Panel de formulario
        JPanel panelFormulario = new JPanel(new GridBagLayout());
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Ingreso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // DNI Visitante
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panelFormulario.add(new JLabel("DNI Visitante:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtDniIngreso = new JTextField(20);
        txtDniIngreso.setToolTipText("Ingrese DNI sin puntos (ej: 12345678)");
        panelFormulario.add(txtDniIngreso, gbc);

        // Legajo Interno
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panelFormulario.add(new JLabel("Legajo Interno:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtLegajoIngreso = new JTextField(20);
        txtLegajoIngreso.setToolTipText("Ingrese número de legajo del interno");
        panelFormulario.add(txtLegajoIngreso, gbc);

        // Botón Registrar Ingreso
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnRegistrarIngreso = new JButton("Registrar Ingreso");
        btnRegistrarIngreso.setPreferredSize(new Dimension(200, 35));
        btnRegistrarIngreso.setBackground(new Color(46, 204, 113));
        btnRegistrarIngreso.setForeground(Color.WHITE);
        btnRegistrarIngreso.setFocusPainted(false);
        btnRegistrarIngreso.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrarIngreso.addActionListener(e -> registrarIngreso());
        panelFormulario.add(btnRegistrarIngreso, gbc);

        // Panel de resultado
        JPanel panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBorder(BorderFactory.createTitledBorder("Resultado de Validación"));
        txtResultadoIngreso = new JTextArea(8, 40);
        txtResultadoIngreso.setEditable(false);
        txtResultadoIngreso.setFont(new Font("Monospaced", Font.PLAIN, 12));
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
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Registrar Egreso"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ID Visita
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panelFormulario.add(new JLabel("ID Visita:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtIdVisita = new JTextField(20);
        txtIdVisita.setToolTipText("Ingrese el ID de la visita a finalizar");
        panelFormulario.add(txtIdVisita, gbc);

        // Observaciones
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panelFormulario.add(new JLabel("Observaciones (opcional):"), gbc);

        gbc.gridy = 2;
        txtObservaciones = new JTextArea(4, 30);
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
        btnRegistrarEgreso.setBackground(new Color(231, 76, 60));
        btnRegistrarEgreso.setForeground(Color.WHITE);
        btnRegistrarEgreso.setFocusPainted(false);
        btnRegistrarEgreso.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrarEgreso.addActionListener(e -> registrarEgreso());
        panelFormulario.add(btnRegistrarEgreso, gbc);

        panel.add(panelFormulario, BorderLayout.NORTH);

        return panel;
    }

    /**
     * Crea el panel con la tabla de visitas en curso.
     */
    private JPanel crearPanelVisitasEnCurso() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        tablaVisitas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVisitas.getTableHeader().setReorderingAllowed(false);

        // Ajustar anchos de columnas
        tablaVisitas.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaVisitas.getColumnModel().getColumn(1).setPreferredWidth(100); // DNI
        tablaVisitas.getColumnModel().getColumn(2).setPreferredWidth(150); // Visitante
        tablaVisitas.getColumnModel().getColumn(3).setPreferredWidth(100); // Legajo
        tablaVisitas.getColumnModel().getColumn(4).setPreferredWidth(150); // Interno
        tablaVisitas.getColumnModel().getColumn(5).setPreferredWidth(120); // Hora
        tablaVisitas.getColumnModel().getColumn(6).setPreferredWidth(80);  // Duración

        JScrollPane scrollPane = new JScrollPane(tablaVisitas);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel de información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblInfo = new JLabel("Doble click en una fila para usar su ID en el egreso");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
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
                        JTabbedPane parent = (JTabbedPane) VistaControlAcceso.this
                            .getContentPane().getComponent(0);
                        parent.setSelectedIndex(1);
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
                        txtResultadoIngreso.setForeground(new Color(0, 128, 0));

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
                        txtResultadoIngreso.setForeground(new Color(192, 0, 0));
                    }
                } catch (Exception e) {
                    txtResultadoIngreso.append("\n✗ ERROR: " + e.getMessage());
                    txtResultadoIngreso.setForeground(Color.RED);
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
            fila[5] = visita.getHoraIngreso() != null ? formatoFecha.format(visita.getHoraIngreso()) : "N/A";
            fila[6] = visita.getDuracionFormateada();

            modeloTabla.addRow(fila);
        }

        // Actualizar título de la pestaña
        JTabbedPane parent = (JTabbedPane) getContentPane().getComponent(0);
        parent.setTitleAt(2, "Visitas en Curso (" + visitas.size() + ")");
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
