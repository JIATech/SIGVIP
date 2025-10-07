package com.sigvip.modelo;

import com.sigvip.modelo.enums.AplicableA;
import com.sigvip.modelo.enums.TipoRestriccion;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Entidad que representa una restricción de acceso para un visitante.
 * Impide que un visitante pueda ingresar a visitar.
 * Mapea a la tabla 'restricciones' de la base de datos.
 *
 * Especificación: PDF Sección 10.3, tabla 'restricciones'
 */
public class Restriccion {

    // Atributos según especificación de tabla 'restricciones'
    private Long idRestriccion;
    private Visitante visitante;
    private TipoRestriccion tipoRestriccion;
    private String motivo;
    private Date fechaInicio;
    private Date fechaFin;  // NULL = restricción indefinida
    private AplicableA aplicableA;
    private Interno interno;  // Solo si aplicableA = INTERNO_ESPECIFICO
    private boolean activa;
    private Usuario creadoPor;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Restriccion() {
        this.activa = true;
        this.aplicableA = AplicableA.TODOS;
        this.fechaInicio = new Date();
    }

    /**
     * Constructor para restricción aplicable a todos los internos.
     *
     * @param visitante visitante restringido
     * @param tipoRestriccion clasificación de la restricción
     * @param motivo justificación detallada
     */
    public Restriccion(Visitante visitante, TipoRestriccion tipoRestriccion, String motivo) {
        this();
        this.visitante = visitante;
        this.tipoRestriccion = tipoRestriccion;
        this.motivo = motivo;
    }

    /**
     * Constructor para restricción aplicable a un interno específico.
     *
     * @param visitante visitante restringido
     * @param tipoRestriccion clasificación de la restricción
     * @param motivo justificación detallada
     * @param interno interno específico al que aplica la restricción
     */
    public Restriccion(Visitante visitante, TipoRestriccion tipoRestriccion,
                       String motivo, Interno interno) {
        this(visitante, tipoRestriccion, motivo);
        this.aplicableA = AplicableA.INTERNO_ESPECIFICO;
        this.interno = interno;
    }

    // ===== MÉTODOS DE VALIDACIÓN Y NEGOCIO =====

    /**
     * Verifica si la restricción está activa en el momento actual.
     * Método crítico para validación de ingreso (RF003).
     *
     * Condiciones para estar activa:
     * 1. Campo 'activa' debe ser true
     * 2. Fecha actual >= fechaInicio
     * 3. Si tiene fechaFin, fecha actual <= fechaFin
     *
     * @return true si la restricción está vigente y activa
     */
    public boolean estaActiva() {
        if (!activa) {
            return false;
        }

        LocalDate hoy = LocalDate.now();

        // Verificar que ya comenzó
        LocalDate inicio = fechaInicio.toInstant()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDate();

        if (hoy.isBefore(inicio)) {
            return false;  // Aún no comenzó
        }

        // Si no tiene fecha fin, es indefinida y está activa
        if (fechaFin == null) {
            return true;
        }

        // Verificar que no terminó
        LocalDate fin = fechaFin.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

        return !hoy.isAfter(fin);
    }

    /**
     * Verifica si esta restricción aplica para un interno específico.
     *
     * @param interno interno a verificar
     * @return true si la restricción impide visitar a ese interno
     */
    public boolean aplicaParaInterno(Interno interno) {
        if (!estaActiva()) {
            return false;
        }

        // Si aplica a todos, siempre es true
        if (aplicableA == AplicableA.TODOS) {
            return true;
        }

        // Si es específica, verificar que sea el mismo interno
        return aplicableA == AplicableA.INTERNO_ESPECIFICO &&
               this.interno != null &&
               Objects.equals(this.interno.getIdInterno(), interno.getIdInterno());
    }

    /**
     * Levanta (desactiva) la restricción.
     *
     * @param motivo razón por la cual se levanta
     */
    public void levantar(String motivo) {
        this.activa = false;
        this.motivo += "\nLEVANTADA: " + motivo;
        this.fechaFin = new Date();  // Establecer fecha fin al momento actual
    }

    /**
     * Extiende la restricción estableciendo una nueva fecha de finalización.
     *
     * @param nuevaFechaFin nueva fecha límite (null = indefinida)
     * @throws IllegalStateException si la restricción no está activa
     */
    public void extender(Date nuevaFechaFin) {
        if (!activa) {
            throw new IllegalStateException("No se puede extender una restricción inactiva");
        }

        this.fechaFin = nuevaFechaFin;
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdRestriccion() {
        return idRestriccion;
    }

    public void setIdRestriccion(Long idRestriccion) {
        this.idRestriccion = idRestriccion;
    }

    public Visitante getVisitante() {
        return visitante;
    }

    public void setVisitante(Visitante visitante) {
        this.visitante = visitante;
    }

    public TipoRestriccion getTipoRestriccion() {
        return tipoRestriccion;
    }

    public void setTipoRestriccion(TipoRestriccion tipoRestriccion) {
        this.tipoRestriccion = tipoRestriccion;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public AplicableA getAplicableA() {
        return aplicableA;
    }

    public void setAplicableA(AplicableA aplicableA) {
        this.aplicableA = aplicableA;
    }

    public Interno getInterno() {
        return interno;
    }

    public void setInterno(Interno interno) {
        this.interno = interno;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Usuario getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Restriccion that = (Restriccion) o;
        return Objects.equals(idRestriccion, that.idRestriccion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRestriccion);
    }

    @Override
    public String toString() {
        return "Restriccion{" +
                "id=" + idRestriccion +
                ", visitante=" + (visitante != null ? visitante.getDni() : "null") +
                ", tipo=" + tipoRestriccion +
                ", activa=" + estaActiva() +
                ", aplicableA=" + aplicableA +
                (interno != null ? ", interno=" + interno.getNumeroLegajo() : "") +
                '}';
    }
}
