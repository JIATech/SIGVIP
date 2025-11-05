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
 * <p><b>TP4 - Herencia de Clase Abstracta:</b></p>
 * Esta clase hereda de EntidadBase el campo activo (activa en BD).
 *
 * Especificación: PDF Sección 10.3, tabla 'restricciones'
 */
public class Restriccion extends EntidadBase {

    // Atributos según especificación de tabla 'restricciones'
    private Long idRestriccion;
    private Visitante visitante;
    private TipoRestriccion tipoRestriccion;
    private String motivo;
    private Date fechaInicio;
    private Date fechaFin;  // NULL = restricción indefinida
    private AplicableA aplicableA;
    private Interno interno;  // Solo si aplicableA = INTERNO_ESPECIFICO
    // activo heredado de EntidadBase (llamado 'activa' en BD)
    private Usuario creadoPor;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Restriccion() {
        super(); // Inicializa activo=true
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
     * 1. Campo 'activo' (heredado) debe ser true
     * 2. Fecha actual >= fechaInicio
     * 3. Si tiene fechaFin, fecha actual <= fechaFin
     *
     * @return true si la restricción está vigente y activa
     */
    public boolean estaActiva() {
        if (!isActivo()) { // Usa isActivo() heredado de EntidadBase
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
        setActivo(false); // Usa setActivo() heredado de EntidadBase
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
        if (!isActivo()) { // Usa isActivo() heredado de EntidadBase
            throw new IllegalStateException("No se puede extender una restricción inactiva");
        }

        this.fechaFin = nuevaFechaFin;
    }

    // ===== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS DE EntidadBase =====

    /**
     * Valida las reglas de negocio para una restricción.
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

        if (tipoRestriccion == null) {
            errores.append("Tipo de restricción obligatorio. ");
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            errores.append("Motivo obligatorio. ");
        }

        if (fechaInicio == null) {
            errores.append("Fecha de inicio obligatoria. ");
        }

        if (aplicableA == null) {
            errores.append("Aplicable a obligatorio. ");
        }

        if (aplicableA == AplicableA.INTERNO_ESPECIFICO && interno == null) {
            errores.append("Debe especificar el interno para restricciones específicas. ");
        }

        if (errores.length() > 0) {
            throw new IllegalStateException("Restricción inválida: " + errores.toString());
        }

        return true;
    }

    /**
     * Obtiene un resumen textual de la restricción para logs y auditoría.
     *
     * @return String con resumen de la entidad
     */
    @Override
    public String obtenerResumen() {
        return String.format("Restriccion[ID=%d, Visitante=%s, Tipo=%s, Aplicable=%s, Activa=%s]",
                idRestriccion != null ? idRestriccion : 0L,
                visitante != null ? visitante.getNombreCompleto() : "N/A",
                tipoRestriccion != null ? tipoRestriccion : "N/A",
                aplicableA != null ? aplicableA : "N/A",
                estaActiva() ? "SÍ" : "NO");
    }

    /**
     * Verifica si la restricción es nueva (aún no persistida).
     *
     * @return true si idRestriccion es null
     */
    @Override
    public boolean esNuevo() {
        return idRestriccion == null;
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

    // isActiva() renombrado: usa isActivo() heredado de EntidadBase
    // setActiva() renombrado: usa setActivo() heredado de EntidadBase

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
