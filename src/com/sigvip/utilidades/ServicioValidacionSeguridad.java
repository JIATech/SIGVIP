package com.sigvip.utilidades;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.EstadoVisitante;
import com.sigvip.modelo.enums.EstadoAutorizacion;
import com.sigvip.modelo.enums.TipoRelacion;
import com.sigvip.modelo.enums.Rol;
import com.sigvip.persistencia.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Servicio de validación de seguridad para el control de acceso de visitantes.
 * Implementa el flujo de validación de 6 pasos especificado en RF003.
 *
 * Especificación: PDF Sección 9.2.1 - RF003: Control de Ingreso
 * Responsabilidad: Coordinar validaciones de seguridad entre múltiples entidades
 */
public class ServicioValidacionSeguridad {

    private VisitanteDAO visitanteDAO;
    private AutorizacionDAO autorizacionDAO;
    private RestriccionDAO restriccionDAO;
    private InternoDAO internoDAO;
    private VisitaDAO visitaDAO;
    private EstablecimientoDAO establecimientoDAO;

    /**
     * Constructor que inicializa todos los DAOs necesarios.
     */
    public ServicioValidacionSeguridad() {
        this.visitanteDAO = new VisitanteDAO();
        this.autorizacionDAO = new AutorizacionDAO();
        this.restriccionDAO = new RestriccionDAO();
        this.internoDAO = new InternoDAO();
        this.visitaDAO = new VisitaDAO();
        this.establecimientoDAO = new EstablecimientoDAO();
    }

    /**
     * Resultado de la validación de ingreso.
     * Contiene el estado (permitido/denegado) y los mensajes de error/advertencia.
     */
    public static class ResultadoValidacion {
        private boolean permitido;
        private List<String> errores;
        private List<String> advertencias;

        // Campos para autorización inmediata
        private boolean requiereAutorizacionInmediata;
        private Visitante visitante;
        private Interno interno;
        private Usuario operador;

        public ResultadoValidacion() {
            this.errores = new ArrayList<>();
            this.advertencias = new ArrayList<>();
            this.permitido = true;
        }

        public void agregarError(String error) {
            this.errores.add(error);
            this.permitido = false;
        }

        public void agregarAdvertencia(String advertencia) {
            this.advertencias.add(advertencia);
        }

        public boolean isPermitido() {
            return permitido;
        }

        public List<String> getErrores() {
            return errores;
        }

        public List<String> getAdvertencias() {
            return advertencias;
        }

        public boolean tieneErrores() {
            return !errores.isEmpty();
        }

        public boolean tieneAdvertencias() {
            return !advertencias.isEmpty();
        }

        // Métodos para autorización inmediata
        public boolean requiereAutorizacionInmediata() {
            return requiereAutorizacionInmediata;
        }

        public void setRequiereAutorizacionInmediata(boolean requiereAutorizacionInmediata) {
            this.requiereAutorizacionInmediata = requiereAutorizacionInmediata;
        }

        public void setDatosAutorizacionInmediata(Visitante visitante, Interno interno, Usuario operador) {
            this.visitante = visitante;
            this.interno = interno;
            this.operador = operador;
            this.requiereAutorizacionInmediata = true;
        }

        public Visitante getVisitante() {
            return visitante;
        }

        public Interno getInterno() {
            return interno;
        }

        public Usuario getOperador() {
            return operador;
        }

        public String getMensajeCompleto() {
            StringBuilder sb = new StringBuilder();

            if (tieneErrores()) {
                sb.append("ERRORES:\n");
                for (String error : errores) {
                    sb.append("  ✗ ").append(error).append("\n");
                }
            }

            if (tieneAdvertencias()) {
                sb.append("ADVERTENCIAS:\n");
                for (String advertencia : advertencias) {
                    sb.append("  ⚠ ").append(advertencia).append("\n");
                }
            }

            if (!tieneErrores() && !tieneAdvertencias()) {
                sb.append("✓ Validación exitosa - Ingreso permitido");
            }

            return sb.toString();
        }
    }

