package com.sigvip.vista;

import com.sigvip.controlador.ControladorVisitantes;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.EstadoVisitante;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Vista para registro de nuevos visitantes en el sistema.
 * Implementa RF001: Registrar Visitante.
 *
 * Funcionalidades:
 * - Alta de visitantes con validación de datos
 * - Validación de DNI único
 * - Validación de edad mínima (18 años)
 * - Campos obligatorios y opcionales
 *
 * Especificación: PDF Sección 7.1 (RF001)
 */
public class VistaRegistroVisitante extends JFrame {

    private Usuario usuarioActual;
    private ControladorVisitantes controlador;

    // Componentes del formulario
    private JTextField txtDni;
    private JTextField txtApellido;
    private JTextField txtNombre;
    private JTextField txtFechaNacimiento; // Formato dd/MM/yyyy
    private JTextField txtTelefono;
    private JTextField txtDomicilio;
    private JComboBox<EstadoVisitante> cboEstado;
    private JButton btnGuardar;
    private JButton btnLimpiar;
    private JButton btnCerrar;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor que inicializa la vista.
     *
     * @param usuario usuario autenticado
     */
    public VistaRegistroVisitante(Usuario usuario) {
        this.usuarioActual = usuario;
        this.controlador = new ControladorVisitantes();
        formatoFecha.setLenient(false); // Validación estricta de fechas
        inicializarComponentes();
        configurarVentana();
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        setTitle("SIGVIP - Registrar Visitante (RF001)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Inicializa todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Panel de información
        JPanel panelInfo = crearPanelInformacion();
        panelPrincipal.add(panelInfo, BorderLayout.NORTH);

        // Panel de formulario
        JPanel panelFormulario = crearPanelFormulario();
        panelPrincipal.add(panelFormulario, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = crearPanelBotones();
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    /**
     * Crea el panel de información superior.
     */
    private JPanel crearPanelInformacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(new Color(232, 243, 251));

        JTextArea txtInfo = new JTextArea(
            "REGISTRO DE NUEVO VISITANTE (RF001)\n\n" +
            "Requisitos:\n" +
            "• El visitante debe ser mayor de 18 años\n" +
            "• El DNI debe ser único en el sistema\n" +
            "• Los campos marcados con (*) son obligatorios"
        );
        txtInfo.setEditable(false);
        txtInfo.setOpaque(false);
        txtInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        panel.add(txtInfo, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Crea el panel del formulario.
     */
    private JPanel crearPanelFormulario() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Datos del Visitante"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int fila = 0;

        // DNI (*)
        agregarCampo(panel, gbc, fila++, "DNI: *",
            txtDni = crearTextField(20, "Ingrese DNI sin puntos (ej: 12345678)"));

        // Apellido (*)
        agregarCampo(panel, gbc, fila++, "Apellido: *",
            txtApellido = crearTextField(30, "Apellido del visitante"));

        // Nombre (*)
        agregarCampo(panel, gbc, fila++, "Nombre: *",
            txtNombre = crearTextField(30, "Nombre del visitante"));

        // Fecha de Nacimiento (*)
        agregarCampo(panel, gbc, fila++, "Fecha Nacimiento: *",
            txtFechaNacimiento = crearTextField(20, "Formato: dd/MM/yyyy (ej: 15/03/1990)"));

        // Teléfono
        agregarCampo(panel, gbc, fila++, "Teléfono:",
            txtTelefono = crearTextField(20, "Número de teléfono"));

        // Domicilio
        agregarCampo(panel, gbc, fila++, "Domicilio:",
            txtDomicilio = crearTextField(40, "Dirección completa"));

        // Estado
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.3;
        panel.add(new JLabel("Estado:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        cboEstado = new JComboBox<>(EstadoVisitante.values());
        cboEstado.setSelectedItem(EstadoVisitante.ACTIVO);
        panel.add(cboEstado, gbc);

        return panel;
    }

    /**
     * Agrega un campo al formulario.
     */
    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila,
                             String etiqueta, JComponent campo) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        gbc.weightx = 0.3;
        panel.add(new JLabel(etiqueta), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(campo, gbc);
    }

    /**
     * Crea un campo de texto con tooltip.
     */
    private JTextField crearTextField(int columnas, String tooltip) {
        JTextField txt = new JTextField(columnas);
        txt.setToolTipText(tooltip);
        return txt;
    }

    /**
     * Crea el panel de botones.
     */
    private JPanel crearPanelBotones() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setPreferredSize(new Dimension(100, 30));
        btnLimpiar.addActionListener(e -> limpiarFormulario());

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setPreferredSize(new Dimension(100, 30));
        btnCerrar.addActionListener(e -> dispose());

        btnGuardar = new JButton("Guardar");
        btnGuardar.setPreferredSize(new Dimension(100, 30));
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarVisitante());

        panel.add(btnLimpiar);
        panel.add(btnCerrar);
        panel.add(btnGuardar);

        return panel;
    }

    /**
     * Valida y guarda un nuevo visitante.
     * Implementa RF001 con todas sus validaciones.
     */
    private void guardarVisitante() {
        try {
            // Validar campos obligatorios
            if (!validarCamposObligatorios()) {
                return;
            }

            // Crear objeto Visitante
            Visitante visitante = new Visitante();
            visitante.setDni(txtDni.getText().trim());
            visitante.setApellido(txtApellido.getText().trim().toUpperCase());
            visitante.setNombre(txtNombre.getText().trim().toUpperCase());

            // Parsear y validar fecha de nacimiento
            try {
                Date fechaNac = formatoFecha.parse(txtFechaNacimiento.getText().trim());
                visitante.setFechaNacimiento(fechaNac);
            } catch (ParseException e) {
                mostrarError("Formato de fecha inválido.\nUse dd/MM/yyyy (ej: 15/03/1990)");
                txtFechaNacimiento.requestFocus();
                return;
            }

            // Validar edad mínima (18 años) - RF001
            if (!visitante.esMayorDeEdad()) {
                mostrarError("El visitante debe ser mayor de 18 años.\n" +
                           "Edad actual: " + visitante.calcularEdad() + " años");
                return;
            }

            // Campos opcionales
            String telefono = txtTelefono.getText().trim();
            if (!telefono.isEmpty()) {
                visitante.setTelefono(telefono);
            }

            String domicilio = txtDomicilio.getText().trim();
            if (!domicilio.isEmpty()) {
                visitante.setDomicilio(domicilio);
            }

            visitante.setEstado((EstadoVisitante) cboEstado.getSelectedItem());
            visitante.setFechaRegistro(new Date());

            // Deshabilitar botón durante guardado
            btnGuardar.setEnabled(false);
            btnGuardar.setText("Guardando...");

            // Guardar en base de datos
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return controlador.registrarVisitante(visitante);
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            mostrarExito(
                                "Visitante registrado exitosamente\n\n" +
                                "DNI: " + visitante.getDni() + "\n" +
                                "Nombre: " + visitante.getNombreCompleto()
                            );
                            limpiarFormulario();
                        } else {
                            mostrarError(
                                "No se pudo registrar el visitante.\n" +
                                "El DNI podría estar duplicado.\n" +
                                "Verifique la consola para más detalles."
                            );
                        }
                    } catch (Exception e) {
                        mostrarError("Error al guardar: " + e.getMessage());
                        e.printStackTrace();
                    } finally {
                        btnGuardar.setEnabled(true);
                        btnGuardar.setText("Guardar");
                    }
                }
            };

            worker.execute();

        } catch (Exception e) {
            mostrarError("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Valida que los campos obligatorios estén completos.
     */
    private boolean validarCamposObligatorios() {
        if (txtDni.getText().trim().isEmpty()) {
            mostrarError("El DNI es obligatorio");
            txtDni.requestFocus();
            return false;
        }

        if (txtApellido.getText().trim().isEmpty()) {
            mostrarError("El apellido es obligatorio");
            txtApellido.requestFocus();
            return false;
        }

        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            txtNombre.requestFocus();
            return false;
        }

        if (txtFechaNacimiento.getText().trim().isEmpty()) {
            mostrarError("La fecha de nacimiento es obligatoria");
            txtFechaNacimiento.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Limpia todos los campos del formulario.
     */
    private void limpiarFormulario() {
        txtDni.setText("");
        txtApellido.setText("");
        txtNombre.setText("");
        txtFechaNacimiento.setText("");
        txtTelefono.setText("");
        txtDomicilio.setText("");
        cboEstado.setSelectedItem(EstadoVisitante.ACTIVO);
        txtDni.requestFocus();
    }

    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Error de Validación",
            JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Muestra un mensaje de éxito.
     */
    private void mostrarExito(String mensaje) {
        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Registro Exitoso",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
