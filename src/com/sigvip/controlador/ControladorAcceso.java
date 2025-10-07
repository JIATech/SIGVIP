package com.sigvip.controlador;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.EstadoVisita;
import com.sigvip.persistencia.*;
import com.sigvip.utilidades.ServicioValidacionSeguridad;
import com.sigvip.utilidades.ServicioValidacionSeguridad.ResultadoValidacion;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Controlador para el control de acceso e ingreso/egreso de visitas.
 * Implementa la capa de control en el patrón MVC.
 *
 * Especificación: PDF Sección 11.2.2 - Capa de Control
 * Funcionalidades críticas:
 * - RF003: Control de Ingreso (6 pasos de validación)
 * - RF004: Control de Egreso
 * - RF010: Validación de Capacidad
 */
public class ControladorAcceso {

    private VisitanteDAO visitanteDAO;
    private InternoDAO internoDAO;
    private VisitaDAO visitaDAO;
    private UsuarioDAO usuarioDAO;
    private ServicioValidacionSeguridad servicioValidacion;

    /**
     * Constructor que inicializa DAOs y servicios.
     */
    public ControladorAcceso() {
        this.visitanteDAO = new VisitanteDAO();
        this.internoDAO = new InternoDAO();
        this.visitaDAO = new VisitaDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.servicioValidacion = new ServicioValidacionSeguridad();
    }

