package com.sigvip.modelo;

import com.sigvip.modelo.enums.EstadoAutorizacion;
import com.sigvip.modelo.enums.TipoRelacion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Entidad que representa una autorización de visita.
 * Vincula un visitante con un interno específico estableciendo el permiso de visita.
 * Mapea a la tabla 'autorizaciones' de la base de datos.
 *
 * <p><b>TP4 - Herencia de Clase Abstracta:</b></p>
 * Esta clase hereda de EntidadBase, demostrando herencia y reutilización de código.
 *
 * Especificación: PDF Sección 9.2.2, página 14
 * Constraint: UNIQUE(id_visitante, id_interno) - solo una autorización por par
 */
public class Autorizacion extends EntidadBase {

    // Atributos según especificación de tabla 'autorizaciones'
    private Long idAutorizacion;
    private Visitante visitante;
    private Interno interno;
    private TipoRelacion tipoRelacion;
    private String descripcionRelacion;
    private Date fechaAutorizacion;
    private Date fechaVencimiento;  // NULL = autorización indefinida
    private EstadoAutorizacion estado;
    private Usuario autorizadoPor;
    private String observaciones;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Autorizacion() {
        super();
        this.estado = EstadoAutorizacion.VIGENTE;
        this.fechaAutorizacion = new Date();
    }

    /**
     * Constructor con datos mínimos requeridos.
     *
     * @param visitante persona autorizada a visitar
     * @param interno persona a ser visitada
     * @param tipoRelacion tipo de vínculo entre ambos
     */
    public Autorizacion(Visitante visitante, Interno interno, TipoRelacion tipoRelacion) {
        this();
        this.visitante = visitante;
        this.interno = interno;
        this.tipoRelacion = tipoRelacion;
    }

    // ===== MÉTODOS DE VALIDACIÓN Y NEGOCIO =====

    /**
     * Verifica si la autorización está vigente y permite realizar visitas.
     * Método crítico para validación de ingreso (RF003).
     *
     * Condiciones para estar vigente:
     * 1. Estado debe ser VIGENTE
     * 2. Si tiene fecha de vencimiento, no debe estar vencida
     *
     * @return true si la autorización permite visitas
     */
    public boolean estaVigente() {
        // Verificar que el estado permita visitas
        if (estado == null || !estado.permiteVisitas()) {
            return false;
        }

        // Si no tiene fecha de vencimiento, es indefinida y vigente
        if (fechaVencimiento == null) {
            return true;
        }

        // Verificar que no esté vencida
        LocalDate hoy = LocalDate.now();
        LocalDate fechaVenc = fechaVencimiento.toInstant()
                                              .atZone(ZoneId.systemDefault())
                                              .toLocalDate();

        return !fechaVenc.isBefore(hoy);
    }

    /**
     * Verifica si la autorización ha expirado por fecha de vencimiento.
     *
     * @return true si tiene fecha de vencimiento y ya pasó
     */
    public boolean estaVencida() {
        if (fechaVencimiento == null) {
            return false;  // Sin vencimiento = nunca vence
        }

        LocalDate hoy = LocalDate.now();
        LocalDate fechaVenc = fechaVencimiento.toInstant()
                                              .atZone(ZoneId.systemDefault())
                                              .toLocalDate();

        return fechaVenc.isBefore(hoy);
    }

    /**
     * Renueva la autorización estableciendo una nueva fecha de vencimiento.
     * Solo puede renovarse si está en estado VIGENTE o VENCIDA.
     *
     * @param fechaNuevoVencimiento nueva fecha de expiración (null = indefinida)
     * @throws IllegalStateException si la autorización está suspendida o revocada
     */
    public void renovar(Date fechaNuevoVencimiento) {
        if (estado == EstadoAutorizacion.SUSPENDIDA) {
            throw new IllegalStateException(
                "No se puede renovar una autorización suspendida. " +
                "Primero debe reactivarse.");
        }

        if (estado == EstadoAutorizacion.REVOCADA) {
            throw new IllegalStateException(
                "No se puede renovar una autorización revocada. " +
                "Debe crearse una nueva autorización.");
        }

        this.fechaVencimiento = fechaNuevoVencimiento;
        this.estado = EstadoAutorizacion.VIGENTE;
    }

    /**
     * Suspende temporalmente la autorización.
     *
     * @param motivo razón de la suspensión
     * @throws IllegalArgumentException si el motivo está vacío
     * @throws IllegalStateException si la autorización ya está revocada
     */
    public void suspender(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo para la suspensión");
        }

        if (estado == EstadoAutorizacion.REVOCADA) {
            throw new IllegalStateException("No se puede suspender una autorización revocada");
        }

