package com.sigvip.vista;

import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.Rol;

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
        setTitle("SIGVIP - Sistema de Gestión de Visitas Penitenciarias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
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
        panel.setBackground(new Color(41, 128, 185));
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Título
        JLabel lblTitulo = new JLabel("SIGVIP");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);

        // Subtítulo
        JLabel lblSubtitulo = new JLabel("Sistema de Gestión de Visitas Penitenciarias");
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        lblSubtitulo.setForeground(Color.WHITE);

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
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

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
            new Color(46, 204, 113),
            () -> abrirControlAcceso()
        ), gbc);

        // Botón Registrar Visitante (RF001)
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(crearBotonAccesoDirecto(
            "Registrar Visitante",
            "Dar de alta nuevos visitantes",
            new Color(52, 152, 219),
            () -> abrirRegistroVisitante()
        ), gbc);

        // Botón Autorizaciones (RF002)
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(crearBotonAccesoDirecto(
            "Autorizaciones",
            "Gestionar autorizaciones de visita",
            new Color(155, 89, 182),
            () -> abrirNuevaAutorizacion()
        ), gbc);

        // Botón Reportes (RF007)
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(crearBotonAccesoDirecto(
            "Reportes",
            "Generar e imprimir reportes",
            new Color(230, 126, 34),
            () -> abrirReportes()
        ), gbc);

        return panel;
    }

    /**
     * Crea un botón de acceso directo con estilo personalizado.
     */
    private JButton crearBotonAccesoDirecto(String titulo, String descripcion,
                                           Color color, Runnable accion) {
        JButton boton = new JButton();
        boton.setLayout(new BorderLayout(10, 5));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker(), 2),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Título
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Descripción
        JLabel lblDescripcion = new JLabel(descripcion);
        lblDescripcion.setFont(new Font("Arial", Font.PLAIN, 12));
        lblDescripcion.setForeground(Color.WHITE);
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
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Información del usuario
        lblUsuario = new JLabel("Usuario: " + usuarioActual.getNombreCompleto());
        lblRol = new JLabel("Rol: " + usuarioActual.getRol());
        lblEstablecimiento = new JLabel("Establecimiento: " +
            (usuarioActual.getEstablecimiento() != null ?
             usuarioActual.getEstablecimiento().getNombre() : "N/A"));

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        panelInfo.add(lblUsuario);
        panelInfo.add(new JSeparator(SwingConstants.VERTICAL));
        panelInfo.add(lblRol);
        panelInfo.add(new JSeparator(SwingConstants.VERTICAL));
        panelInfo.add(lblEstablecimiento);

        panel.add(panelInfo, BorderLayout.WEST);

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
                        "2024";

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