    /**
     * Registra el ingreso de un visitante.
     * Implementa RF003: Control de Ingreso con validación de 6 pasos.
     *
     * Flujo:
     * 1. Validar permisos de acceso (6 pasos de seguridad)
     * 2. Crear registro de visita en estado PROGRAMADA
     * 3. Registrar ingreso y cambiar a EN_CURSO
     * 4. Actualizar último acceso del operador
     *
     * @param dniVisitante DNI del visitante
     * @param legajoInterno legajo del interno
     * @param nombreUsuarioOperador username del operador que registra
     * @return ID de la visita creada o null si falló
     */
    public Long registrarIngreso(String dniVisitante, String legajoInterno,
                                String nombreUsuarioOperador) {

        System.out.println("\n=== INICIANDO PROCESO DE INGRESO ===");

        // PASO 1: Validación de seguridad (6 pasos)
        ResultadoValidacion validacion = servicioValidacion.validarIngresoVisita(
            dniVisitante, legajoInterno
        );

        System.out.println(validacion.getMensajeCompleto());

        if (!validacion.isPermitido()) {
            System.err.println("✗ INGRESO DENEGADO");
            return null;
        }

        try {
            // PASO 2: Buscar entidades
            Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
            Interno interno = internoDAO.buscarPorLegajo(legajoInterno);
            Usuario operador = usuarioDAO.buscarPorNombreUsuario(nombreUsuarioOperador);

            if (visitante == null || interno == null || operador == null) {
                System.err.println("✗ Error: No se encontraron las entidades necesarias");
                return null;
            }

            // PASO 3: Crear visita
            Visita visita = new Visita(visitante, interno);
            visita.setFechaVisita(new Date());

            // Insertar en base de datos
            Long idVisita = visitaDAO.insertar(visita);

            if (idVisita == null) {
                System.err.println("✗ Error al crear el registro de visita");
                return null;
            }

            // PASO 4: Registrar ingreso (cambia estado a EN_CURSO)
            visita.setIdVisita(idVisita);
            visita.registrarIngreso(operador);

            // Actualizar en base de datos
            visitaDAO.actualizar(visita);

            // PASO 5: Actualizar último acceso del operador
            usuarioDAO.actualizarUltimoAcceso(operador.getIdUsuario());

            System.out.println("✓ INGRESO AUTORIZADO");
            System.out.println("  Visita ID: " + idVisita);
            System.out.println("  Visitante: " + visitante.getNombreCompleto());
            System.out.println("  Interno: " + interno.getNombreCompleto());
            System.out.println("  Hora de ingreso: " + visita.getHoraIngreso());
            System.out.println("  Operador: " + operador.getNombreCompleto());
            System.out.println("=====================================\n");

            return idVisita;

        } catch (SQLException e) {
            System.err.println("✗ Error de base de datos al registrar ingreso: " +
                             e.getMessage());
            return null;
        } catch (IllegalStateException e) {
            System.err.println("✗ Error en transición de estado: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registra el egreso de un visitante.
     * Implementa RF004: Control de Egreso.
     *
     * @param idVisita ID de la visita
     * @param nombreUsuarioOperador username del operador que registra
     * @param observaciones observaciones opcionales del egreso
     * @return true si se registró correctamente
     */
    public boolean registrarEgreso(Long idVisita, String nombreUsuarioOperador,
                                  String observaciones) {

        System.out.println("\n=== REGISTRANDO EGRESO ===");

        try {
            // Buscar la visita
            Visita visita = visitaDAO.buscarPorId(idVisita);

            if (visita == null) {
                System.err.println("✗ No se encontró visita con ID: " + idVisita);
                return false;
            }

            if (visita.getEstadoVisita() != EstadoVisita.EN_CURSO) {
                System.err.println("✗ La visita no está en curso. Estado actual: " +
                                 visita.getEstadoVisita());
                return false;
            }

            // Buscar operador
            Usuario operador = usuarioDAO.buscarPorNombreUsuario(nombreUsuarioOperador);

            if (operador == null) {
                System.err.println("✗ No se encontró el operador: " + nombreUsuarioOperador);
                return false;
            }

            // Registrar egreso (cambia estado a FINALIZADA)
            visita.registrarEgreso(operador);

            // Agregar observaciones si las hay
            if (observaciones != null && !observaciones.trim().isEmpty()) {
                String obsActuales = visita.getObservaciones();
                visita.setObservaciones(obsActuales != null ?
                                       obsActuales + "\n" + observaciones :
                                       observaciones);
            }

            // Actualizar en base de datos
            visitaDAO.actualizar(visita);

            // Actualizar último acceso del operador
            usuarioDAO.actualizarUltimoAcceso(operador.getIdUsuario());

            System.out.println("✓ EGRESO REGISTRADO");
            System.out.println("  Visita ID: " + idVisita);
            System.out.println("  Hora de egreso: " + visita.getHoraEgreso());
            System.out.println("  Duración: " + visita.getDuracionFormateada());
            System.out.println("  Operador: " + operador.getNombreCompleto());
            System.out.println("=========================\n");

            return true;

        } catch (SQLException e) {
            System.err.println("✗ Error de base de datos al registrar egreso: " +
                             e.getMessage());
            return false;
        } catch (IllegalStateException e) {
            System.err.println("✗ Error en transición de estado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancela una visita programada o en curso.
     *
     * @param idVisita ID de la visita
     * @param motivo motivo de la cancelación
     * @return true si se canceló correctamente
     */
    public boolean cancelarVisita(Long idVisita, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            System.err.println("✗ Debe proporcionar un motivo para la cancelación");
            return false;
        }

        try {
            Visita visita = visitaDAO.buscarPorId(idVisita);

            if (visita == null) {
                System.err.println("✗ No se encontró visita con ID: " + idVisita);
                return false;
            }

            visita.cancelar(motivo);
            visitaDAO.actualizar(visita);

            System.out.println("✓ Visita cancelada: " + motivo);
            return true;

        } catch (SQLException e) {
            System.err.println("✗ Error al cancelar visita: " + e.getMessage());
            return false;
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.err.println("✗ " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene las visitas actualmente en curso.
     * Útil para verificar capacidad (RF010).
     *
     * @return lista de visitas en curso
     */
    public List<Visita> obtenerVisitasEnCurso() {
        try {
            return visitaDAO.obtenerEnCurso();
        } catch (SQLException e) {
            System.err.println("Error al obtener visitas en curso: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Cuenta las visitas actualmente en curso.
     *
     * @return número de visitas en curso
     */
    public int contarVisitasEnCurso() {
        try {
            return visitaDAO.contarVisitasEnCurso();
        } catch (SQLException e) {
            System.err.println("Error al contar visitas en curso: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Obtiene visitas de un visitante específico.
     *
     * @param dniVisitante DNI del visitante
     * @return lista de visitas del visitante
     */
    public List<Visita> obtenerVisitasPorVisitante(String dniVisitante) {
        try {
            Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);

            if (visitante == null) {
                return List.of();
            }

            return visitaDAO.buscarPorVisitante(visitante.getIdVisitante());

        } catch (SQLException e) {
            System.err.println("Error al obtener visitas: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene visitas a un interno específico.
     *
     * @param legajoInterno legajo del interno
     * @return lista de visitas al interno
     */
    public List<Visita> obtenerVisitasPorInterno(String legajoInterno) {
        try {
            Interno interno = internoDAO.buscarPorLegajo(legajoInterno);

            if (interno == null) {
                return List.of();
            }

            return visitaDAO.buscarPorInterno(interno.getIdInterno());

        } catch (SQLException e) {
            System.err.println("Error al obtener visitas: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene las visitas de una fecha específica.
     *
     * @param fecha fecha a consultar
     * @return lista de visitas de esa fecha
     */
    public List<Visita> obtenerVisitasPorFecha(Date fecha) {
        try {
            return visitaDAO.buscarPorFecha(fecha);
        } catch (SQLException e) {
            System.err.println("Error al obtener visitas por fecha: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene internos que un visitante puede visitar.
     *
     * @param dniVisitante DNI del visitante
     * @return lista de nombres de internos autorizados
     */
    public List<String> obtenerInternosAutorizados(String dniVisitante) {
        try {
            return servicioValidacion.obtenerInternosAutorizados(dniVisitante);
        } catch (SQLException e) {
            System.err.println("Error al obtener internos autorizados: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Muestra el estado actual de una visita.
     *
     * @param idVisita ID de la visita
     */
    public void mostrarEstadoVisita(Long idVisita) {
        try {
            Visita visita = visitaDAO.buscarPorId(idVisita);

            if (visita == null) {
                System.out.println("No se encontró visita con ID: " + idVisita);
                return;
            }

            // Cargar datos completos del visitante e interno
            Visitante visitante = visitanteDAO.buscarPorId(
                visita.getVisitante().getIdVisitante()
            );
            Interno interno = internoDAO.buscarPorId(
                visita.getInterno().getIdInterno()
            );

            System.out.println("\n=== ESTADO DE LA VISITA ===");
            System.out.println("ID: " + visita.getIdVisita());
            System.out.println("Visitante: " + (visitante != null ?
                                               visitante.getNombreCompleto() : "N/A"));
            System.out.println("Interno: " + (interno != null ?
                                             interno.getNombreCompleto() : "N/A"));
            System.out.println("Fecha: " + visita.getFechaVisita());
            System.out.println("Hora ingreso: " + visita.getHoraIngreso());
            System.out.println("Hora egreso: " + (visita.getHoraEgreso() != null ?
                                                  visita.getHoraEgreso() : "En curso"));
            System.out.println("Estado: " + visita.getEstadoVisita());
            System.out.println("Duración: " + visita.getDuracionFormateada());

            if (visita.getObservaciones() != null) {
                System.out.println("Observaciones: " + visita.getObservaciones());
            }

            System.out.println("============================\n");

        } catch (SQLException e) {
            System.err.println("Error al mostrar estado de visita: " + e.getMessage());
        }
    }

    /**
     * Muestra el resumen de visitas en curso.
     */
    public void mostrarResumenVisitasEnCurso() {
        List<Visita> visitasEnCurso = obtenerVisitasEnCurso();

        System.out.println("\n=== VISITAS EN CURSO ===");
        System.out.println("Total: " + visitasEnCurso.size());

        if (visitasEnCurso.isEmpty()) {
            System.out.println("No hay visitas en curso actualmente.");
        } else {
            System.out.println("\nListado:");
            for (Visita visita : visitasEnCurso) {
                System.out.println("  - ID: " + visita.getIdVisita() +
                                 " | Ingreso: " + visita.getHoraIngreso());
            }
        }

        System.out.println("========================\n");
    }
}
