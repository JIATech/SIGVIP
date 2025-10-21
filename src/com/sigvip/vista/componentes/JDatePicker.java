package com.sigvip.vista.componentes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Selector de fecha personalizado implementado con Swing puro.
 * Permite seleccionar fechas mediante un calendario visual sin librerías externas.
 *
 * Funcionalidades:
 * - Selección de fecha mediante calendario
 * - Navegación por meses/años
 * - Validación automática de formato dd/MM/yyyy
 * - Botón para mostrar/ocultar calendario
 */
public class JDatePicker extends JPanel {

    private static final long serialVersionUID = 1L;

    // Componentes principales
    private JTextField txtFecha;
    private JButton btnCalendario;
    private JDialog dialogoCalendario;
    private JPanel panelCalendario;

    // Formato y estado
    private SimpleDateFormat formatoFecha;
    private Calendar fechaSeleccionada;

    // Navegación del calendario
    private Calendar calendarioActual;
    private JLabel lblMesAnio;
    private JPanel panelDias;
    private JButton[][] btnDias = new JButton[6][7];

    // Colores (blanco y negro)
    private static final Color COLOR_FONDO = Color.WHITE;
    private static final Color COLOR_TEXTO = Color.BLACK;
    private static final Color COLOR_BOTON = Color.WHITE;
    private static final Color COLOR_SELECCIONADO = Color.LIGHT_GRAY;
    private static final Color COLOR_HOY = new Color(230, 230, 230);

    /**
     * Constructor por defecto.
     */
    public JDatePicker() {
        this(new Date());
    }

    /**
     * Constructor con fecha inicial.
     *
     * @param fechaInicial fecha inicial a mostrar
     */
    public JDatePicker(Date fechaInicial) {
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
        this.fechaSeleccionada = Calendar.getInstance();
        this.calendarioActual = Calendar.getInstance();

        if (fechaInicial != null) {
            this.fechaSeleccionada.setTime(fechaInicial);
            this.calendarioActual.setTime(fechaInicial);
        }

        inicializarComponentes();
        configurarEventos();
    }

