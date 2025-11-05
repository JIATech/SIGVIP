package com.sigvip.utilidades;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.TipoReporte;
import com.sigvip.persistencia.*;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generador de reportes en formato HTML/Texto con persistencia en base de datos.
 * Implementa RF007: Generar Reportes (versión sin PDF).
 *
 * Enfoque: Reporte en pantalla + guardar en BD para auditoría.
 *
 * Ventajas:
 * - Sin dependencias externas (cumple restricción académica)
 * - Reporte visible inmediatamente en pantalla
 * - Impresión directa compatible con impresoras térmicas
 * - Auditoría completa en base de datos
 */
public class GeneradorReportes {

    private VisitaDAO visitaDAO;
    private VisitanteDAO visitanteDAO;
    private InternoDAO internoDAO;
    private AutorizacionDAO autorizacionDAO;
    private RestriccionDAO restriccionDAO;
    private ReporteDAO reporteDAO;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatoCompleto = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Constructor que inicializa todos los DAOs necesarios.
     */
    public GeneradorReportes() {
        this.visitaDAO = new VisitaDAO();
        this.visitanteDAO = new VisitanteDAO();
        this.internoDAO = new InternoDAO();
        this.autorizacionDAO = new AutorizacionDAO();
        this.restriccionDAO = new RestriccionDAO();
        this.reporteDAO = new ReporteDAO();
    }

