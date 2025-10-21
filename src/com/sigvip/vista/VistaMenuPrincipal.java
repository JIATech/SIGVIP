package com.sigvip.vista;

import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.Rol;
import com.sigvip.persistencia.GestorModo;
import com.sigvip.utilidades.TemaColors;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal del sistema SIGVIP.
 * Proporciona acceso a todas las funcionalidades según el rol del usuario.
 *
 * Funcionalidades:
 * - Menú de navegación completo
 * - Panel de accesos directos (botones grandes)
 * - Barra de estado con información del usuario
 * - Control de permisos por rol
 *
 * Implementa RNF001: Usabilidad - Máximo 4 clicks para cualquier tarea
 */
public class VistaMenuPrincipal extends JFrame {

    private Usuario usuarioActual;
    private JLabel lblUsuario;
    private JLabel lblRol;
    private JLabel lblEstablecimiento;
    private JPanel panelCentral;

    /**
     * Constructor que inicializa la ventana principal.
     *
     * @param usuario usuario autenticado en el sistema
     */
    public VistaMenuPrincipal(Usuario usuario) {
        this.usuarioActual = usuario;
        inicializarComponentes();
        configurarVentana();
        mostrarBienvenida();
    }

    /**
     * Configura las propiedades de la ventana.
     */
    private void configurarVentana() {
        // Agregar indicador de modo offline en el título si aplica
        String titulo = "SIGVIP - Sistema de Gestión de Visitas Penitenciarias";
        if (GestorModo.getInstancia().isModoOffline()) {
            titulo += " [MODO OFFLINE]";
        }

        setTitle(titulo);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 650));
        setLocationRelativeTo(null);
    }

    /**
     * Inicializa y configura todos los componentes de la interfaz.
     */
    private void inicializarComponentes() {
        // Crear barra de menú
        setJMenuBar(crearBarraMenu());

        // Panel principal con BorderLayout
        JPanel panelPrincipal = new JPanel(new BorderLayout());

        // Panel de encabezado
        JPanel panelEncabezado = crearPanelEncabezado();
        panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);

        // Panel central con accesos directos
        panelCentral = crearPanelCentral();
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        // Configurar color de fondo principal
        panelPrincipal.setBackground(TemaColors.FONDO_PRINCIPAL);

        // Barra de estado
        JPanel panelEstado = crearPanelEstado();
        panelPrincipal.add(panelEstado, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    /**
     * Crea la barra de menú principal.
     */
    private JMenuBar crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();

        // Menú Archivo
        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemCerrarSesion = new JMenuItem("Cerrar Sesión");
        itemCerrarSesion.addActionListener(e -> cerrarSesion());
        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> salir());
        menuArchivo.add(itemCerrarSesion);
        menuArchivo.addSeparator();
        menuArchivo.add(itemSalir);

        // Menú Visitantes
        JMenu menuVisitantes = new JMenu("Visitantes");
        JMenuItem itemRegistrarVisitante = new JMenuItem("Registrar Visitante (RF001)");
        itemRegistrarVisitante.addActionListener(e -> abrirRegistroVisitante());
        JMenuItem itemBuscarVisitante = new JMenuItem("Buscar Visitante");
        itemBuscarVisitante.addActionListener(e -> buscarVisitante());
        menuVisitantes.add(itemRegistrarVisitante);
        menuVisitantes.add(itemBuscarVisitante);

        // Menú Autorizaciones
        JMenu menuAutorizaciones = new JMenu("Autorizaciones");
        JMenuItem itemNuevaAutorizacion = new JMenuItem("Nueva Autorización (RF002)");
        itemNuevaAutorizacion.addActionListener(e -> abrirNuevaAutorizacion());
        JMenuItem itemVerAutorizaciones = new JMenuItem("Ver Autorizaciones");
        itemVerAutorizaciones.addActionListener(e -> verAutorizaciones());
        menuAutorizaciones.add(itemNuevaAutorizacion);
        menuAutorizaciones.add(itemVerAutorizaciones);

        // Menú Control de Acceso
        JMenu menuAcceso = new JMenu("Control de Acceso");
        JMenuItem itemRegistrarIngreso = new JMenuItem("Registrar Ingreso (RF003)");
        itemRegistrarIngreso.addActionListener(e -> abrirControlAcceso());
        JMenuItem itemRegistrarEgreso = new JMenuItem("Registrar Egreso (RF004)");
        itemRegistrarEgreso.addActionListener(e -> abrirControlAcceso());
        JMenuItem itemVisitasEnCurso = new JMenuItem("Visitas en Curso");
        itemVisitasEnCurso.addActionListener(e -> verVisitasEnCurso());
        menuAcceso.add(itemRegistrarIngreso);
        menuAcceso.add(itemRegistrarEgreso);
        menuAcceso.addSeparator();
        menuAcceso.add(itemVisitasEnCurso);

        // Menú Internos
        JMenu menuInternos = new JMenu("Internos");
        JMenuItem itemGestionInternos = new JMenuItem("Gestión de Internos (RF006)");
        itemGestionInternos.addActionListener(e -> abrirGestionInternos());
        menuInternos.add(itemGestionInternos);

        // Menú Reportes
        JMenu menuReportes = new JMenu("Reportes");
        JMenuItem itemReportes = new JMenuItem("Generar Reportes (RF007)");
        itemReportes.addActionListener(e -> abrirReportes());
        menuReportes.add(itemReportes);

        // Menú Administración (solo para ADMIN y SUPERVISOR)
        JMenu menuAdmin = new JMenu("Administración");
        if (usuarioActual.getRol() == Rol.ADMINISTRADOR ||
            usuarioActual.getRol() == Rol.SUPERVISOR) {
            JMenuItem itemUsuarios = new JMenuItem("Gestión de Usuarios (RF008)");
            itemUsuarios.addActionListener(e -> gestionarUsuarios());
            JMenuItem itemRestricciones = new JMenuItem("Gestión de Restricciones (RF009)");
            itemRestricciones.addActionListener(e -> gestionarRestricciones());
            menuAdmin.add(itemUsuarios);
            menuAdmin.add(itemRestricciones);
        } else {
            menuAdmin.setEnabled(false);
        }

        // Menú Ayuda
        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de SIGVIP");
        itemAcercaDe.addActionListener(e -> mostrarAcercaDe());
        menuAyuda.add(itemAcercaDe);

        // Agregar menús a la barra
        menuBar.add(menuArchivo);
        menuBar.add(menuVisitantes);
        menuBar.add(menuAutorizaciones);
        menuBar.add(menuAcceso);
        menuBar.add(menuInternos);
        menuBar.add(menuReportes);
        menuBar.add(menuAdmin);
        menuBar.add(menuAyuda);

        return menuBar;
    }

    /**
     * Crea el panel de encabezado con título y logo.
     */
    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel();
        panel.setBackground(TemaColors.FONDO_ENCABEZADO);
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Título
        JLabel lblTitulo = new JLabel("SIGVIP");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE); // Texto blanco sobre fondo negro

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Sistema de Gestión de Visitas Penitenciarias");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitulo.setForeground(Color.WHITE); // Texto blanco sobre fondo negro

        // Panel de títulos
        JPanel panelTitulos = new JPanel(new GridLayout(2, 1));
        panelTitulos.setOpaque(false);
        panelTitulos.add(lblTitulo);
        panelTitulos.add(lblSubtitulo);

        panel.add(panelTitulos, BorderLayout.WEST);

        return panel;
    }

    /**
     * Crea el panel central con accesos directos a las funciones principales.
     */
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TemaColors.PRIMARIO, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        panel.setBackground(TemaColors.FONDO_PANEL);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Botón Control de Acceso (más importante - RF003/RF004)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(crearBotonAccesoDirecto(
            "Control de Acceso",
            "Registrar ingreso y egreso de visitantes",
            TemaColors.BOTON_ACCION,
            () -> abrirControlAcceso()
        ), gbc);

        // Botón Registrar Visitante (RF001)
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(crearBotonAccesoDirecto(
            "Registrar Visitante",
            "Dar de alta nuevos visitantes",
            TemaColors.BOTON_ACCION,
            () -> abrirRegistroVisitante()
        ), gbc);

        // Botón Autorizaciones (RF002)
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(crearBotonAccesoDirecto(
            "Autorizaciones",
            "Gestionar autorizaciones de visita",
            TemaColors.BOTON_ACCION,
            () -> abrirNuevaAutorizacion()
        ), gbc);

        // Botón Reportes (RF007)
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(crearBotonAccesoDirecto(
            "Reportes",
            "Generar e imprimir reportes",
            TemaColors.BOTON_ACCION,
            () -> abrirReportes()
        ), gbc);

        return panel;
    }

    /**
     * Crea un botón de acceso directo con estilo blanco y negro.
     */
    private JButton crearBotonAccesoDirecto(String titulo, String descripcion,
                                           Color color, Runnable accion) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(10, 5));
        boton.setBackground(Color.WHITE); // Fondo blanco
        boton.setForeground(Color.BLACK); // Texto negro
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2), // Borde negro
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        // Título
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.BLACK); // Texto negro
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Descripción
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDescripcion.setForeground(Color.BLACK); // Texto negro
        lblDescripcion.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel con texto
        JPanel panelTexto = new JPanel(new GridLayout(2, 1, 0, 5));
        panelTexto.setOpaque(false);
        panelTexto.add(lblTitulo);
        panelTexto.add(lblDescripcion);

        boton.add(panelTexto, BorderLayout.CENTER);

        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });

        boton.addActionListener(e -> accion.run());

        return boton;
    }

    /**
     * Crea la barra de estado con información del usuario.
     */
    private JPanel crearPanelEstado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        panel.setBackground(TemaColors.FONDO_PANEL);

        // Información del usuario con mejor espaciado
        lblUsuario = new JLabel("Usuario: " + usuarioActual.getNombreCompleto());
        lblUsuario.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUsuario.setForeground(TemaColors.TEXTO_PRIMARIO);

        lblRol = new JLabel("Rol: " + usuarioActual.getRol());
        lblRol.setFont(new Font("Arial", Font.PLAIN, 12));
        lblRol.setForeground(TemaColors.TEXTO_PRIMARIO);

        lblEstablecimiento = new JLabel("Establecimiento: " +
            (usuarioActual.getEstablecimiento() != null ?
             usuarioActual.getEstablecimiento().getNombre() : "N/A"));
        lblEstablecimiento.setFont(new Font("Arial", Font.PLAIN, 12));
        lblEstablecimiento.setForeground(TemaColors.TEXTO_PRIMARIO);

        // Usar BoxLayout para mejor distribución del espacio
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.X_AXIS));
        panelInfo.setBackground(TemaColors.FONDO_PANEL);

        panelInfo.add(lblUsuario);
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(new JSeparator(SwingConstants.VERTICAL));
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(lblRol);
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(new JSeparator(SwingConstants.VERTICAL));
        panelInfo.add(Box.createHorizontalStrut(20));
        panelInfo.add(lblEstablecimiento);
        panelInfo.add(Box.createHorizontalGlue()); // Espacio flexible

        panel.add(panelInfo, BorderLayout.CENTER);

        // Indicador de modo (ONLINE/OFFLINE) más visible
        JPanel panelModo = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelModo.setBackground(TemaColors.FONDO_PANEL);
        JLabel lblModo;

        if (GestorModo.getInstancia().isModoOffline()) {
            lblModo = new JLabel("MODO OFFLINE - Datos no persistentes");
            lblModo.setForeground(TemaColors.ESTADO_ERROR);
            lblModo.setFont(new Font("Arial", Font.BOLD, 12));
        } else {
            lblModo = new JLabel("CONECTADO a MySQL");
            lblModo.setForeground(TemaColors.ESTADO_EXITO);
            lblModo.setFont(new Font("Arial", Font.BOLD, 12));
        }

        panelModo.add(lblModo);
        panel.add(panelModo, BorderLayout.EAST);

        return panel;
    }

    /**
     * Muestra mensaje de bienvenida.
     */
    private void mostrarBienvenida() {
        String mensaje = "Bienvenido/a " + usuarioActual.getNombreCompleto() + "\n\n" +
                        "Rol: " + usuarioActual.getRol() + "\n" +
                        "Último acceso: " + usuarioActual.getUltimoAcceso();

        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Bienvenido a SIGVIP",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    // ===== MÉTODOS DE NAVEGACIÓN =====

    private void abrirControlAcceso() {
        VistaControlAcceso vista = new VistaControlAcceso(usuarioActual);
        vista.setVisible(true);
    }

    private void abrirRegistroVisitante() {
        VistaRegistroVisitante vista = new VistaRegistroVisitante(usuarioActual);
        vista.setVisible(true);
    }

    private void abrirNuevaAutorizacion() {
        VistaAutorizaciones vista = new VistaAutorizaciones(usuarioActual);
        vista.setVisible(true);
    }

    private void abrirGestionInternos() {
        VistaGestionInternos vista = new VistaGestionInternos(usuarioActual);
        vista.setVisible(true);
    }

    private void abrirReportes() {
        VistaReportes vista = new VistaReportes(usuarioActual);
        vista.setVisible(true);
    }

    private void buscarVisitante() {
        JOptionPane.showMessageDialog(this, "Funcionalidad en desarrollo");
    }

    private void verAutorizaciones() {
        // Reutiliza la misma vista que incluye tanto formulario como listado
        VistaAutorizaciones vista = new VistaAutorizaciones(usuarioActual);
        vista.setVisible(true);
    }

    private void verVisitasEnCurso() {
        JOptionPane.showMessageDialog(this, "Funcionalidad en desarrollo");
    }

    private void gestionarUsuarios() {
        VistaGestionUsuarios vista = new VistaGestionUsuarios(usuarioActual);
        vista.setVisible(true);
    }

    private void gestionarRestricciones() {
        JOptionPane.showMessageDialog(this, "Funcionalidad en desarrollo");
    }

    private void mostrarAcercaDe() {
        String mensaje = "SIGVIP - Sistema Integral de Gestión de Visitas Penitenciarias\n\n" +
                        "Versión 1.0\n\n" +
                        "Desarrollado como proyecto académico\n" +
                        "Universidad Siglo 21 - INF275\n" +
                        "Seminario de Práctica de Informática\n\n" +
                        "Estudiante: Arnaboldi, Juan Ignacio\n" +
                        "Profesor: Marcos Darío Aranda\n\n" +
                        "2025";

        JOptionPane.showMessageDialog(
            this,
            mensaje,
            "Acerca de SIGVIP",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea cerrar sesión?",
            "Cerrar Sesión",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                VistaLogin login = new VistaLogin();
                login.setVisible(true);
            });
        }
    }

    private void salir() {
        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "¿Está seguro que desea salir del sistema?",
            "Salir",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }
}