    /**
     * Valida si un visitante puede ingresar a visitar a un interno.
     * Implementa el flujo completo de 6 pasos de RF003.
     *
     * Flujo de validación:
     * 1. Visitante está HABILITADO
     * 2. Visitante no tiene restricciones activas que apliquen
     * 3. Existe autorización vigente visitante-interno
     * 4. Interno está disponible para recibir visitas
     * 5. Horario dentro del permitido por establecimiento
     * 6. Capacidad del establecimiento no superada
     *
     * @param dniVisitante DNI del visitante
     * @param legajoInterno número de legajo del interno
     * @return resultado de la validación
     */
    public ResultadoValidacion validarIngresoVisita(String dniVisitante, String legajoInterno, Usuario operador) {
        ResultadoValidacion resultado = new ResultadoValidacion();
        System.out.println("DEBUG: Iniciando validarIngresoVisita - DNI: " + dniVisitante + ", Legajo: " + legajoInterno +
                         ", Operador: " + (operador != null ? operador.getNombreCompleto() : "N/A") +
                         ", Rol: " + (operador != null ? operador.getRol() : "N/A"));

        try {
            // PASO 1: Buscar y validar visitante
            Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
            if (visitante == null) {
                resultado.agregarError("No existe un visitante registrado con DNI: " + dniVisitante);
                return resultado;
            }

            if (visitante.getEstado() != EstadoVisitante.ACTIVO) {
                resultado.agregarError("El visitante no está habilitado. Estado actual: " +
                                      visitante.getEstado());
                return resultado;
            }

            // PASO 2: Buscar y validar interno
            Interno interno = internoDAO.buscarPorLegajo(legajoInterno);
            if (interno == null) {
                resultado.agregarError("No existe un interno registrado con legajo: " + legajoInterno);
                return resultado;
            }

            if (!interno.estaDisponibleParaVisita()) {
                resultado.agregarError("El interno no está disponible para recibir visitas. " +
                                      "Estado: " + interno.getEstado());
                return resultado;
            }

            // PASO 3: Verificar restricciones activas
            List<Restriccion> restriccionesAplicables = restriccionDAO.obtenerRestriccionesAplicables(
                visitante.getIdVisitante(),
                interno.getIdInterno()
            );

            if (!restriccionesAplicables.isEmpty()) {
                for (Restriccion restriccion : restriccionesAplicables) {
                    resultado.agregarError("Restricción activa: " +
                                          restriccion.getTipoRestriccion() +
                                          " - Motivo: " + restriccion.getMotivo());
                }
                return resultado;
            }

            // PASO 4: Verificar autorización vigente
            System.out.println("DEBUG: Buscando autorización - Visitante ID: " + visitante.getIdVisitante() +
                             ", Interno ID: " + interno.getIdInterno());

            Autorizacion autorizacion = autorizacionDAO.buscarPorVisitanteInterno(
                visitante.getIdVisitante(),
                interno.getIdInterno()
            );

            System.out.println("DEBUG: Autorización encontrada: " + (autorizacion != null ? "SI" : "NO"));
            if (autorizacion != null) {
                System.out.println("DEBUG: Estado autorización: " + autorizacion.getEstado());
                System.out.println("DEBUG: Vigente: " + autorizacion.estaVigente());
            }

            if (autorizacion == null) {
                // Verificar si el operador puede autorizar inmediatamente
                if (operador != null && (operador.getRol() == Rol.ADMINISTRADOR || operador.getRol() == Rol.SUPERVISOR)) {
                    System.out.println("🚨 AUTORIZACIÓN INMEDIATA POSIBLE - Operador con privilegios: " + operador.getRol());

                    // Configurar datos para autorización inmediata (sin crearla aún)
                    resultado.setDatosAutorizacionInmediata(visitante, interno, operador);
                    resultado.agregarAdvertencia("ADVERTENCIA: El visitante no tiene autorización previa. Como " + operador.getRol() +
                                               ", puedes autorizar esta visita inmediatamente.");
                    // Continuar con las demás validaciones (horario, capacidad, etc.)
                } else {
                    resultado.agregarError("No existe autorización para que " +
                                          visitante.getNombreCompleto() +
                                          " visite a " + interno.getNombreCompleto() +
                                          ". Solo ADMINISTRADOR y SUPERVISOR pueden autorizar inmediatamente.");
                    return resultado;
                }
            } else {
                // Si existe autorización, verificar su vigencia
                if (!autorizacion.estaVigente()) {
                    resultado.agregarError("La autorización no está vigente. Estado: " +
                                          autorizacion.getEstado());
                    if (autorizacion.estaVencida()) {
                        resultado.agregarAdvertencia("La autorización venció el: " +
                                                    autorizacion.getFechaVencimiento());
                    }
                    return resultado;
                }
            }

            // PASO 5: Validar horario del establecimiento (con advertencia, no bloqueo)
            if (interno.getEstablecimiento() != null) {
                Establecimiento establecimiento = establecimientoDAO.buscarPorId(
                    interno.getEstablecimiento().getIdEstablecimiento()
                );

                if (establecimiento != null) {
                    Date ahora = new Date();
                    if (!establecimiento.horarioPermiteVisita(ahora)) {
                        resultado.agregarAdvertencia("ADVERTENCIA: Visita fuera de horario habitual. " +
                                                   "Horario habilitado: " +
                                                   establecimiento.getHorarioFormateado() +
                                                   " - Se permite el ingreso por excepción.");
                        System.out.println("ADVERTENCIA: Visitante registrado fuera de horario - " +
                                         "Visitante: " + visitante.getNombreCompleto() +
                                         ", Hora actual: " + new java.text.SimpleDateFormat("HH:mm").format(ahora) +
                                         ", Horario permitido: " + establecimiento.getHorarioFormateado());
                    } else {
                        resultado.agregarAdvertencia("✓ Visita dentro del horario habilitado: " +
                                                   establecimiento.getHorarioFormateado());
                    }
                }
            }

            // Todas las validaciones pasaron
            if (autorizacion != null) {
                resultado.agregarAdvertencia("Autorización tipo: " + autorizacion.getTipoRelacion());
            } else if (resultado.requiereAutorizacionInmediata()) {
                resultado.agregarAdvertencia("Autorización: Inmediata (pendiente de confirmación)");
            }
            resultado.agregarAdvertencia("Interno ubicado en: " + interno.getUbicacionCompleta());

        } catch (SQLException e) {
            System.out.println("DEBUG: SQLException en validarIngresoVisita: " + e.getMessage());
            e.printStackTrace();
            resultado.agregarError("Error al validar ingreso: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Verifica rápidamente si un visitante tiene alguna restricción activa.
     *
     * @param dniVisitante DNI del visitante
     * @return true si tiene restricciones activas
     * @throws SQLException si ocurre un error en la consulta
     */
    public boolean tieneRestriccionesActivas(String dniVisitante) throws SQLException {
        Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
        if (visitante == null) {
            return false;
        }

        List<Restriccion> restricciones = restriccionDAO.obtenerActivasPorVisitante(
            visitante.getIdVisitante()
        );

        return !restricciones.isEmpty();
    }

    /**
     * Verifica si existe autorización vigente entre visitante e interno.
     *
     * @param dniVisitante DNI del visitante
     * @param legajoInterno legajo del interno
     * @return true si existe autorización vigente
     * @throws SQLException si ocurre un error en la consulta
     */
    public boolean tieneAutorizacionVigente(String dniVisitante, String legajoInterno)
            throws SQLException {
        Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
        Interno interno = internoDAO.buscarPorLegajo(legajoInterno);

        if (visitante == null || interno == null) {
            return false;
        }

        Autorizacion autorizacion = autorizacionDAO.buscarPorVisitanteInterno(
            visitante.getIdVisitante(),
            interno.getIdInterno()
        );

        return autorizacion != null && autorizacion.estaVigente();
    }

    /**
     * Obtiene la lista de internos que un visitante puede visitar.
     *
     * @param dniVisitante DNI del visitante
     * @return lista de nombres completos de internos autorizados
     * @throws SQLException si ocurre un error
     */
    public List<String> obtenerInternosAutorizados(String dniVisitante) throws SQLException {
        List<String> autorizados = new ArrayList<>();

        Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
        if (visitante == null) {
            return autorizados;
        }

        List<Autorizacion> autorizaciones = autorizacionDAO.obtenerVigentesPorVisitante(
            visitante.getIdVisitante()
        );

        for (Autorizacion auth : autorizaciones) {
            if (auth.getInterno() != null) {
                // Cargar datos completos del interno
                Interno interno = internoDAO.buscarPorId(auth.getInterno().getIdInterno());
                if (interno != null && interno.estaDisponibleParaVisita()) {
                    autorizados.add(interno.getNombreCompleto() +
                                  " (Legajo: " + interno.getNumeroLegajo() + ")");
                }
            }
        }

        return autorizados;
    }

    /**
     * Valida si el horario actual permite visitas en un establecimiento.
     *
     * @param idEstablecimiento ID del establecimiento
     * @return resultado con detalles de validación
     */
    public ResultadoValidacion validarHorarioEstablecimiento(Long idEstablecimiento) {
        ResultadoValidacion resultado = new ResultadoValidacion();

        try {
            Establecimiento establecimiento = establecimientoDAO.buscarPorId(idEstablecimiento);

            if (establecimiento == null) {
                resultado.agregarError("Establecimiento no encontrado");
                return resultado;
            }

            if (!establecimiento.isActivo()) {
                resultado.agregarError("El establecimiento no está activo");
                return resultado;
            }

            Date ahora = new Date();
            if (!establecimiento.horarioPermiteVisita(ahora)) {
                resultado.agregarError("Fuera del horario de visitas. " +
                                      "Horario permitido: " +
                                      establecimiento.getHorarioFormateado());
            }

        } catch (SQLException e) {
            resultado.agregarError("Error al validar horario: " + e.getMessage());
        }

        return resultado;
    }

    /**
     * Crea una autorización inmediata para usuarios autorizados.
     * Vence al día siguiente a las 23:59.
     *
     * @param visitante visitante a autorizar
     * @param interno interno a visitar
     * @param autorizadoPor usuario que autoriza
     * @return autorización creada o null si hay error
     */
    public Autorizacion crearAutorizacionInmediata(Visitante visitante, Interno interno, Usuario autorizadoPor) {
        try {
            // Crear fecha de vencimiento: mañana a las 23:59
            Calendar calVencimiento = Calendar.getInstance();
            calVencimiento.add(Calendar.DAY_OF_MONTH, 1);
            calVencimiento.set(Calendar.HOUR_OF_DAY, 23);
            calVencimiento.set(Calendar.MINUTE, 59);
            calVencimiento.set(Calendar.SECOND, 0);
            calVencimiento.set(Calendar.MILLISECOND, 0);

            // Crear autorización con tipo de relación por defecto
            Autorizacion autorizacion = new Autorizacion(
                visitante,
                interno,
                TipoRelacion.OTRO // Para autorizaciones inmediatas espontáneas
            );

            // Establecer datos adicionales
            autorizacion.setDescripcionRelacion("Autorización inmediata - Visitante espontáneo - " +
                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            autorizacion.setFechaAutorizacion(new Date()); // Fecha de autorización = ahora
            autorizacion.setFechaVencimiento(calVencimiento.getTime()); // Vence mañana
            autorizacion.setAutorizadoPor(autorizadoPor);

            // Insertar autorización en la base de datos
            Long idAutorizacion = autorizacionDAO.insertar(autorizacion);

            if (idAutorizacion != null) {
                autorizacion.setIdAutorizacion(idAutorizacion);
                System.out.println("📋 Autorización inmediata creada - Tipo: " + autorizacion.getTipoRelacion() +
                                 ", Vence: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(autorizacion.getFechaVencimiento()));
                return autorizacion;
            } else {
                System.err.println("✗ Error al insertar autorización inmediata");
                return null;
            }

        } catch (SQLException e) {
            System.err.println("✗ Error al crear autorización inmediata: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