    /**
     * Genera reporte de visitas por rango de fechas y lo persiste.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @param estadoFiltro filtro por estado (null o "TODAS" para todos)
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con ID
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteVisitasPorFecha(Date fechaInicio, Date fechaFin,
                                                       String estadoFiltro, Long idUsuarioGenerador)
            throws SQLException {

        // Obtener datos
        List<Visita> visitas = visitaDAO.buscarPorRangoFechas(fechaInicio, fechaFin);

        // Aplicar filtros
        if (estadoFiltro != null && !estadoFiltro.equals("TODAS")) {
            visitas = visitas.stream()
                .filter(v -> v.getEstadoVisita().name().equals(estadoFiltro))
                .collect(Collectors.toList());
        }

        // Generar contenido HTML
        String contenido = generarHTMLVisitasPorFecha(visitas, fechaInicio, fechaFin, estadoFiltro);

        // Crear objeto reporte
        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(TipoReporte.VISITAS_FECHA);
        reporte.setTitulo("Reporte de Visitas por Fecha");
        reporte.setParametrosFiltro(generarJSONFiltrosFecha(fechaInicio, fechaFin, estadoFiltro));
        reporte.setContenido(contenido);
        reporte.setTotalRegistros(visitas.size());
        reporte.setIdGeneradoPor(idUsuarioGenerador);

        // Persistir reporte
        Long idReporte = reporteDAO.insertar(reporte);
        reporte.setIdReporte(idReporte);

        return reporte;
    }

    /**
     * Genera reporte de visitas por visitante específico.
     *
     * @param dniVisitante DNI del visitante
     * @param fechaInicio fecha de inicio (opcional)
     * @param fechaFin fecha de fin (opcional)
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con ID
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteVisitasPorVisitante(String dniVisitante, Date fechaInicio,
                                                          Date fechaFin, Long idUsuarioGenerador)
            throws SQLException {

        Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
        if (visitante == null) {
            throw new IllegalArgumentException("Visitante no encontrado: " + dniVisitante);
        }

        List<Visita> visitas;
        if (fechaInicio != null && fechaFin != null) {
            visitas = visitaDAO.buscarPorVisitanteRangoFechas(visitante.getIdVisitante(), fechaInicio, fechaFin);
        } else {
            visitas = visitaDAO.buscarPorVisitante(visitante.getIdVisitante());
        }

        String contenido = generarHTMLVisitasPorVisitante(visitas, visitante, fechaInicio, fechaFin);

        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(TipoReporte.VISITAS_VISITANTE);
        reporte.setTitulo("Reporte de Visitas - " + visitante.getNombreCompleto());
        reporte.setParametrosFiltro(generarJSONFiltrosVisitante(dniVisitante, fechaInicio, fechaFin));
        reporte.setContenido(contenido);
        reporte.setTotalRegistros(visitas.size());
        reporte.setIdGeneradoPor(idUsuarioGenerador);

        Long idReporte = reporteDAO.insertar(reporte);
        reporte.setIdReporte(idReporte);

        return reporte;
    }

    /**
     * Genera reporte de visitas por interno específico.
     *
     * @param legajoInterno Legajo del interno
     * @param fechaInicio fecha de inicio (opcional)
     * @param fechaFin fecha de fin (opcional)
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con ID
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteVisitasPorInterno(String legajoInterno, Date fechaInicio,
                                                         Date fechaFin, Long idUsuarioGenerador)
            throws SQLException {

        Interno interno = internoDAO.buscarPorLegajo(legajoInterno);
        if (interno == null) {
            throw new IllegalArgumentException("Interno no encontrado: " + legajoInterno);
        }

        List<Visita> visitas;
        if (fechaInicio != null && fechaFin != null) {
            visitas = visitaDAO.buscarPorInternoRangoFechas(interno.getIdInterno(), fechaInicio, fechaFin);
        } else {
            visitas = visitaDAO.buscarPorInterno(interno.getIdInterno());
        }

        String contenido = generarHTMLVisitasPorInterno(visitas, interno, fechaInicio, fechaFin);

        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(TipoReporte.VISITAS_INTERNO);
        reporte.setTitulo("Reporte de Visitas - " + interno.getNombreCompleto());
        reporte.setParametrosFiltro(generarJSONFiltrosInterno(legajoInterno, fechaInicio, fechaFin));
        reporte.setContenido(contenido);
        reporte.setTotalRegistros(visitas.size());
        reporte.setIdGeneradoPor(idUsuarioGenerador);

        Long idReporte = reporteDAO.insertar(reporte);
        reporte.setIdReporte(idReporte);

        return reporte;
    }

    /**
     * Genera reporte de estadísticas generales de visitas.
     *
     * @param fechaInicio fecha de inicio del período
     * @param fechaFin fecha de fin del período
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con ID
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteEstadisticas(Date fechaInicio, Date fechaFin,
                                                    Long idUsuarioGenerador) throws SQLException {

        List<Visita> visitas = visitaDAO.buscarPorRangoFechas(fechaInicio, fechaFin);
        EstadisticasVisitas estadisticas = calcularEstadisticas(visitas, fechaInicio, fechaFin);

        String contenido = generarHTMLEstadisticas(estadisticas, fechaInicio, fechaFin);

        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(TipoReporte.ESTADISTICAS);
        reporte.setTitulo("Reporte de Estadísticas de Visitas");
        reporte.setParametrosFiltro(generarJSONFiltrosFecha(fechaInicio, fechaFin, null));
        reporte.setContenido(contenido);
        reporte.setTotalRegistros(visitas.size());
        reporte.setIdGeneradoPor(idUsuarioGenerador);

        Long idReporte = reporteDAO.insertar(reporte);
        reporte.setIdReporte(idReporte);

        return reporte;
    }

    /**
     * Genera reporte de restricciones activas.
     *
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con ID
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteRestriccionesActivas(Long idUsuarioGenerador) throws SQLException {

        List<Restriccion> restricciones = restriccionDAO.obtenerActivas();
        String contenido = generarHTMLRestricciones(restricciones);

        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(TipoReporte.RESTRICCIONES_ACTIVAS);
        reporte.setTitulo("Reporte de Restricciones Activas");
        reporte.setParametrosFiltro("{\"fecha\":\"" + formatoCompleto.format(new Date()) + "\"}");
        reporte.setContenido(contenido);
        reporte.setTotalRegistros(restricciones.size());
        reporte.setIdGeneradoPor(idUsuarioGenerador);

        Long idReporte = reporteDAO.insertar(reporte);
        reporte.setIdReporte(idReporte);

        return reporte;
    }

    /**
     * Genera reporte de autorizaciones vigentes.
     *
     * @param idUsuarioGenerador ID del usuario que genera el reporte
     * @return reporte generado con ID
     * @throws SQLException si ocurre error en base de datos
     */
    public ReporteGenerado generarReporteAutorizacionesVigentes(Long idUsuarioGenerador) throws SQLException {

        List<Autorizacion> autorizaciones = autorizacionDAO.obtenerVigentes();
        String contenido = generarHTMLAutorizaciones(autorizaciones);

        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(TipoReporte.AUTORIZACIONES_VIGENTES);
        reporte.setTitulo("Reporte de Autorizaciones Vigentes");
        reporte.setParametrosFiltro("{\"fecha\":\"" + formatoCompleto.format(new Date()) + "\"}");
        reporte.setContenido(contenido);
        reporte.setTotalRegistros(autorizaciones.size());
        reporte.setIdGeneradoPor(idUsuarioGenerador);

        Long idReporte = reporteDAO.insertar(reporte);
        reporte.setIdReporte(idReporte);

        return reporte;
    }

    // ===== MÉTODOS AUXILIARES PARA GENERACIÓN DE HTML =====