    /**
     * Inicializa todos los componentes del selector.
     */
    private void inicializarComponentes() {
        setLayout(new BorderLayout());
        setBackground(COLOR_FONDO);

        // Panel principal con campo de texto y botón
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 0));
        panelPrincipal.setBackground(COLOR_FONDO);

        // Campo de texto para mostrar la fecha
        txtFecha = new JTextField();
        txtFecha.setEditable(true); // Permitir entrada rápida
        txtFecha.setBackground(COLOR_FONDO);
        txtFecha.setForeground(COLOR_TEXTO);
        txtFecha.setBorder(BorderFactory.createLineBorder(COLOR_TEXTO));
        txtFecha.setFont(new Font("Arial", Font.PLAIN, 12));
        txtFecha.setToolTipText("Escriba fecha como DD/MM/YYYY o números seguidos: 25121991 → 25/12/1991");
        actualizarCampoFecha();

        // Botón para abrir calendario
        btnCalendario = new JButton("Cal");
        btnCalendario.setPreferredSize(new Dimension(35, 25));
        btnCalendario.setBackground(COLOR_BOTON);
        btnCalendario.setForeground(COLOR_TEXTO);
        btnCalendario.setBorder(BorderFactory.createLineBorder(COLOR_TEXTO));
        btnCalendario.setFont(new Font("Arial", Font.BOLD, 11));
        btnCalendario.setToolTipText("Seleccionar fecha");

        panelPrincipal.add(txtFecha, BorderLayout.CENTER);
        panelPrincipal.add(btnCalendario, BorderLayout.EAST);

        add(panelPrincipal, BorderLayout.CENTER);

        // Crear diálogo del calendario (inicialmente oculto)
        crearDialogoCalendario();
    }

    /**
     * Crea el diálogo con el calendario.
     */
    private void crearDialogoCalendario() {
        dialogoCalendario = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                       "Seleccionar Fecha", true);
        dialogoCalendario.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        dialogoCalendario.setSize(280, 220);
        dialogoCalendario.setResizable(false);

        panelCalendario = new JPanel(new BorderLayout());
        panelCalendario.setBackground(COLOR_FONDO);
        panelCalendario.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel de navegación (mes/año)
        JPanel panelNavegacion = crearPanelNavegacion();
        panelCalendario.add(panelNavegacion, BorderLayout.NORTH);

        // Panel de días
        panelDias = crearPanelDias();
        panelCalendario.add(panelDias, BorderLayout.CENTER);

        dialogoCalendario.add(panelCalendario);
        actualizarCalendario();
    }

    /**
     * Crea el panel de navegación del calendario.
     */
    private JPanel crearPanelNavegacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);

        // Botón mes anterior
        JButton btnAnterior = new JButton("◀");
        btnAnterior.setBackground(COLOR_BOTON);
        btnAnterior.setForeground(COLOR_TEXTO);
        btnAnterior.setBorder(BorderFactory.createLineBorder(COLOR_TEXTO));
        btnAnterior.setPreferredSize(new Dimension(30, 25));
        btnAnterior.addActionListener(e -> {
            calendarioActual.add(Calendar.MONTH, -1);
            actualizarCalendario();
        });

        // Botón mes siguiente
        JButton btnSiguiente = new JButton("▶");
        btnSiguiente.setBackground(COLOR_BOTON);
        btnSiguiente.setForeground(COLOR_TEXTO);
        btnSiguiente.setBorder(BorderFactory.createLineBorder(COLOR_TEXTO));
        btnSiguiente.setPreferredSize(new Dimension(30, 25));
        btnSiguiente.addActionListener(e -> {
            calendarioActual.add(Calendar.MONTH, 1);
            actualizarCalendario();
        });

        // Etiqueta con mes y año
        lblMesAnio = new JLabel();
        lblMesAnio.setHorizontalAlignment(SwingConstants.CENTER);
        lblMesAnio.setFont(new Font("Arial", Font.BOLD, 12));
        lblMesAnio.setForeground(COLOR_TEXTO);

        panel.add(btnAnterior, BorderLayout.WEST);
        panel.add(lblMesAnio, BorderLayout.CENTER);
        panel.add(btnSiguiente, BorderLayout.EAST);

        return panel;
    }

    /**
     * Crea el panel con los días del calendario.
     */
    private JPanel crearPanelDias() {
        JPanel panel = new JPanel(new GridLayout(7, 7, 2, 2));
        panel.setBackground(COLOR_FONDO);

        // Días de la semana (cabecera)
        String[] diasSemana = {"Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb"};
        for (String dia : diasSemana) {
            JLabel lblDia = new JLabel(dia, SwingConstants.CENTER);
            lblDia.setFont(new Font("Arial", Font.BOLD, 10));
            lblDia.setForeground(COLOR_TEXTO);
            panel.add(lblDia);
        }

        // Botones para los días
        for (int fila = 0; fila < 6; fila++) {
            for (int col = 0; col < 7; col++) {
                btnDias[fila][col] = new JButton();
                btnDias[fila][col].setBackground(COLOR_BOTON);
                btnDias[fila][col].setForeground(COLOR_TEXTO);
                btnDias[fila][col].setBorder(BorderFactory.createLineBorder(COLOR_TEXTO));
                btnDias[fila][col].setFont(new Font("Arial", Font.PLAIN, 10));
                btnDias[fila][col].setPreferredSize(new Dimension(25, 25));

                final int f = fila;
                final int c = col;
                btnDias[fila][col].addActionListener(e -> seleccionarDia(f, c));

                panel.add(btnDias[fila][col]);
            }
        }

        return panel;
    }

    /**
     * Actualiza la visualización del calendario.
     */
    private void actualizarCalendario() {
        // Actualizar etiqueta de mes/año
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                         "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        lblMesAnio.setText(meses[calendarioActual.get(Calendar.MONTH)] + " " +
                          calendarioActual.get(Calendar.YEAR));

        // Obtener primer día del mes
        Calendar temp = (Calendar) calendarioActual.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);

        int primerDiaSemana = temp.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Domingo
        int ultimoDiaMes = temp.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Limpiar botones
        for (int fila = 0; fila < 6; fila++) {
            for (int col = 0; col < 7; col++) {
                btnDias[fila][col].setText("");
                btnDias[fila][col].setEnabled(false);
                btnDias[fila][col].setBackground(COLOR_BOTON);
            }
        }

        // Llenar días del mes
        int dia = 1;
        for (int fila = 0; fila < 6 && dia <= ultimoDiaMes; fila++) {
            for (int col = 0; col < 7 && dia <= ultimoDiaMes; col++) {
                if (fila == 0 && col < primerDiaSemana) {
                    // Días vacíos antes del primer día del mes
                    continue;
                }

                JButton btnDia = btnDias[fila][col];
                btnDia.setText(String.valueOf(dia));
                btnDia.setEnabled(true);

                // Resaltar hoy
                Calendar hoy = Calendar.getInstance();
                if (calendarioActual.get(Calendar.YEAR) == hoy.get(Calendar.YEAR) &&
                    calendarioActual.get(Calendar.MONTH) == hoy.get(Calendar.MONTH) &&
                    dia == hoy.get(Calendar.DAY_OF_MONTH)) {
                    btnDia.setBackground(COLOR_HOY);
                }

                // Resaltar fecha seleccionada
                if (calendarioActual.get(Calendar.YEAR) == fechaSeleccionada.get(Calendar.YEAR) &&
                    calendarioActual.get(Calendar.MONTH) == fechaSeleccionada.get(Calendar.MONTH) &&
                    dia == fechaSeleccionada.get(Calendar.DAY_OF_MONTH)) {
                    btnDia.setBackground(COLOR_SELECCIONADO);
                }

                dia++;
            }
        }
    }

    /**
     * Maneja la selección de un día específico.
     */
    private void seleccionarDia(int fila, int col) {
        JButton btnDia = btnDias[fila][col];
        String textoDia = btnDia.getText();

        if (!textoDia.isEmpty()) {
            int dia = Integer.parseInt(textoDia);

            // Actualizar fecha seleccionada
            fechaSeleccionada = (Calendar) calendarioActual.clone();
            fechaSeleccionada.set(Calendar.DAY_OF_MONTH, dia);

            // Actualizar campo de texto
            actualizarCampoFecha();

            // Cerrar diálogo
            dialogoCalendario.setVisible(false);

            // Notificar cambio
            firePropertyChange("fecha", null, getFecha());
        }
    }

    /**
     * Actualiza el campo de texto con la fecha seleccionada.
     */
    private void actualizarCampoFecha() {
        txtFecha.setText(formatoFecha.format(fechaSeleccionada.getTime()));
    }

    /**
     * Procesa la entrada rápida de números en formato DDMMYYYY.
     * Convierte automáticamente: 25121991 → 25/12/1991
     * También soporta formatos intermedios: 2512 → 25/12/, 251220 → 25/12/2020
     */
    private void procesarEntradaRapida() {
        String texto = txtFecha.getText().trim();

        // Eliminar cualquier barra existente para procesar solo números
        String soloNumeros = texto.replaceAll("[^0-9]", "");

        // Procesar según la longitud de números ingresados
        String textoFormateado = texto;
        boolean actualizarFecha = false;

        switch (soloNumeros.length()) {
            case 0:
            case 1:
            case 2:
                // No formatear aún
                break;

            case 3:
            case 4:
                // Formato DD/MM/
                if (soloNumeros.length() == 4) {
                    int dia = Integer.parseInt(soloNumeros.substring(0, 2));
                    int mes = Integer.parseInt(soloNumeros.substring(2, 4));

                    if (mes >= 1 && mes <= 12) {
                        textoFormateado = String.format("%02d/%02d/", dia, mes);
                    }
                }
                break;

            case 5:
            case 6:
                // Formato DD/MM/YY (asumir 2000s)
                if (soloNumeros.length() == 6) {
                    int dia = Integer.parseInt(soloNumeros.substring(0, 2));
                    int mes = Integer.parseInt(soloNumeros.substring(2, 4));
                    int anio = Integer.parseInt(soloNumeros.substring(4, 6));

                    if (dia >= 1 && dia <= 31 && mes >= 1 && mes <= 12) {
                        // Asumir años 2000-2099 para números de 2 dígitos
                        int anioCompleto = (anio < 50) ? 2000 + anio : 1900 + anio;
                        textoFormateado = String.format("%02d/%02d/%04d", dia, mes, anioCompleto);
                        actualizarFecha = true;
                    }
                }
                break;

            case 8:
                // Formato completo DDMMYYYY → DD/MM/YYYY
                try {
                    int dia = Integer.parseInt(soloNumeros.substring(0, 2));
                    int mes = Integer.parseInt(soloNumeros.substring(2, 4));
                    int anio = Integer.parseInt(soloNumeros.substring(4, 8));

                    // Validar rango de valores
                    if (dia >= 1 && dia <= 31 && mes >= 1 && mes <= 12 && anio >= 1900 && anio <= 2100) {
                        textoFormateado = String.format("%02d/%02d/%04d", dia, mes, anio);
                        actualizarFecha = true;
                    }
                } catch (Exception e) {
                    // Ignorar errores de parseo
                }
                break;
        }

        // Actualizar el campo de texto si cambió
        if (!texto.equals(textoFormateado)) {
            txtFecha.setText(textoFormateado);
            // Posicionar cursor al final
            txtFecha.setCaretPosition(textoFormateado.length());
        }

        // Si tenemos una fecha completa, actualizarla internamente
        if (actualizarFecha && setFechaTexto(textoFormateado)) {
            firePropertyChange("fecha", null, getFecha());
        }
    }

    /**
     * Valida y actualiza la fecha desde el texto cuando el componente pierde el foco.
     */
    private void validarYActualizarDesdeTexto() {
        String texto = txtFecha.getText().trim();

        // Si está vacío, usar fecha actual
        if (texto.isEmpty()) {
            limpiar();
            return;
        }

        // Intentar parsear el texto actual
        if (!setFechaTexto(texto)) {
            // Si no es válido, restaurar la fecha formateada actual
            actualizarCampoFecha();
        }
    }

    /**
     * Configura los eventos del componente.
     */
    private void configurarEventos() {
        btnCalendario.addActionListener(e -> {
            // Posicionar diálogo cerca del componente
            Point ubicacion = getLocationOnScreen();
            dialogoCalendario.setLocation(ubicacion.x, ubicacion.y + getHeight());

            // Actualizar calendario con la fecha actual
            calendarioActual = (Calendar) fechaSeleccionada.clone();
            actualizarCalendario();

            // Mostrar diálogo
            dialogoCalendario.setVisible(true);
        });

        // Listener para entrada rápida de fechas
        txtFecha.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                // Solo procesar si no está en el diálogo del calendario
                if (!dialogoCalendario.isVisible()) {
                    procesarEntradaRapida();
                }
            }
        });

        // Listener para perder el foco y validar
        txtFecha.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                validarYActualizarDesdeTexto();
            }
        });
    }

    // ===== MÉTODOS PÚBLICOS =====

    /**
     * Obtiene la fecha seleccionada.
     *
     * @return fecha seleccionada o null si no hay selección
     */
    public Date getFecha() {
        return fechaSeleccionada.getTime();
    }

    /**
     * Establece la fecha seleccionada.
     *
     * @param fecha fecha a establecer
     */
    public void setFecha(Date fecha) {
        if (fecha != null) {
            this.fechaSeleccionada.setTime(fecha);
            this.calendarioActual.setTime(fecha);
            actualizarCampoFecha();
        }
    }

    /**
     * Obtiene la fecha como texto en formato dd/MM/yyyy.
     *
     * @return fecha formateada
     */
    public String getFechaTexto() {
        return txtFecha.getText();
    }

    /**
     * Establece la fecha desde texto en formato dd/MM/yyyy.
     *
     * @param fechaTexto fecha en formato dd/MM/yyyy
     * @return true si se pudo establecer, false si el formato es inválido
     */
    public boolean setFechaTexto(String fechaTexto) {
        try {
            Date fecha = formatoFecha.parse(fechaTexto);
            setFecha(fecha);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Limpia la selección (establece fecha actual).
     */
    public void limpiar() {
        setFecha(new Date());
    }

    /**
     * Habilita o deshabilita el componente.
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        txtFecha.setEnabled(enabled);
        btnCalendario.setEnabled(enabled);
    }
}