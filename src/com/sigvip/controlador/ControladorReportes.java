package com.sigvip.controlador;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.TipoReporte;
import com.sigvip.persistencia.*;
import com.sigvip.utilidades.GeneradorReportes;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controlador para generación de reportes con HTML y persistencia en BD.
 * Implementa RF007: Generar Reportes (versión con pantalla + BD).
 *
 * Funcionalidades principales:
 * - Generación de reportes en formato HTML
 * - Persistencia automática en base de datos
 * - Historial de reportes generados
 * - Soporte para impresión directa
 *
 * Enfoque: Reportes en pantalla + guardar en BD para auditoría.
 */
public class ControladorReportes {

    private VisitaDAO visitaDAO;
    private VisitanteDAO visitanteDAO;
    private InternoDAO internoDAO;
    private AutorizacionDAO autorizacionDAO;
    private RestriccionDAO restriccionDAO;
    private UsuarioDAO usuarioDAO;
    private ReporteDAO reporteDAO;
    private GeneradorReportes generadorReportes;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Constructor que inicializa los DAOs y el generador de reportes.
     */
    public ControladorReportes() {
        this.visitaDAO = new VisitaDAO();
        this.visitanteDAO = new VisitanteDAO();
        this.internoDAO = new InternoDAO();
        this.autorizacionDAO = new AutorizacionDAO();
        this.restriccionDAO = new RestriccionDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.reporteDAO = new ReporteDAO();
        this.generadorReportes = new GeneradorReportes();
    }

    // ===== MÉTODOS PRINCIPALES DE GENERACIÓN DE REPORTES =====

    /**
     * Genera reporte de visitas por rango de fechas.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @param estadoFiltro filtro por estado (null o "TODAS" para todos)
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con HTML persistido
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteVisitasPorFecha(Date fechaInicio, Date fechaFin,
                                                        String estadoFiltro, Long idUsuarioGenerador)
            throws SQLException {
        return generadorReportes.generarReporteVisitasPorFecha(fechaInicio, fechaFin, estadoFiltro, idUsuarioGenerador);
    }

    /**
     * Genera reporte de visitas por visitante específico.
     *
     * @param dniVisitante DNI del visitante
     * @param fechaInicio fecha de inicio (opcional)
     * @param fechaFin fecha de fin (opcional)
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con HTML persistido
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteVisitasPorVisitante(String dniVisitante, Date fechaInicio,
                                                            Date fechaFin, Long idUsuarioGenerador)
            throws SQLException {
        return generadorReportes.generarReporteVisitasPorVisitante(dniVisitante, fechaInicio, fechaFin, idUsuarioGenerador);
    }

    /**
     * Genera reporte de visitas por interno específico.
     *
     * @param legajoInterno Legajo del interno
     * @param fechaInicio fecha de inicio (opcional)
     * @param fechaFin fecha de fin (opcional)
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con HTML persistido
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteVisitasPorInterno(String legajoInterno, Date fechaInicio,
                                                          Date fechaFin, Long idUsuarioGenerador)
            throws SQLException {
        return generadorReportes.generarReporteVisitasPorInterno(legajoInterno, fechaInicio, fechaFin, idUsuarioGenerador);
    }

    /**
     * Genera reporte de estadísticas generales de visitas.
     *
     * @param fechaInicio fecha de inicio del período
     * @param fechaFin fecha de fin del período
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con HTML persistido
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteEstadisticas(Date fechaInicio, Date fechaFin,
                                                     Long idUsuarioGenerador) throws SQLException {
        return generadorReportes.generarReporteEstadisticas(fechaInicio, fechaFin, idUsuarioGenerador);
    }

    /**
     * Genera reporte de restricciones activas.
     *
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con HTML persistido
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteRestriccionesActivas(Long idUsuarioGenerador) throws SQLException {
        return generadorReportes.generarReporteRestriccionesActivas(idUsuarioGenerador);
    }

    /**
     * Genera reporte de autorizaciones vigentes.
     *
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con HTML persistido
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteAutorizacionesVigentes(Long idUsuarioGenerador) throws SQLException {
        return generadorReportes.generarReporteAutorizacionesVigentes(idUsuarioGenerador);
    }

    // ===== MÉTODOS DE CONSULTA DE REPORTES =====

    /**
     * Obtiene el historial completo de reportes generados.
     *
     * @return lista de todos los reportes ordenados por fecha descendente
     * @throws SQLException si ocurre error en base de datos
     */
    public List<ReporteGenerado> obtenerHistorialReportes() throws SQLException {
        return reporteDAO.obtenerTodos();
    }

    /**
     * Obtiene reportes generados por un usuario específico.
     *
     * @param idUsuario ID del usuario
     * @return lista de reportes del usuario
     * @throws SQLException si ocurre error en base de datos
     */
    public List<ReporteGenerado> obtenerReportesPorUsuario(Long idUsuario) throws SQLException {
        return reporteDAO.obtenerPorUsuario(idUsuario);
    }

