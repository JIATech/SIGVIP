package com.sigvip.controlador;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.*;
import com.sigvip.persistencia.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador para generación de reportes y consultas estadísticas.
 * Implementa la capa de control en el patrón MVC.
 *
 * Especificación: PDF Sección 11.2.2 - Capa de Control
 * Funcionalidades:
 * - RF006: Historial de Visitas
 * - RF007: Reportes del Sistema
 * - RF009: Consulta de Información
 */
public class ControladorReportes {

    private VisitaDAO visitaDAO;
    private VisitanteDAO visitanteDAO;
    private InternoDAO internoDAO;
    private AutorizacionDAO autorizacionDAO;
    private RestriccionDAO restriccionDAO;
    private UsuarioDAO usuarioDAO;

    private SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

    /**
     * Constructor que inicializa los DAOs.
     */
    public ControladorReportes() {
        this.visitaDAO = new VisitaDAO();
        this.visitanteDAO = new VisitanteDAO();
        this.internoDAO = new InternoDAO();
        this.autorizacionDAO = new AutorizacionDAO();
        this.restriccionDAO = new RestriccionDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Genera reporte de visitas en un rango de fechas.
     * Implementa RF006: Historial de Visitas.
     *
     * @param fechaInicio fecha de inicio (inclusive)
     * @param fechaFin fecha de fin (inclusive)
     */
    public void generarReporteVisitasPorFecha(Date fechaInicio, Date fechaFin) {
        try {
            List<Visita> visitas = visitaDAO.buscarPorRangoFechas(fechaInicio, fechaFin);

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║        REPORTE DE VISITAS POR PERÍODO                 ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Período: " + formatoFecha.format(fechaInicio) +
                             " al " + formatoFecha.format(fechaFin));
            System.out.println("Total de visitas: " + visitas.size());
            System.out.println("────────────────────────────────────────────────────────");

            if (visitas.isEmpty()) {
                System.out.println("No hay visitas registradas en este período.");
            } else {
                // Agrupar por estado
                Map<EstadoVisita, Long> porEstado = visitas.stream()
                    .collect(Collectors.groupingBy(Visita::getEstadoVisita, Collectors.counting()));

                System.out.println("\nDistribución por estado:");
                for (EstadoVisita estado : EstadoVisita.values()) {
                    long count = porEstado.getOrDefault(estado, 0L);
                    if (count > 0) {
                        System.out.println("  " + estado + ": " + count);
                    }
                }

                System.out.println("\nDetalle de visitas:");
                for (Visita visita : visitas) {
                    System.out.println("\n  ID: " + visita.getIdVisita());
                    System.out.println("  Fecha: " + formatoFecha.format(visita.getFechaVisita()));
                    System.out.println("  Ingreso: " + (visita.getHoraIngreso() != null ?
                                                        formatoHora.format(visita.getHoraIngreso()) : "N/A"));
                    System.out.println("  Egreso: " + (visita.getHoraEgreso() != null ?
                                                       formatoHora.format(visita.getHoraEgreso()) : "En curso"));
                    System.out.println("  Estado: " + visita.getEstadoVisita());
                    System.out.println("  Duración: " + visita.getDuracionFormateada());
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Genera reporte de visitas de hoy.
     * Implementa RF009: Consulta de Información - Visitas del día.
     */
    public void generarReporteVisitasHoy() {
        Date hoy = new Date();
        generarReporteVisitasPorFecha(hoy, hoy);
    }

    /**
     * Genera reporte de visitantes por estado.
     * Implementa RF007: Reportes del Sistema.
     */
    public void generarReporteVisitantesPorEstado() {
        try {
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║      REPORTE DE VISITANTES POR ESTADO                 ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");

            int total = visitanteDAO.contarTotal();
            System.out.println("Total de visitantes: " + total);
            System.out.println("────────────────────────────────────────────────────────");

            for (EstadoVisitante estado : EstadoVisitante.values()) {
                List<Visitante> visitantes = visitanteDAO.buscarPorEstado(estado);
                System.out.println("\n" + estado + ": " + visitantes.size() + " visitantes");

                if (!visitantes.isEmpty() && visitantes.size() <= 10) {
                    for (Visitante v : visitantes) {
                        System.out.println("  - " + v.getNombreCompleto() +
                                         " (DNI: " + v.getDni() + ")");
                    }
                } else if (visitantes.size() > 10) {
                    System.out.println("  (Mostrando primeros 10 de " + visitantes.size() + ")");
                    for (int i = 0; i < 10; i++) {
                        Visitante v = visitantes.get(i);
                        System.out.println("  - " + v.getNombreCompleto() +
                                         " (DNI: " + v.getDni() + ")");
                    }
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Genera reporte de autorizaciones vigentes.
     * Implementa RF007: Reportes del Sistema.
     */
    public void generarReporteAutorizacionesVigentes() {
        try {
            List<Autorizacion> autorizaciones = autorizacionDAO.buscarPorEstado(
                EstadoAutorizacion.VIGENTE
            );

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║     REPORTE DE AUTORIZACIONES VIGENTES                ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Total: " + autorizaciones.size());
            System.out.println("────────────────────────────────────────────────────────");

            // Agrupar por tipo de relación
            Map<TipoRelacion, Long> porTipo = autorizaciones.stream()
                .collect(Collectors.groupingBy(Autorizacion::getTipoRelacion, Collectors.counting()));

            System.out.println("\nDistribución por tipo de relación:");
            for (TipoRelacion tipo : TipoRelacion.values()) {
                long count = porTipo.getOrDefault(tipo, 0L);
                if (count > 0) {
                    System.out.println("  " + tipo + ": " + count);
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Genera reporte de restricciones activas.
     * Implementa RF007: Reportes del Sistema.
     */
    public void generarReporteRestriccionesActivas() {
        try {
            int totalActivas = restriccionDAO.contarActivas();

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║      REPORTE DE RESTRICCIONES ACTIVAS                 ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Total de restricciones activas: " + totalActivas);
            System.out.println("────────────────────────────────────────────────────────");

            // Mostrar por tipo
            for (TipoRestriccion tipo : TipoRestriccion.values()) {
                List<Restriccion> restricciones = restriccionDAO.buscarPorTipo(tipo);

                // Filtrar solo las activas
                List<Restriccion> activas = restricciones.stream()
                    .filter(Restriccion::estaActiva)
                    .collect(Collectors.toList());

                if (!activas.isEmpty()) {
                    System.out.println("\n" + tipo + ": " + activas.size());
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Genera reporte de internos activos por establecimiento.
     * Implementa RF009: Consulta de Información.
     */
    public void generarReporteInternosPorEstablecimiento() {
        try {
            List<Interno> internos = internoDAO.obtenerActivos();

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║    REPORTE DE INTERNOS ACTIVOS                        ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Total de internos activos: " + internos.size());
            System.out.println("────────────────────────────────────────────────────────");

            // Agrupar por situación procesal
            Map<SituacionProcesal, Long> porSituacion = internos.stream()
                .filter(i -> i.getSituacionProcesal() != null)
                .collect(Collectors.groupingBy(Interno::getSituacionProcesal, Collectors.counting()));

            System.out.println("\nDistribución por situación procesal:");
            for (SituacionProcesal situacion : SituacionProcesal.values()) {
                long count = porSituacion.getOrDefault(situacion, 0L);
                if (count > 0) {
                    System.out.println("  " + situacion + ": " + count);
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Genera reporte de usuarios por rol.
     * Implementa RF007: Reportes del Sistema.
     */
    public void generarReporteUsuariosPorRol() {
        try {
            List<Usuario> usuarios = usuarioDAO.obtenerActivos();

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║      REPORTE DE USUARIOS ACTIVOS                      ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Total de usuarios activos: " + usuarios.size());
            System.out.println("────────────────────────────────────────────────────────");

            for (Rol rol : Rol.values()) {
                int count = usuarioDAO.contarPorRol(rol);
                if (count > 0) {
                    System.out.println("\n" + rol + ": " + count + " usuarios");

                    List<Usuario> usuariosPorRol = usuarioDAO.buscarPorRol(rol);
                    for (Usuario u : usuariosPorRol) {
                        System.out.println("  - " + u.getNombreCompleto() +
                                         " (" + u.getNombreUsuario() + ")");
                    }
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar reporte: " + e.getMessage());
        }
    }

    /**
     * Genera dashboard con estadísticas generales del sistema.
     * Implementa RF007: Reportes del Sistema - Vista general.
     */
    public void generarDashboardGeneral() {
        try {
            System.out.println("\n╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║                  DASHBOARD DEL SISTEMA                        ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");

            // Estadísticas de visitantes
            int totalVisitantes = visitanteDAO.contarTotal();
            List<Visitante> habilitados = visitanteDAO.buscarPorEstado(EstadoVisitante.HABILITADO);

            System.out.println("\n📋 VISITANTES");
            System.out.println("  Total registrados: " + totalVisitantes);
            System.out.println("  Habilitados: " + habilitados.size());

            // Estadísticas de internos
            List<Interno> internosActivos = internoDAO.obtenerActivos();

            System.out.println("\n👤 INTERNOS");
            System.out.println("  Activos: " + internosActivos.size());

            // Estadísticas de visitas
            int visitasEnCurso = visitaDAO.contarVisitasEnCurso();
            List<Visita> visitasHoy = visitaDAO.buscarPorFecha(new Date());

            System.out.println("\n🚪 VISITAS");
            System.out.println("  En curso ahora: " + visitasEnCurso);
            System.out.println("  Total de hoy: " + visitasHoy.size());

            // Estadísticas de autorizaciones
            List<Autorizacion> autorizacionesVigentes = autorizacionDAO.buscarPorEstado(
                EstadoAutorizacion.VIGENTE
            );

            System.out.println("\n✓ AUTORIZACIONES");
            System.out.println("  Vigentes: " + autorizacionesVigentes.size());

            // Advertencias de autorizaciones próximas a vencer
            List<Autorizacion> proximasVencer = autorizacionDAO.obtenerProximasAVencer(30);
            if (!proximasVencer.isEmpty()) {
                System.out.println("  ⚠ Próximas a vencer (30 días): " + proximasVencer.size());
            }

            // Estadísticas de restricciones
            int restriccionesActivas = restriccionDAO.contarActivas();

            System.out.println("\n🚫 RESTRICCIONES");
            System.out.println("  Activas: " + restriccionesActivas);

            // Estadísticas de usuarios
            List<Usuario> usuariosActivos = usuarioDAO.obtenerActivos();

            System.out.println("\n👮 USUARIOS");
            System.out.println("  Activos: " + usuariosActivos.size());

            System.out.println("\n═══════════════════════════════════════════════════════════════");
            System.out.println("Fecha y hora: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            System.out.println("═══════════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar dashboard: " + e.getMessage());
        }
    }

    /**
     * Genera reporte del historial de visitas de un visitante.
     * Implementa RF006: Historial de Visitas.
     *
     * @param dniVisitante DNI del visitante
     */
    public void generarHistorialVisitante(String dniVisitante) {
        try {
            Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);

            if (visitante == null) {
                System.out.println("No se encontró visitante con DNI: " + dniVisitante);
                return;
            }

            List<Visita> visitas = visitaDAO.buscarPorVisitante(visitante.getIdVisitante());

            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║        HISTORIAL DE VISITAS                           ║");
            System.out.println("╚════════════════════════════════════════════════════════╝");
            System.out.println("Visitante: " + visitante.getNombreCompleto());
            System.out.println("DNI: " + visitante.getDni());
            System.out.println("Total de visitas: " + visitas.size());
            System.out.println("────────────────────────────────────────────────────────");

            if (visitas.isEmpty()) {
                System.out.println("No hay visitas registradas.");
            } else {
                for (Visita visita : visitas) {
                    System.out.println("\n  Fecha: " + formatoFecha.format(visita.getFechaVisita()));
                    System.out.println("  Estado: " + visita.getEstadoVisita());
                    System.out.println("  Duración: " + visita.getDuracionFormateada());
                }
            }

            System.out.println("════════════════════════════════════════════════════════\n");

        } catch (SQLException e) {
            System.err.println("Error al generar historial: " + e.getMessage());
        }
    }
}