    /**
     * Genera HTML para reporte de visitas por fecha.
     */
    private String generarHTMLVisitasPorFecha(List<Visita> visitas, Date fechaInicio, Date fechaFin, String estadoFiltro) {
        StringBuilder html = new StringBuilder();
        html.append(plantillaHTMLInicio());

        // Encabezado
        html.append(encabezadoReporte("Reporte de Visitas por Fecha"));
        html.append("<div class='info-filtros'>");
        html.append("<p><strong>Período:</strong> ").append(formatoFecha.format(fechaInicio))
            .append(" al ").append(formatoFecha.format(fechaFin)).append("</p>");
        if (estadoFiltro != null && !estadoFiltro.equals("TODAS")) {
            html.append("<p><strong>Estado:</strong> ").append(estadoFiltro).append("</p>");
        }
        html.append("<p><strong>Total de registros:</strong> ").append(visitas.size()).append("</p>");
        html.append("</div>");

        // Tabla de visitas
        html.append(tablaVisitas(visitas));

        html.append(plantillaHTMLFin());
        return html.toString();
    }

    /**
     * Genera HTML para reporte de visitas por visitante.
     */
    private String generarHTMLVisitasPorVisitante(List<Visita> visitas, Visitante visitante,
                                                Date fechaInicio, Date fechaFin) {
        StringBuilder html = new StringBuilder();
        html.append(plantillaHTMLInicio());

        html.append(encabezadoReporte("Reporte de Visitas - " + visitante.getNombreCompleto()));
        html.append("<div class='info-visitante'>");
        html.append("<p><strong>DNI:</strong> ").append(visitante.getDni()).append("</p>");
        html.append("<p><strong>Estado:</strong> ").append(visitante.getEstado()).append("</p>");
        html.append("<p><strong>Total de visitas:</strong> ").append(visitas.size()).append("</p>");
        if (fechaInicio != null && fechaFin != null) {
            html.append("<p><strong>Período:</strong> ").append(formatoFecha.format(fechaInicio))
                .append(" al ").append(formatoFecha.format(fechaFin)).append("</p>");
        }
        html.append("</div>");

        html.append(tablaVisitas(visitas));

        html.append(plantillaHTMLFin());
        return html.toString();
    }

    /**
     * Genera HTML para reporte de visitas por interno.
     */
    private String generarHTMLVisitasPorInterno(List<Visita> visitas, Interno interno,
                                              Date fechaInicio, Date fechaFin) {
        StringBuilder html = new StringBuilder();
        html.append(plantillaHTMLInicio());

        html.append(encabezadoReporte("Reporte de Visitas - " + interno.getNombreCompleto()));
        html.append("<div class='info-interno'>");
        html.append("<p><strong>Legajo:</strong> ").append(interno.getNumeroLegajo()).append("</p>");
        html.append("<p><strong>Ubicación:</strong> ").append(interno.getUbicacionCompleta()).append("</p>");
        html.append("<p><strong>Situación:</strong> ").append(interno.getSituacionProcesal()).append("</p>");
        html.append("<p><strong>Total de visitas:</strong> ").append(visitas.size()).append("</p>");
        if (fechaInicio != null && fechaFin != null) {
            html.append("<p><strong>Período:</strong> ").append(formatoFecha.format(fechaInicio))
                .append(" al ").append(formatoFecha.format(fechaFin)).append("</p>");
        }
        html.append("</div>");

        html.append(tablaVisitas(visitas));

        html.append(plantillaHTMLFin());
        return html.toString();
    }

    /**
     * Genera HTML para reporte de estadísticas.
     */
    private String generarHTMLEstadisticas(EstadisticasVisitas estadisticas, Date fechaInicio, Date fechaFin) {
        StringBuilder html = new StringBuilder();
        html.append(plantillaHTMLInicio());

        html.append(encabezadoReporte("Reporte de Estadísticas de Visitas"));
        html.append("<div class='info-periodo'>");
        html.append("<p><strong>Período analizado:</strong> ").append(formatoFecha.format(fechaInicio))
            .append(" al ").append(formatoFecha.format(fechaFin)).append("</p>");
        html.append("</div>");

        html.append("<div class='estadisticas'>");
        html.append("<h2>Métricas Generales</h2>");
        html.append("<div class='metric-grid'>");
        html.append("<div class='metrica'><span class='numero'>").append(estadisticas.totalVisitas)
            .append("</span><span class='label'>Total Visitas</span></div>");
        html.append("<div class='metrica'><span class='numero'>").append(estadisticas.visitantesUnicos)
            .append("</span><span class='label'>Visitantes Únicos</span></div>");
        html.append("<div class='metrica'><span class='numero'>").append(estadisticas.internosVisitados)
            .append("</span><span class='label'>Internos Visitados</span></div>");
        html.append("<div class='metrica'><span class='numero'>").append(estadisticas.duracionPromedio)
            .append("</span><span class='label'>Duración Promedio</span></div>");
        html.append("</div>");

        html.append("<h2>Visitas por Estado</h2>");
        html.append("<table class='estadisticas-table'>");
        html.append("<thead><tr><th>Estado</th><th>Cantidad</th><th>Porcentaje</th></tr></thead>");
        html.append("<tbody>");
        for (Map.Entry<String, Integer> entry : estadisticas.visitasPorEstado.entrySet()) {
            double porcentaje = (entry.getValue() * 100.0) / estadisticas.totalVisitas;
            html.append("<tr><td>").append(entry.getKey()).append("</td>")
                .append("<td>").append(entry.getValue()).append("</td>")
                .append("<td>").append(String.format("%.1f%%", porcentaje)).append("</td></tr>");
        }
        html.append("</tbody></table>");

        html.append("</div>");

        html.append(plantillaHTMLFin());
        return html.toString();
    }

