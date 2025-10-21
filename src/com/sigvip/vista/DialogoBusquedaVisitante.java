package com.sigvip.vista;

import com.sigvip.modelo.Visitante;
import com.sigvip.controlador.ControladorVisitantes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Diálogo de búsqueda avanzada de visitantes.
 * Permite buscar visitantes por DNI, apellido o nombre
 * con filtros y selección directa.
 */
public class DialogoBusquedaVisitante extends JDialog {

    private Visitante visitanteSeleccionado;
    private JTable tablaVisitantes;
    private DefaultTableModel modeloTabla;
    private JTextField txtFiltro;
    private final ControladorVisitantes controlador;

    public DialogoBusquedaVisitante(Frame parent) {
        super(parent, "Buscar Visitante", true);
        this.controlador = new ControladorVisitantes();
        inicializarComponentes();
        configurarVentana();
        cargarVisitantes();
    }

    private void configurarVentana() {
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de búsqueda
        JPanel panelBusqueda = new JPanel(new BorderLayout(5, 5));
        panelBusqueda.add(new JLabel("Buscar (DNI, apellido o nombre):"), BorderLayout.WEST);

        txtFiltro = new JTextField();
        txtFiltro.setToolTipText("Ingrese DNI, apellido o nombre para filtrar");
        txtFiltro.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrarVisitantes(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrarVisitantes(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrarVisitantes(); }
        });
        panelBusqueda.add(txtFiltro, BorderLayout.CENTER);

        JButton btnLimpiarFiltro = new JButton("Limpiar");
        btnLimpiarFiltro.addActionListener(e -> {
            txtFiltro.setText("");
            cargarVisitantes();
        });
        panelBusqueda.add(btnLimpiarFiltro, BorderLayout.EAST);

        panelPrincipal.add(panelBusqueda, BorderLayout.NORTH);

        // Tabla de visitantes
        String[] columnas = {"DNI", "Apellido", "Nombre", "Domicilio", "Teléfono", "Estado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaVisitantes = new JTable(modeloTabla);
        tablaVisitantes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVisitantes.setRowHeight(25);
        tablaVisitantes.getTableHeader().setReorderingAllowed(false);

        // Configurar ordenamiento
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modeloTabla);
        tablaVisitantes.setRowSorter(sorter);

        // Ajustar anchos de columna
        tablaVisitantes.getColumnModel().getColumn(0).setPreferredWidth(80);  // DNI
        tablaVisitantes.getColumnModel().getColumn(1).setPreferredWidth(120); // Apellido
        tablaVisitantes.getColumnModel().getColumn(2).setPreferredWidth(120); // Nombre
        tablaVisitantes.getColumnModel().getColumn(3).setPreferredWidth(200); // Domicilio
        tablaVisitantes.getColumnModel().getColumn(4).setPreferredWidth(100); // Teléfono
        tablaVisitantes.getColumnModel().getColumn(5).setPreferredWidth(80);  // Estado

        // Doble click para seleccionar
        tablaVisitantes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    seleccionarVisitante();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaVisitantes);
        panelPrincipal.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout());

        JButton btnSeleccionar = new JButton("Seleccionar");
        btnSeleccionar.addActionListener(e -> seleccionarVisitante());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());

        panelBotones.add(btnSeleccionar);
        panelBotones.add(btnCancelar);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private void cargarVisitantes() {
        try {
            List<Visitante> visitantes = controlador.obtenerTodos();
            modeloTabla.setRowCount(0);

            for (Visitante visitante : visitantes) {
                Object[] fila = {
                    visitante.getDni(),
                    visitante.getApellido(),
                    visitante.getNombre(),
                    visitante.getDomicilio(),
                    visitante.getTelefono(),
                    visitante.getEstado()
                };
                modeloTabla.addRow(fila);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar visitantes: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filtrarVisitantes() {
        String textoFiltro = txtFiltro.getText().trim().toLowerCase();

        if (textoFiltro.isEmpty()) {
            cargarVisitantes();
            return;
        }

        try {
            List<Visitante> visitantes = controlador.obtenerTodos();
            modeloTabla.setRowCount(0);

            for (Visitante visitante : visitantes) {
                boolean coincide =
                    visitante.getDni().toLowerCase().contains(textoFiltro) ||
                    visitante.getApellido().toLowerCase().contains(textoFiltro) ||
                    visitante.getNombre().toLowerCase().contains(textoFiltro);

                if (coincide) {
                    Object[] fila = {
                        visitante.getDni(),
                        visitante.getApellido(),
                        visitante.getNombre(),
                        visitante.getDomicilio(),
                        visitante.getTelefono(),
                        visitante.getEstado()
                    };
                    modeloTabla.addRow(fila);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al filtrar visitantes: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarVisitante() {
        int filaSeleccionada = tablaVisitantes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor seleccione un visitante de la lista",
                "Seleccione Visitante",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            List<Visitante> visitantes = controlador.obtenerTodos();
            if (filaSeleccionada < visitantes.size()) {
                visitanteSeleccionado = visitantes.get(filaSeleccionada);
                dispose();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al seleccionar visitante: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public Visitante getVisitanteSeleccionado() {
        return visitanteSeleccionado;
    }

    /**
     * Método estático para mostrar el diálogo y obtener el visitante seleccionado.
     */
    public static Visitante mostrarDialogo(Frame parent) {
        DialogoBusquedaVisitante dialogo = new DialogoBusquedaVisitante(parent);
        dialogo.setVisible(true);
        return dialogo.getVisitanteSeleccionado();
    }
}