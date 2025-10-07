package com.sigvip.modelo;

import com.sigvip.modelo.enums.EstadoVisita;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * Entidad que representa un evento de visita.
 * Registra el ingreso y egreso de un visitante para ver a un interno.
 * Mapea a la tabla 'visitas' de la base de datos.
 *
 * Especificación: PDF Sección 9.2.2, página 13-14
 * Ciclo de vida: PROGRAMADA → EN_CURSO → FINALIZADA (o CANCELADA)
 */
public class Visita {

    // Atributos según especificación de tabla 'visitas'
    private Long idVisita;
    private Visitante visitante;
    private Interno interno;
    private Date fechaVisita;
    private Date horaIngreso;
    private Date horaEgreso;
    private EstadoVisita estadoVisita;
    private Usuario operadorIngreso;
    private Usuario operadorEgreso;
    private String observaciones;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Visita() {
        this.estadoVisita = EstadoVisita.PROGRAMADA;
    }

    /**
     * Constructor para crear una nueva visita programada.
     *
     * @param visitante persona que visita
     * @param interno persona visitada
     */
    public Visita(Visitante visitante, Interno interno) {
        this();
        this.visitante = visitante;
        this.interno = interno;
        this.fechaVisita = new Date();
    }

    // ===== MÉTODOS DE NEGOCIO Y GESTIÓN DE ESTADO =====

    /**
     * Registra el ingreso del visitante.
     * Transición: PROGRAMADA → EN_CURSO
     * RF003: Control de Ingreso
     *
     * @param operador usuario que registra el ingreso
     * @throws IllegalStateException si el estado no permite la transición
     */
    public void registrarIngreso(Usuario operador) {
        if (!estadoVisita.puedeTransicionarA(EstadoVisita.EN_CURSO)) {
            throw new IllegalStateException(
                "No se puede registrar ingreso desde el estado " + estadoVisita);
        }

        if (operador == null) {
            throw new IllegalArgumentException("Debe especificarse el operador que registra el ingreso");
        }

        this.horaIngreso = new Date();
        this.estadoVisita = EstadoVisita.EN_CURSO;
        this.operadorIngreso = operador;
    }

    /**
     * Registra el egreso del visitante.
     * Transición: EN_CURSO → FINALIZADA
     * RF004: Control de Egreso
     *
     * @param operador usuario que registra el egreso
     * @throws IllegalStateException si el estado no permite la transición
     */
    public void registrarEgreso(Usuario operador) {
        if (!estadoVisita.puedeTransicionarA(EstadoVisita.FINALIZADA)) {
            throw new IllegalStateException(
                "No se puede registrar egreso desde el estado " + estadoVisita);
        }

        if (operador == null) {
            throw new IllegalArgumentException("Debe especificarse el operador que registra el egreso");
        }

        if (horaIngreso == null) {
            throw new IllegalStateException("No se puede registrar egreso sin ingreso previo");
        }

        this.horaEgreso = new Date();
        this.estadoVisita = EstadoVisita.FINALIZADA;
        this.operadorEgreso = operador;
    }

    /**
     * Cancela la visita.
     * Transiciones posibles: PROGRAMADA → CANCELADA o EN_CURSO → CANCELADA
     *
     * @param motivo razón de la cancelación
     * @throws IllegalArgumentException si no se proporciona motivo
     */
    public void cancelar(String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionarse un motivo para la cancelación");
        }

        if (!estadoVisita.puedeTransicionarA(EstadoVisita.CANCELADA)) {
            throw new IllegalStateException(
                "No se puede cancelar una visita en estado " + estadoVisita);
        }

        this.estadoVisita = EstadoVisita.CANCELADA;
        this.observaciones = "CANCELADA: " + motivo +
                           (observaciones != null ? "\n" + observaciones : "");

        // Si estaba en curso, registrar hora de egreso
        if (horaIngreso != null && horaEgreso == null) {
            this.horaEgreso = new Date();
        }
    }

    /**
     * Calcula la duración de la visita.
     * Equivalente a SQL: TIMEDIFF(hora_egreso, hora_ingreso)
     *
     * @return duración de la visita, o null si no ha finalizado
     */
    public Duration calcularDuracion() {
        if (horaIngreso == null || horaEgreso == null) {
            return null;
        }

        LocalTime ingreso = horaIngreso.toInstant()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalTime();

        LocalTime egreso = horaEgreso.toInstant()
                                      .atZone(ZoneId.systemDefault())
                                      .toLocalTime();

        return Duration.between(ingreso, egreso);
    }

    /**
     * Obtiene la duración formateada de la visita.
     *
     * @return duración en formato "Xh Ym" o "No finalizada"
     */
    public String getDuracionFormateada() {
        Duration duracion = calcularDuracion();

        if (duracion == null) {
            return "No finalizada";
        }

        long horas = duracion.toHours();
        long minutos = duracion.toMinutesPart();

        return horas + "h " + minutos + "m";
    }

    /**
     * Valida si el horario de la visita está dentro del horario permitido.
     * Verifica contra los horarios del establecimiento.
     *
     * @return true si el horario es válido
     */
    public boolean validarHorarioPermitido() {
        if (horaIngreso == null || interno == null ||
            interno.getEstablecimiento() == null) {
            return false;
        }

        Establecimiento estab = interno.getEstablecimiento();
        return estab.horarioPermiteVisita(horaIngreso);
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdVisita() {
        return idVisita;
    }

    public void setIdVisita(Long idVisita) {
        this.idVisita = idVisita;
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

    public Date getFechaVisita() {
        return fechaVisita;
    }

    public void setFechaVisita(Date fechaVisita) {
        this.fechaVisita = fechaVisita;
    }

    public Date getHoraIngreso() {
        return horaIngreso;
    }

    public void setHoraIngreso(Date horaIngreso) {
        this.horaIngreso = horaIngreso;
    }

    public Date getHoraEgreso() {
        return horaEgreso;
    }

    public void setHoraEgreso(Date horaEgreso) {
        this.horaEgreso = horaEgreso;
    }

    public EstadoVisita getEstadoVisita() {
        return estadoVisita;
    }

    public void setEstadoVisita(EstadoVisita estadoVisita) {
        this.estadoVisita = estadoVisita;
    }

    public Usuario getOperadorIngreso() {
        return operadorIngreso;
    }

    public void setOperadorIngreso(Usuario operadorIngreso) {
        this.operadorIngreso = operadorIngreso;
    }

    public Usuario getOperadorEgreso() {
        return operadorEgreso;
    }

    public void setOperadorEgreso(Usuario operadorEgreso) {
        this.operadorEgreso = operadorEgreso;
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
        Visita visita = (Visita) o;
        return Objects.equals(idVisita, visita.idVisita);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVisita);
    }

    @Override
    public String toString() {
        return "Visita{" +
                "id=" + idVisita +
                ", visitante=" + (visitante != null ? visitante.getDni() : "null") +
                ", interno=" + (interno != null ? interno.getNumeroLegajo() : "null") +
                ", fecha=" + fechaVisita +
                ", estado=" + estadoVisita +
                ", duracion=" + getDuracionFormateada() +
                '}';
    }
}