    /**
     * Obtiene reportes por tipo específico.
     *
     * @param tipoReporte tipo de reporte
     * @return lista de reportes de ese tipo
     * @throws SQLException si ocurre error en base de datos
     */
    public List<ReporteGenerado> obtenerReportesPorTipo(TipoReporte tipoReporte) throws SQLException {
        return reporteDAO.obtenerPorTipo(tipoReporte);
    }

    /**
     * Obtiene reportes generados en los últimos N días.
     *
     * @param días número de días hacia atrás
     * @return lista de reportes recientes
     * @throws SQLException si ocurre error en base de datos
     */
    public List<ReporteGenerado> obtenerReportesRecientes(int días) throws SQLException {
        return reporteDAO.obtenerRecientes(días);
    }

    /**
     * Busca un reporte específico por su ID.
     *
     * @param idReporte ID del reporte
     * @return reporte encontrado o null si no existe
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado buscarReportePorId(Long idReporte) throws SQLException {
        return reporteDAO.buscarPorId(idReporte);
    }

    // ===== MÉTODOS DE ESTADÍSTICAS =====

    /**
     * Obtiene estadísticas generales de reportes.
     *
     * @return mapa con estadísticas por tipo de reporte
     * @throws SQLException si ocurre error en base de datos
     */
    public java.util.Map<String, Integer> obtenerEstadisticasReportes() throws SQLException {
        return reporteDAO.obtenerEstadisticasPorTipo();
    }

    /**
     * Cuenta el total de reportes generados.
     *
     * @return número total de reportes
     * @throws SQLException si ocurre error en base de datos
     */
    public int contarTotalReportes() throws SQLException {
        return reporteDAO.contarTotal();
    }

    // ===== MÉTODOS DE MANTENIMIENTO =====

    /**
     * Limpia reportes antiguos para liberar espacio.
     *
     * @param díasAntigüedad eliminar reportes más antiguos que estos días
     * @return número de reportes eliminados
     * @throws SQLException si ocurre error en base de datos
     */
    public int limpiarReportesAntiguos(int díasAntigüedad) throws SQLException {
        return reporteDAO.limpiarAntiguos(díasAntigüedad);
    }

    // ===== MÉTODOS AUXILIARES PARA VISTAS =====

    /**
     * Obtiene las fechas por defecto para reportes (últimos 30 días).
     *
     * @return array con [fechaInicio, fechaFin]
     */
    public Date[] obtenerFechasPorDefecto() {
        Date fechaFin = new Date();
        Date fechaInicio = new Date(fechaFin.getTime() - (30L * 24 * 60 * 60 * 1000)); // 30 días atrás
        return new Date[]{fechaInicio, fechaFin};
    }

    /**
     * Obtiene la lista de visitantes disponibles para filtros.
     *
     * @return lista de todos los visitantes
     * @throws SQLException si ocurre error en base de datos
     */
    public List<Visitante> obtenerVisitantesDisponibles() throws SQLException {
        return visitanteDAO.obtenerTodos();
    }

    /**
     * Obtiene la lista de internos disponibles para filtros.
     *
     * @return lista de todos los internos
     * @throws SQLException si ocurre error en base de datos
     */
    public List<Interno> obtenerInternosDisponibles() throws SQLException {
        return internoDAO.obtenerTodos();
    }

    /**
     * Valida los parámetros de un reporte.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return mensaje de error o null si es válido
     */
    public String validarParametrosReporte(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null) {
            return "La fecha de inicio es obligatoria";
        }
        if (fechaFin == null) {
            return "La fecha de fin es obligatoria";
        }
        if (fechaInicio.after(fechaFin)) {
            return "La fecha de inicio no puede ser posterior a la fecha de fin";
        }

        // Validar que no sea un rango muy grande (más de 1 año)
        long unAnioEnMs = 365L * 24 * 60 * 60 * 1000;
        if (fechaFin.getTime() - fechaInicio.getTime() > unAnioEnMs) {
            return "El rango de fechas no puede superar un año";
        }

        return null; // Válido
    }

    /**
     * Formatea una fecha para mostrar en la interfaz.
     *
     * @param fecha fecha a formatear
     * @return fecha formateada como dd/MM/yyyy
     */
    public String formatearFecha(Date fecha) {
        if (fecha == null) {
            return "";
        }
        return formatoFecha.format(fecha);
    }

    // ===== MÉTODOS HEREDADOS (compatibilidad con versiones anteriores) =====

    /**
     * Genera reporte de visitas por fecha (método de compatibilidad).
     * @deprecated Usar generarReporteVisitasPorFecha con persistencia
     */
    @Deprecated
    public void generarReporteVisitasPorFecha(Date fechaInicio, Date fechaFin) {
        try {
            System.out.println("╔════════════════════════════════════════════════════════╗");
            System.out.println("║        REPORTE DE VISITAS POR PERÍODO                 ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Período: " + formatoFecha.format(fechaInicio) +
                             " al " + formatoFecha.format(fechaFin));

            List<Visita> visitas = visitaDAO.buscarPorRangoFechas(fechaInicio, fechaFin);
            System.out.println("Total de visitas: " + visitas.size());
            System.out.println("════════════════════════════════════════════════════════\n");
        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }
}