        this.estado = EstadoAutorizacion.SUSPENDIDA;
        this.observaciones = "SUSPENDIDA: " + motivo +
                           (observaciones != null ? "\n" + observaciones : "");
    }

    /**
     * Revoca permanentemente la autorización.
     *
     * @param motivo razón de la revocación
     * @throws IllegalArgumentException si el motivo está vacío
     */
    public void revocar(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar un motivo para la revocación");
        }

        this.estado = EstadoAutorizacion.REVOCADA;
        this.observaciones = "REVOCADA: " + motivo +
                           (observaciones != null ? "\n" + observaciones : "");
    }

    /**
     * Reactiva una autorización suspendida.
     *
     * @throws IllegalStateException si no está suspendida o está vencida
     */
    public void reactivar() {
        if (estado != EstadoAutorizacion.SUSPENDIDA) {
            throw new IllegalStateException("Solo se pueden reactivar autorizaciones suspendidas");
        }

        if (estaVencida()) {
            throw new IllegalStateException("La autorización está vencida. Debe renovarse primero");
        }

        this.estado = EstadoAutorizacion.VIGENTE;
    }

    // ===== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS DE EntidadBase =====

    /**
     * Valida las reglas de negocio para una autorización.
     *
     * @return true si todas las validaciones pasan
     * @throws IllegalStateException si hay errores críticos de validación
     */
    @Override
    public boolean validar() throws IllegalStateException {
        StringBuilder errores = new StringBuilder();

        if (visitante == null) {
            errores.append("Visitante obligatorio. ");
        }

        if (interno == null) {
            errores.append("Interno obligatorio. ");
        }

        if (tipoRelacion == null) {
            errores.append("Tipo de relación obligatorio. ");
        }

        if (estado == null) {
            errores.append("Estado obligatorio. ");
        }

        if (fechaAutorizacion == null) {
            errores.append("Fecha de autorización obligatoria. ");
        }

        if (errores.length() > 0) {
            throw new IllegalStateException("Autorización inválida: " + errores.toString());
        }

        return true;
    }

    /**
     * Obtiene un resumen textual de la autorización para logs y auditoría.
     *
     * @return String con resumen de la entidad
     */
    @Override
    public String obtenerResumen() {
        return String.format("Autorizacion[ID=%d, Visitante=%s, Interno=%s, Relación=%s, Estado=%s, Vigente=%s]",
                idAutorizacion != null ? idAutorizacion : 0L,
                visitante != null ? visitante.getNombreCompleto() : "N/A",
                interno != null ? interno.getNombreCompleto() : "N/A",
                tipoRelacion != null ? tipoRelacion : "N/A",
                estado != null ? estado : "N/A",
                estaVigente() ? "SÍ" : "NO");
    }

    /**
     * Verifica si la autorización es nueva (aún no persistida).
     *
     * @return true si idAutorizacion es null
     */
    @Override
    public boolean esNuevo() {
        return idAutorizacion == null;
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdAutorizacion() {
        return idAutorizacion;
    }

    public void setIdAutorizacion(Long idAutorizacion) {
        this.idAutorizacion = idAutorizacion;
    }

    public Visitante getVisitante() {
        return visitante;
    }

    public void setVisitante(Visitante visitante) {
        this.visitante = visitante;
    }

    public Interno getInterno() {
        return interno;
    }

    public void setInterno(Interno interno) {
        this.interno = interno;
    }

    public TipoRelacion getTipoRelacion() {
        return tipoRelacion;
    }

    public void setTipoRelacion(TipoRelacion tipoRelacion) {
        this.tipoRelacion = tipoRelacion;
    }

    public String getDescripcionRelacion() {
        return descripcionRelacion;
    }

    public void setDescripcionRelacion(String descripcionRelacion) {
        this.descripcionRelacion = descripcionRelacion;
    }

    public Date getFechaAutorizacion() {
        return fechaAutorizacion;
    }

    public void setFechaAutorizacion(Date fechaAutorizacion) {
        this.fechaAutorizacion = fechaAutorizacion;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoAutorizacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoAutorizacion estado) {
        this.estado = estado;
    }

    public Usuario getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(Usuario autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Autorizacion that = (Autorizacion) o;
        // Basado en la restricción UNIQUE(id_visitante, id_interno)
        return Objects.equals(visitante, that.visitante) &&
               Objects.equals(interno, that.interno);
    }

    @Override
    public int hashCode() {
        return Objects.hash(visitante, interno);
    }

    @Override
    public String toString() {
        return "Autorizacion{" +
                "id=" + idAutorizacion +
                ", visitante=" + (visitante != null ? visitante.getNombreCompleto() : "null") +
                ", interno=" + (interno != null ? interno.getNombreCompleto() : "null") +
                ", relacion=" + tipoRelacion +
                ", estado=" + estado +
                ", vigente=" + estaVigente() +
                '}';
    }
}
