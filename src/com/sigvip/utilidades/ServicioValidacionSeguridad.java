package com.sigvip.utilidades;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.EstadoVisitante;
import com.sigvip.persistencia.*;

import java.sql.SQLException;
import java.util.ArrayList;
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
    public ResultadoValidacion validarIngresoVisita(String dniVisitante, String legajoInterno) {
        ResultadoValidacion resultado = new ResultadoValidacion();

        try {
            // PASO 1: Buscar y validar visitante
            Visitante visitante = visitanteDAO.buscarPorDni(dniVisitante);
            if (visitante == null) {
                resultado.agregarError("No existe un visitante registrado con DNI: " + dniVisitante);
                return resultado;
            }

            if (visitante.getEstado() != EstadoVisitante.HABILITADO) {
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
            Autorizacion autorizacion = autorizacionDAO.buscarPorVisitanteInterno(
                visitante.getIdVisitante(),
                interno.getIdInterno()
            );

            if (autorizacion == null) {
                resultado.agregarError("No existe autorización para que " +
                                      visitante.getNombreCompleto() +
                                      " visite a " + interno.getNombreCompleto());
                return resultado;
            }

            if (!autorizacion.estaVigente()) {
                resultado.agregarError("La autorización no está vigente. Estado: " +
                                      autorizacion.getEstado());
                if (autorizacion.estaVencida()) {
                    resultado.agregarAdvertencia("La autorización venció el: " +
                                                autorizacion.getFechaVencimiento());
                }
                return resultado;
            }

            // PASO 5: Validar horario del establecimiento
            if (interno.getEstablecimiento() != null) {
                Establecimiento establecimiento = establecimientoDAO.buscarPorId(
                    interno.getEstablecimiento().getIdEstablecimiento()
                );

                if (establecimiento != null) {
                    Date ahora = new Date();
                    if (!establecimiento.horarioPermiteVisita(ahora)) {
                        resultado.agregarError("Horario de visita no permitido. " +
                                              "Horario habilitado: " +
                                              establecimiento.getHorarioFormateado());
                        return resultado;
                    }
                }
            }

            // PASO 6: Verificar capacidad del establecimiento
            if (interno.getEstablecimiento() != null) {
                Establecimiento establecimiento = establecimientoDAO.buscarPorId(
                    interno.getEstablecimiento().getIdEstablecimiento()
                );

                if (establecimiento != null && establecimiento.getCapacidadMaxima() > 0) {
                    int visitasEnCurso = visitaDAO.contarVisitasEnCurso();

                    if (establecimiento.capacidadAlcanzada(visitasEnCurso)) {
                        resultado.agregarError("Capacidad máxima del establecimiento alcanzada (" +
                                              visitasEnCurso + "/" +
                                              establecimiento.getCapacidadMaxima() + ")");
                        return resultado;
                    }

                    // Advertencia si está cerca del límite
                    double porcentajeOcupacion = (visitasEnCurso * 100.0) /
                                                 establecimiento.getCapacidadMaxima();
                    if (porcentajeOcupacion >= 80) {
                        resultado.agregarAdvertencia("Capacidad al " + (int)porcentajeOcupacion +
                                                    "% (" + visitasEnCurso + "/" +
                                                    establecimiento.getCapacidadMaxima() + ")");
                    }
                }
            }

            // Todas las validaciones pasaron
            resultado.agregarAdvertencia("Autorización tipo: " + autorizacion.getTipoRelacion());
            resultado.agregarAdvertencia("Interno ubicado en: " + interno.getUbicacionCompleta());

        } catch (SQLException e) {
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
}
