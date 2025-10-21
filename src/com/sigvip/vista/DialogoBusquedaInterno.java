package com.sigvip.vista;

import com.sigvip.modelo.Interno;
import com.sigvip.controlador.ControladorInternos;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Diálogo de búsqueda avanzada de internos.
 * Permite buscar internos por legajo, apellido, nombre, DNI, pabellón o piso
 * con filtros y selección directa.
 */
public class DialogoBusquedaInterno extends JDialog {

    private Interno internoSeleccionado;
    private JTable tablaInternos;
    private DefaultTableModel modeloTabla;
    private JTextField txtFiltro;
    private final ControladorInternos controlador;

    public DialogoBusquedaInterno(Frame parent) {
        super(parent, "Buscar Interno", true);
        // El controlador requiere un usuario, usaremos null para este caso de búsqueda
        this.controlador = new ControladorInternos(null);
        inicializarComponentes();
        configurarVentana();
        cargarInternos();
    }

    private void configurarVentana() {
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));
        panelBusqueda.add(new JLabel("Buscar (legajo, DNI, apellido, nombre, pabellón o piso):"), BorderLayout.WEST);

        txtFiltro = new JTextField();
        txtFiltro.setToolTipText("Ingrese legajo, DNI, apellido, nombre, pabellón o piso para filtrar");
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarInternos(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarInternos(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarInternos(); }
        });
        panelBusqueda.add(txtFiltro, BorderLayout.CENTER);

        JButton btnLimpiarFiltro = new JButton("Limpiar");
        btnLimpiarFiltro.addActionListener(e -> {
            txtFiltro.setText("");
            cargarInternos();
        });
        panelBusqueda.add(btnLimpiarFiltro, BorderLayout.EAST);

        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla de internos
        String[] columnas = {"Legajo", "Apellido", "Nombre", "DNI", "Pabellón", "Piso", "Situación", "Fecha Ingreso"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaInternos = new JTable(modeloTabla);
        tablaInternos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaInternos.setRowHeight(25);
        tablaInternos.getTableHeader().setReorderingAllowed(false);

        // Configurar ordenamiento
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaInternos.setRowSorter(sorter);

        // Ajustar anchos de columna
        tablaInternos.getColumnModel().getColumn(0).setPreferredWidth(100); // Legajo
        tablaInternos.getColumnModel().getColumn(1).setPreferredWidth(120); // Apellido
        tablaInternos.getColumnModel().getColumn(2).setPreferredWidth(120); // Nombre
        tablaInternos.getColumnModel().getColumn(3).setPreferredWidth(80);  // DNI
        tablaInternos.getColumnModel().getColumn(4).setPreferredWidth(60);  // Pabellón
        tablaInternos.getColumnModel().getColumn(5).setPreferredWidth(40);  // Piso
        tablaInternos.getColumnModel().getColumn(6).setPreferredWidth(100); // Situación
        tablaInternos.getColumnModel().getColumn(7).setPreferredWidth(90);  // Fecha Ingreso

        // Doble click para seleccionar
        tablaInternos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarInterno();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaInternos);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Panel de información
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelInfo.setBackground(Color.WHITE);
        JLabel lblInfo = new JLabel("Doble click en una fila o use 'Seleccionar' para elegir un interno");
        lblInfo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblInfo.setForeground(Color.GRAY);
        panelInfo.add(lblInfo);
        panelPrincipal.add(panelInfo, BorderLayout.SOUTH);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnSeleccionar = new JButton("Seleccionar");
        btnSeleccionar.addActionListener(e -> seleccionarInterno());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnSeleccionar);
        panelBotones.add(btnCancelar);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void cargarInternos() {
        try {
            System.out.println("DEBUG: DialogoBusquedaInterno - Cargando internos...");
            System.out.println("DEBUG: Modo offline: " + com.sigvip.persistencia.GestorModo.getInstancia().isModoOffline());

            List<Interno> internos = controlador.listarTodos();
            System.out.println("DEBUG: Internos cargados: " + internos.size());

            modeloTabla.setRowCount(0);

            for (Interno interno : internos) {
                System.out.println("DEBUG: Agregando interno: " + interno.getNombreCompleto());
                Object[] fila = {
                    interno.getNumeroLegajo(),
                    interno.getApellido(),
                    interno.getNombre(),
                    interno.getDni(),
                    interno.getPabellonActual(),
                    interno.getPisoActual(),
                    interno.getSituacionProcesal(),
                    interno.getFechaIngreso()
                };
                modeloTabla.addRow(fila);
            }

            System.out.println("DEBUG: Filas en tabla: " + modeloTabla.getRowCount());

            // Forzar actualización visual de la tabla
            modeloTabla.fireTableDataChanged();
            tablaInternos.revalidate();
            tablaInternos.repaint();
            System.out.println("DEBUG: Forzado repintado de tabla");

        } catch (Exception ex) {
            System.out.println("DEBUG: Error en cargarInternos: " + ex.getMessage());
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error al cargar internos: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarInternos() {
        String textoFiltro = txtFiltro.getText().trim().toLowerCase();

        if (textoFiltro.isEmpty()) {
            cargarInternos();
            return;
        }

        try {
            List<Interno> internos = controlador.listarTodos();
            modeloTabla.setRowCount(0);

            for (Interno interno : internos) {
                boolean coincide =
                    interno.getNumeroLegajo().toLowerCase().contains(textoFiltro) ||
                    interno.getDni().toLowerCase().contains(textoFiltro) ||
                    interno.getApellido().toLowerCase().contains(textoFiltro) ||
                    interno.getNombre().toLowerCase().contains(textoFiltro) ||
                    interno.getPabellonActual().toLowerCase().contains(textoFiltro) ||
                    String.valueOf(interno.getPisoActual()).contains(textoFiltro) ||
                    interno.getSituacionProcesal().toString().toLowerCase().contains(textoFiltro);

                if (coincide) {
                    Object[] fila = {
                        interno.getNumeroLegajo(),
                        interno.getApellido(),
                        interno.getNombre(),
                        interno.getDni(),
                        interno.getPabellonActual(),
                        interno.getPisoActual(),
                        interno.getSituacionProcesal(),
                        interno.getFechaIngreso()
                    };
                    modeloTabla.addRow(fila);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al filtrar internos: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarInterno() {
        int filaSeleccionada = tablaInternos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un interno de la lista",
                "Seleccione Interno",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Interno> internos = controlador.listarTodos();
            if (filaSeleccionada < internos.size()) {
                internoSeleccionado = internos.get(filaSeleccionada);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al seleccionar interno: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public Interno getInternoSeleccionado() {
        return internoSeleccionado;
    }

    /**
     * Método estático para mostrar el diálogo y obtener el interno seleccionado.
     */
    public static Interno mostrarDialogo(Frame parent) {
        DialogoBusquedaInterno dialogo = new DialogoBusquedaInterno(parent);
        dialogo.setVisible(true);
        return dialogo.getInternoSeleccionado();
    }
}