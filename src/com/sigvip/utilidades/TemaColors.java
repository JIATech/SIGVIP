package com.sigvip.utilidades;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;

/**
 * Utilidad centralizada para la gestión de colores y temas de la interfaz gráfica.
 * Paleta de colores estrictamente blanco y negro para máxima legibilidad y simplicidad.
 *
 * Diseño minimalista con máximo contraste para mejorar la accesibilidad y usabilidad.
 * Solo se utilizan colores negro (#000000) y blanco (#FFFFFF) en toda la aplicación.
 */
public class TemaColors {

    // ==================== COLORES PRINCIPALES ====================

    /**
     * Color primario del sistema (negro puro)
     * RGB: 0, 0, 0 - Negro puro para máximo contraste
     */
    public static final Color PRIMARIO = Color.BLACK;

    /**
     * Color secundario del sistema (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color SECUNDARIO = Color.BLACK;

    // ==================== COLORES DE BOTONES (TODOS BLANCOS) ====================

    /**
     * Color para todos los botones (blanco puro)
     * RGB: 255, 255, 255 - Blanco con texto negro
     */
    public static final Color BOTON_ACCION = Color.WHITE;
    public static final Color BOTON_CANCELAR = Color.WHITE;
    public static final Color BOTON_PELIGRO = Color.WHITE;
    public static final Color BOTON_EDICION = Color.WHITE;
    public static final Color BOTON_INFO = Color.WHITE;

    // ==================== COLORES DE TEXTO (TODOS NEGROS) ====================

    /**
     * Color de texto primario (negro puro)
     * RGB: 0, 0, 0 - Negro para máximo contraste sobre blanco
     */
    public static final Color TEXTO_PRIMARIO = Color.BLACK;

    /**
     * Color de texto secundario (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color TEXTO_SECUNDARIO = Color.BLACK;

    /**
     * Color de texto (negro)
     * RGB: 0, 0, 0 - Negro sobre cualquier fondo claro
     */
    public static final Color TEXTO_CLARO = Color.BLACK;

    // ==================== COLORES DE FONDO (TODOS BLANCOS) ====================

    /**
     * Color de fondo principal (blanco puro)
     * RGB: 255, 255, 255 - Blanco puro
     */
    public static final Color FONDO_PRINCIPAL = Color.WHITE;

    /**
     * Color de fondo de paneles (blanco)
     * RGB: 255, 255, 255 - Blanco puro
     */
    public static final Color FONDO_PANEL = Color.WHITE;

    /**
     * Color de fondo de encabezados (negro)
     * RGB: 0, 0, 0 - Negro para encabezados con texto blanco
     */
    public static final Color FONDO_ENCABEZADO = Color.BLACK;

    // ==================== COLORES DE ESTADO (TODOS NEGROS) ====================

    /**
     * Color para estado de éxito (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ESTADO_EXITO = Color.BLACK;

    /**
     * Color para estado de error (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ESTADO_ERROR = Color.BLACK;

    /**
     * Color para estado de advertencia (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ESTADO_ADVERTENCIA = Color.BLACK;

    /**
     * Color para estado informativo (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ESTADO_INFO = Color.BLACK;

    // ==================== COLORES PARA ACENTOS ESPECIFICOS (TODOS NEGROS) ====================

    /**
     * Color para botones de autorizaciones (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ACENTO_AUTORIZACIONES = Color.BLACK;

    /**
     * Color para botones de reportes (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ACENTO_REPORTES = Color.BLACK;

    /**
     * Color para botones de gestión de internos (negro)
     * RGB: 0, 0, 0 - Negro para consistencia
     */
    public static final Color ACENTO_INTERNOS = Color.BLACK;

    // ==================== UTILIDADES ====================

    /**
     * Obtiene el color de texto apropiado para un color de fondo dado.
     *
     * @param colorFondo Color de fondo sobre el cual se mostrará el texto
     * @return Color de texto con mejor contraste (blanco o negro)
     */
    public static Color obtenerColorTextoContrastante(Color colorFondo) {
        // Calcular luminosidad relativa del color usando método más simple
        double luminosidad = (0.299 * colorFondo.getRed() +
                            0.587 * colorFondo.getGreen() +
                            0.114 * colorFondo.getBlue()) / 255;

        // Si la luminosidad es mayor a 0.5, usar texto oscuro
        return luminosidad > 0.5 ? TEXTO_PRIMARIO : TEXTO_CLARO;
    }

    /**
     * Calcula la luminosidad relativa de un color según la fórmula WCAG.
     *
     * @param color Color a evaluar
     * @return Luminosidad relativa (0.0 a 1.0)
     */
    private static double calcularLuminosidad(Color color) {
        double r = normalizarComponenteColor(color.getRed());
        double g = normalizarComponenteColor(color.getGreen());
        double b = normalizarComponenteColor(color.getBlue());

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * Normaliza un componente de color (0-255) al rango (0-1).
     */
    private static double normalizarComponenteColor(int componente) {
        double c = componente / 255.0;
        return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    }

    /**
     * Aplica estilo blanco y negro a un botón.
     * Botones blancos con borde negro y texto negro.
     *
     * @param boton Botón a estilizar
     * @param colorFondo Color de fondo del botón (siempre será blanco)
     */
    public static void aplicarEstiloBoton(javax.swing.JButton boton, Color colorFondo) {
        boton.setBackground(Color.WHITE);
        boton.setForeground(Color.BLACK);

        boton.setFocusPainted(false);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);

        // Borde negro bien definido
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        // Fuente audaz para mejor legibilidad
        boton.setFont(new Font("Arial", Font.BOLD, 13));

        // Efectos hover implícitos en Swing
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Forzar repintado para evitar cuadros de rendering
        boton.repaint();
    }

    /**
     * Aplica estilo estándar a un botón de acción.
     */
    public static void aplicarEstiloBotonAccion(javax.swing.JButton boton) {
        aplicarEstiloBoton(boton, BOTON_ACCION);
    }

    /**
     * Aplica estilo estándar a un botón de cancelar.
     */
    public static void aplicarEstiloBotonCancelar(javax.swing.JButton boton) {
        aplicarEstiloBoton(boton, BOTON_CANCELAR);
    }

    /**
     * Aplica estilo estándar a un botón de peligro.
     */
    public static void aplicarEstiloBotonPeligro(javax.swing.JButton boton) {
        aplicarEstiloBoton(boton, BOTON_PELIGRO);
    }

    /**
     * Aplica estilo estándar a un botón de información.
     */
    public static void aplicarEstiloBotonInfo(javax.swing.JButton boton) {
        aplicarEstiloBoton(boton, BOTON_INFO);
    }
}