    /**
     * Genera HTML para reporte de restricciones activas.
     */
    private String generarHTMLRestricciones(List<Restriccion> restricciones) {
        StringBuilder html = new StringBuilder();
        html.append(plantillaHTMLInicio());

        html.append(encabezadoReporte("Reporte de Restricciones Activas"));
        html.append("<div class='info-general'>");
        html.append("<p><strong>Total de restricciones activas:</strong> ").append(restricciones.size()).append("</p>");
        html.append("<p><strong>Fecha de reporte:</strong> ").append(formatoCompleto.format(new Date())).append("</p>");
        html.append("</div>");

        html.append("<table class='data-table'>");
        html.append("<thead><tr><th>Visitante</th><th>Tipo</th><th>Motivo</th><th>Vigencia</th><th>Aplica a</th></tr></thead>");
        html.append("<tbody>");

        for (Restriccion restriccion : restricciones) {
            html.append("<tr>");
            html.append("<td>").append(restriccion.getVisitante().getNombreCompleto())
                .append(" (DNI: ").append(restriccion.getVisitante().getDni()).append(")</td>");
            html.append("<td><span class='tipo-restriccion'>").append(restriccion.getTipoRestriccion())
                .append("</span></td>");
            html.append("<td>").append(restriccion.getMotivo()).append("</td>");
            html.append("<td>").append(formatoFecha.format(restriccion.getFechaInicio()));
            if (restriccion.getFechaFin() != null) {
                html.append(" al ").append(formatoFecha.format(restriccion.getFechaFin()));
            } else {
                html.append(" (Indefinida)");
            }
            html.append("</td>");
            html.append("<td>").append(restriccion.getAplicableA()).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");

        html.append(plantillaHTMLFin());
        return html.toString();
    }

    /**
     * Genera HTML para reporte de autorizaciones vigentes.
     */
    private String generarHTMLAutorizaciones(List<Autorizacion> autorizaciones) {
        StringBuilder html = new StringBuilder();
        html.append(plantillaHTMLInicio());

        html.append(encabezadoReporte("Reporte de Autorizaciones Vigentes"));
        html.append("<div class='info-general'>");
        html.append("<p><strong>Total de autorizaciones vigentes:</strong> ").append(autorizaciones.size()).append("</p>");
        html.append("<p><strong>Fecha de reporte:</strong> ").append(formatoCompleto.format(new Date())).append("</p>");
        html.append("</div>");

        html.append("<table class='data-table'>");
        html.append("<thead><tr><th>Visitante</th><th>Interno</th><th>Tipo Relación</th><th>Fecha Autorización</th><th>Vencimiento</th></tr></thead>");
        html.append("<tbody>");

        for (Autorizacion autorizacion : autorizaciones) {
            html.append("<tr>");
            html.append("<td>").append(autorizacion.getVisitante().getNombreCompleto())
                .append(" (DNI: ").append(autorizacion.getVisitante().getDni()).append(")</td>");
            html.append("<td>").append(autorizacion.getInterno().getNombreCompleto())
                .append(" (Leg: ").append(autorizacion.getInterno().getNumeroLegajo()).append(")</td>");
            html.append("<td>").append(autorizacion.getTipoRelacion()).append("</td>");
            html.append("<td>").append(formatoFecha.format(autorizacion.getFechaAutorizacion())).append("</td>");
            html.append("<td>");
            if (autorizacion.getFechaVencimiento() != null) {
                html.append(formatoFecha.format(autorizacion.getFechaVencimiento()));
            } else {
                html.append("<em>Indefinida</em>");
            }
            html.append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");

        html.append(plantillaHTMLFin());
        return html.toString();
    }

    // ===== PLANTILLAS HTML =====

    private String plantillaHTMLInicio() {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>SIGVIP - Reporte</title>
                <style>
                %s
                </style>
            </head>
            <body>
            """.formatted(generarCSS());
    }

    private String plantillaHTMLFin() {
        return """
                <div class="footer">
                    <p><strong>SIGVIP</strong> - Sistema Integral de Gestión de Visitas Penitenciarias</p>
                    <p>Generado el %s</p>
                </div>
            </body>
            </html>
            """.formatted(formatoCompleto.format(new Date()));
    }

    private String encabezadoReporte(String titulo) {
        return """
            <div class="header">
                <h1>%s</h1>
                <div class="logo">SIGVIP</div>
            </div>
            """.formatted(titulo);
    }

    private String tablaVisitas(List<Visita> visitas) {
        StringBuilder tabla = new StringBuilder();
        tabla.append("<table class='visitas-table'>");
        tabla.append("<thead><tr>");
        tabla.append("<th>ID</th><th>Fecha</th><th>DNI Visitante</th><th>Visitante</th>");
        tabla.append("<th>Legajo Interno</th><th>Interno</th>");
        tabla.append("<th>Hora Ingreso</th><th>Hora Egreso</th><th>Duración</th>");
        tabla.append("<th>Estado</th>");
        tabla.append("</tr></thead><tbody>");

        for (Visita visita : visitas) {
            tabla.append("<tr>");
            tabla.append("<td>").append(visita.getIdVisita()).append("</td>");
            tabla.append("<td>").append(formatoFecha.format(visita.getFechaVisita())).append("</td>");
            tabla.append("<td>").append(visita.getVisitante().getDni()).append("</td>");
            tabla.append("<td>").append(visita.getVisitante().getNombreCompleto()).append("</td>");
            tabla.append("<td>").append(visita.getInterno().getNumeroLegajo()).append("</td>");
            tabla.append("<td>").append(visita.getInterno().getNombreCompleto()).append("</td>");
            tabla.append("<td>").append(formatoHora.format(visita.getHoraIngreso())).append("</td>");
            tabla.append("<td>").append(visita.getHoraEgreso() != null ?
                formatoHora.format(visita.getHoraEgreso()) : "En curso").append("</td>");
            tabla.append("<td>").append(visita.getDuracionFormateada()).append("</td>");
            tabla.append("<td><span class='estado-").append(visita.getEstadoVisita().name().toLowerCase())
                .append("'>").append(visita.getEstadoVisita()).append("</span></td>");
            tabla.append("</tr>");
        }

        tabla.append("</tbody></table>");
        return tabla.toString();
    }

    private String generarCSS() {
        return """
            body {
                font-family: Arial, sans-serif;
                margin: 0;
                padding: 20px;
                background-color: #f5f5f5;
                color: #333;
            }
            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 30px;
                border-radius: 10px;
                margin-bottom: 30px;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            .header h1 {
                margin: 0;
                font-size: 28px;
            }
            .logo {
                font-size: 24px;
                font-weight: bold;
                opacity: 0.8;
            }
            .info-filtros, .info-visitante, .info-interno, .info-periodo, .info-general {
                background: white;
                padding: 20px;
                border-radius: 8px;
                margin-bottom: 20px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            .info-filtros p, .info-visitante p, .info-interno p, .info-periodo p, .info-general p {
                margin: 8px 0;
            }
            .visitas-table, .data-table, .estadisticas-table {
                width: 100%;
                border-collapse: collapse;
                background: white;
                border-radius: 8px;
                overflow: hidden;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                margin-bottom: 30px;
            }
            .visitas-table th, .data-table th, .estadisticas-table th {
                background: #3498db;
                color: white;
                padding: 15px 12px;
                text-align: left;
                font-weight: bold;
            }
            .visitas-table td, .data-table td, .estadisticas-table td {
                padding: 12px;
                border-bottom: 1px solid #eee;
            }
            .visitas-table tr:nth-child(even), .data-table tr:nth-child(even) {
                background-color: #f8f9fa;
            }
            .visitas-table tr:hover, .data-table tr:hover {
                background-color: #e3f2fd;
            }
            .estado-finalizada { color: #27ae60; font-weight: bold; }
            .estado-en_curso { color: #f39c12; font-weight: bold; }
            .estado-programada { color: #3498db; font-weight: bold; }
            .estado-cancelada { color: #e74c3c; font-weight: bold; }
            .tipo-restriccion {
                background: #e74c3c;
                color: white;
                padding: 4px 8px;
                border-radius: 4px;
                font-size: 12px;
            }
            .estadisticas {
                background: white;
                padding: 20px;
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            .metric-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
                gap: 20px;
                margin: 20px 0;
            }
            .metrica {
                text-align: center;
                padding: 20px;
                background: #f8f9fa;
                border-radius: 8px;
                border-left: 4px solid #3498db;
            }
            .metrica .numero {
                display: block;
                font-size: 36px;
                font-weight: bold;
                color: #2c3e50;
            }
            .metrica .label {
                color: #7f8c8d;
                font-size: 14px;
            }
            .footer {
                background: #2c3e50;
                color: white;
                padding: 20px;
                text-align: center;
                border-radius: 8px;
                margin-top: 30px;
            }
            @media print {
                body { background: white; }
                .header { background: #333; }
                .metrica { page-break-inside: avoid; }
            }
            """;
    }

    // ===== CLASE AUXILIAR PARA ESTADISTICAS =====

    private static class EstadisticasVisitas {
        int totalVisitas;
        int visitantesUnicos;
        int internosVisitados;
        String duracionPromedio;
        Map<String, Integer> visitasPorEstado;

        EstadisticasVisitas() {
            this.visitasPorEstado = new HashMap<>();
        }
    }

    private EstadisticasVisitas calcularEstadisticas(List<Visita> visitas, Date fechaInicio, Date fechaFin) {
        EstadisticasVisitas stats = new EstadisticasVisitas();

        stats.totalVisitas = visitas.size();
        stats.visitantesUnicos = (int) visitas.stream()
            .map(v -> v.getVisitante().getIdVisitante())
            .distinct()
            .count();
        stats.internosVisitados = (int) visitas.stream()
            .map(v -> v.getInterno().getIdInterno())
            .distinct()
            .count();

        // Calcular duración promedio
        double totalMinutos = visitas.stream()
            .mapToDouble(v -> {
                if (v.getHoraEgreso() != null && v.getHoraIngreso() != null) {
                    long diff = v.getHoraEgreso().getTime() - v.getHoraIngreso().getTime();
                    return diff / (1000.0 * 60); // Convertir a minutos
                }
                return 0;
            })
            .sum();

        int horas = (int) (totalMinutos / 60);
        int minutos = (int) (totalMinutos % 60);
        stats.duracionPromedio = visitas.isEmpty() ? "0h 0m" :
            String.format("%dh %dm", horas, minutos);

        // Agrupar por estado
        for (Visita visita : visitas) {
            String estado = visita.getEstadoVisita().name();
            stats.visitasPorEstado.put(estado,
                stats.visitasPorEstado.getOrDefault(estado, 0) + 1);
        }

        return stats;
    }

    // ===== GENERADORES DE JSON PARA FILTROS =====

    private String generarJSONFiltrosFecha(Date fechaInicio, Date fechaFin, String estadoFiltro) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"fechaInicio\":\"").append(formatoFecha.format(fechaInicio)).append("\",");
        json.append("\"fechaFin\":\"").append(formatoFecha.format(fechaFin)).append("\"");
        if (estadoFiltro != null && !estadoFiltro.equals("TODAS")) {
            json.append(",\"estado\":\"").append(estadoFiltro).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private String generarJSONFiltrosVisitante(String dni, Date fechaInicio, Date fechaFin) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"dni\":\"").append(dni).append("\"");
        if (fechaInicio != null && fechaFin != null) {
            json.append(",\"fechaInicio\":\"").append(formatoFecha.format(fechaInicio)).append("\",");
            json.append("\"fechaFin\":\"").append(formatoFecha.format(fechaFin)).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    private String generarJSONFiltrosInterno(String legajo, Date fechaInicio, Date fechaFin) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"legajo\":\"").append(legajo).append("\"");
        if (fechaInicio != null && fechaFin != null) {
            json.append(",\"fechaInicio\":\"").append(formatoFecha.format(fechaInicio)).append("\",");
            json.append("\"fechaFin\":\"").append(formatoFecha.format(fechaFin)).append("\"");
        }
        json.append("}");
        return json.toString();
    }

    // ===== EXPORTACIÓN A CSV/TXT (TP4: Uso de archivos) =====

    /**
     * Exporta una lista de visitas a formato CSV.
     * Cumple con requisito académico TP4: "Uso de archivos para guardar información"
     *
     * Formato CSV: ID,Fecha,DNI Visitante,Visitante,Legajo,Interno,Hora Ingreso,Hora Egreso,Duración,Estado
     *
     * @param visitas lista de visitas a exportar
     * @param rutaDestino ruta del archivo CSV destino
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void exportarVisitasCSV(List<Visita> visitas, String rutaDestino) throws IOException {
        try (java.io.FileWriter fw = new java.io.FileWriter(rutaDestino);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

            // Encabezado CSV
            out.println("ID,Fecha,DNI Visitante,Visitante,Legajo Interno,Interno,Hora Ingreso,Hora Egreso,Duracion,Estado");

            // Datos
            for (Visita visita : visitas) {
                StringBuilder linea = new StringBuilder();
                linea.append(visita.getIdVisita()).append(",");
                linea.append(formatoFecha.format(visita.getFechaVisita())).append(",");
                linea.append(visita.getVisitante().getDni()).append(",");
                linea.append("\"").append(visita.getVisitante().getNombreCompleto()).append("\",");
                linea.append(visita.getInterno().getNumeroLegajo()).append(",");
                linea.append("\"").append(visita.getInterno().getNombreCompleto()).append("\",");
                linea.append(formatoHora.format(visita.getHoraIngreso())).append(",");
                linea.append(visita.getHoraEgreso() != null ?
                    formatoHora.format(visita.getHoraEgreso()) : "EN_CURSO").append(",");
                linea.append("\"").append(visita.getDuracionFormateada()).append("\",");
                linea.append(visita.getEstadoVisita());

                out.println(linea.toString());
            }
        }

        ServicioLogs.getInstancia().info("SISTEMA", "EXPORT_CSV",
            "Exportadas " + visitas.size() + " visitas a CSV: " + rutaDestino);
    }

    /**
     * Exporta una lista de visitas a formato TXT (texto plano legible).
     * Cumple con requisito académico TP4: "Uso de archivos para guardar información"
     *
     * @param visitas lista de visitas a exportar
     * @param rutaDestino ruta del archivo TXT destino
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void exportarVisitasTXT(List<Visita> visitas, String rutaDestino) throws IOException {
        try (java.io.FileWriter fw = new java.io.FileWriter(rutaDestino);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

            // Encabezado
            out.println("===============================================");
            out.println("   SIGVIP - REPORTE DE VISITAS");
            out.println("   Generado: " + formatoCompleto.format(new Date()));
            out.println("===============================================");
            out.println();
            out.println("Total de registros: " + visitas.size());
            out.println();

            // Datos en formato legible
            int contador = 1;
            for (Visita visita : visitas) {
                out.println("--- VISITA #" + contador + " (ID: " + visita.getIdVisita() + ") ---");
                out.println("Fecha:          " + formatoFecha.format(visita.getFechaVisita()));
                out.println("Visitante:      " + visita.getVisitante().getNombreCompleto() +
                    " (DNI: " + visita.getVisitante().getDni() + ")");
                out.println("Interno:        " + visita.getInterno().getNombreCompleto() +
                    " (Legajo: " + visita.getInterno().getNumeroLegajo() + ")");
                out.println("Hora Ingreso:   " + formatoHora.format(visita.getHoraIngreso()));
                out.println("Hora Egreso:    " + (visita.getHoraEgreso() != null ?
                    formatoHora.format(visita.getHoraEgreso()) : "EN CURSO"));
                out.println("Duración:       " + visita.getDuracionFormateada());
                out.println("Estado:         " + visita.getEstadoVisita());
                out.println();
                contador++;
            }

            out.println("===============================================");
            out.println("   FIN DEL REPORTE");
            out.println("===============================================");
        }

        ServicioLogs.getInstancia().info("SISTEMA", "EXPORT_TXT",
            "Exportadas " + visitas.size() + " visitas a TXT: " + rutaDestino);
    }

    /**
     * Exporta una lista de restricciones a formato CSV.
     *
     * @param restricciones lista de restricciones a exportar
     * @param rutaDestino ruta del archivo CSV destino
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void exportarRestriccionesCSV(List<Restriccion> restricciones, String rutaDestino) throws IOException {
        try (java.io.FileWriter fw = new java.io.FileWriter(rutaDestino);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

            // Encabezado CSV
            out.println("ID,DNI Visitante,Visitante,Tipo Restriccion,Motivo,Fecha Inicio,Fecha Fin,Aplicable A,Activa");

            // Datos
            for (Restriccion restriccion : restricciones) {
                StringBuilder linea = new StringBuilder();
                linea.append(restriccion.getIdRestriccion()).append(",");
                linea.append(restriccion.getVisitante().getDni()).append(",");
                linea.append("\"").append(restriccion.getVisitante().getNombreCompleto()).append("\",");
                linea.append(restriccion.getTipoRestriccion()).append(",");
                linea.append("\"").append(restriccion.getMotivo()).append("\",");
                linea.append(formatoFecha.format(restriccion.getFechaInicio())).append(",");
                linea.append(restriccion.getFechaFin() != null ?
                    formatoFecha.format(restriccion.getFechaFin()) : "INDEFINIDA").append(",");
                linea.append(restriccion.getAplicableA()).append(",");
                linea.append(restriccion.isActivo() ? "SI" : "NO");

                out.println(linea.toString());
            }
        }

        ServicioLogs.getInstancia().info("SISTEMA", "EXPORT_CSV",
            "Exportadas " + restricciones.size() + " restricciones a CSV: " + rutaDestino);
    }

    /**
     * Exporta una lista de autorizaciones a formato CSV.
     *
     * @param autorizaciones lista de autorizaciones a exportar
     * @param rutaDestino ruta del archivo CSV destino
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void exportarAutorizacionesCSV(List<Autorizacion> autorizaciones, String rutaDestino) throws IOException {
        try (java.io.FileWriter fw = new java.io.FileWriter(rutaDestino);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

            // Encabezado CSV
            out.println("ID,DNI Visitante,Visitante,Legajo Interno,Interno,Tipo Relacion,Fecha Autorizacion,Fecha Vencimiento,Estado");

            // Datos
            for (Autorizacion autorizacion : autorizaciones) {
                StringBuilder linea = new StringBuilder();
                linea.append(autorizacion.getIdAutorizacion()).append(",");
                linea.append(autorizacion.getVisitante().getDni()).append(",");
                linea.append("\"").append(autorizacion.getVisitante().getNombreCompleto()).append("\",");
                linea.append(autorizacion.getInterno().getNumeroLegajo()).append(",");
                linea.append("\"").append(autorizacion.getInterno().getNombreCompleto()).append("\",");
                linea.append(autorizacion.getTipoRelacion()).append(",");
                linea.append(formatoFecha.format(autorizacion.getFechaAutorizacion())).append(",");
                linea.append(autorizacion.getFechaVencimiento() != null ?
                    formatoFecha.format(autorizacion.getFechaVencimiento()) : "INDEFINIDA").append(",");
                linea.append(autorizacion.getEstado());

                out.println(linea.toString());
            }
        }

        ServicioLogs.getInstancia().info("SISTEMA", "EXPORT_CSV",
            "Exportadas " + autorizaciones.size() + " autorizaciones a CSV: " + rutaDestino);
    }

    /**
     * Exporta estadísticas a formato TXT legible.
     *
     * @param fechaInicio fecha de inicio del período analizado
     * @param fechaFin fecha de fin del período analizado
     * @param rutaDestino ruta del archivo TXT destino
     * @throws SQLException si ocurre error en base de datos
     * @throws IOException si ocurre un error al escribir el archivo
     */
    public void exportarEstadisticasTXT(Date fechaInicio, Date fechaFin, String rutaDestino)
            throws SQLException, IOException {

        List<Visita> visitas = visitaDAO.buscarPorRangoFechas(fechaInicio, fechaFin);
        EstadisticasVisitas estadisticas = calcularEstadisticas(visitas, fechaInicio, fechaFin);

        try (java.io.FileWriter fw = new java.io.FileWriter(rutaDestino);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw);
             java.io.PrintWriter out = new java.io.PrintWriter(bw)) {

            out.println("===============================================");
            out.println("   SIGVIP - ESTADÍSTICAS DE VISITAS");
            out.println("   Período: " + formatoFecha.format(fechaInicio) +
                " al " + formatoFecha.format(fechaFin));
            out.println("   Generado: " + formatoCompleto.format(new Date()));
            out.println("===============================================");
            out.println();

            out.println("MÉTRICAS GENERALES:");
            out.println("-------------------------------------------------");
            out.println("Total de Visitas:         " + estadisticas.totalVisitas);
            out.println("Visitantes Únicos:        " + estadisticas.visitantesUnicos);
            out.println("Internos Visitados:       " + estadisticas.internosVisitados);
            out.println("Duración Promedio:        " + estadisticas.duracionPromedio);
            out.println();

            out.println("VISITAS POR ESTADO:");
            out.println("-------------------------------------------------");
            for (Map.Entry<String, Integer> entry : estadisticas.visitasPorEstado.entrySet()) {
                double porcentaje = (entry.getValue() * 100.0) / estadisticas.totalVisitas;
                out.println(String.format("%-20s: %4d (%5.1f%%)",
                    entry.getKey(), entry.getValue(), porcentaje));
            }
            out.println();

            out.println("===============================================");
            out.println("   FIN DEL REPORTE");
            out.println("===============================================");
        }

        ServicioLogs.getInstancia().info("SISTEMA", "EXPORT_STATS_TXT",
            "Estadísticas exportadas a TXT: " + rutaDestino);
    }
}