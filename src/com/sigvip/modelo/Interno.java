package com.sigvip.modelo;

import com.sigvip.modelo.enums.EstadoInterno;
import com.sigvip.modelo.enums.SituacionProcesal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un interno (persona privada de libertad) en el sistema.
 * Mapea a la tabla 'internos' de la base de datos.
 *
 * Especificación: PDF Sección 9.2.2, página 13
 */
public class Interno {

    // Atributos según especificación de tabla 'internos'
    private Long idInterno;
    private String numeroLegajo;
    private String apellido;
    private String nombre;
    private String dni;
    private Establecimiento establecimiento;
    private String pabellonActual;
    private int pisoActual;
    private Date fechaIngreso;
    private String unidadProcedencia;
    private SituacionProcesal situacionProcesal;
    private EstadoInterno estado;

    // Relaciones
    private List<Autorizacion> autorizaciones;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Interno() {
        this.autorizaciones = new ArrayList<>();
        this.estado = EstadoInterno.ACTIVO;
    }

    /**
     * Constructor con datos mínimos requeridos.
     */
    public Interno(String numeroLegajo, String apellido, String nombre, String dni) {
        this();
        this.numeroLegajo = numeroLegajo;
        this.apellido = apellido;
        this.nombre = nombre;
        this.dni = dni;
    }

    // ===== MÉTODOS DE VALIDACIÓN Y NEGOCIO =====

    /**
     * Verifica si el interno está disponible para recibir visitas.
     * Requisito de negocio crítico (RF003, RF010).
     *
     * @return true si puede recibir visitas
     */
    public boolean estaDisponibleParaVisita() {
        // Solo los internos activos pueden recibir visitas
        return estado != null && estado.puedeRecibirVisitas();
    }

    /**
     * Actualiza la ubicación actual del interno dentro del establecimiento.
     *
     * @param pabellon nuevo pabellón/módulo
     * @param piso nuevo piso/nivel
     */
    public void actualizarUbicacion(String pabellon, int piso) {
        if (pabellon == null || pabellon.trim().isEmpty()) {
            throw new IllegalArgumentException("El pabellón no puede estar vacío");
        }
        if (piso < 0) {
            throw new IllegalArgumentException("El piso debe ser un valor positivo");
        }

        this.pabellonActual = pabellon;
        this.pisoActual = piso;
    }

    /**
     * Obtiene las autorizaciones vigentes para este interno.
     * Filtrado de negocio para solo autorizaciones activas.
     *
     * @return lista de autorizaciones vigentes
     */
    public List<Autorizacion> obtenerAutorizacionesVigentes() {
        if (autorizaciones == null || autorizaciones.isEmpty()) {
            return new ArrayList<>();
        }

        return autorizaciones.stream()
                            .filter(Autorizacion::estaVigente)
                            .toList();
    }

    /**
     * Verifica si un visitante específico está autorizado para visitar a este interno.
     *
     * @param idVisitante ID del visitante a verificar
     * @return true si existe autorización vigente
     */
    public boolean tieneAutorizacionVigentePara(Long idVisitante) {
        if (idVisitante == null || autorizaciones == null) {
            return false;
        }

        return autorizaciones.stream()
                            .anyMatch(auth -> auth.estaVigente() &&
                                            auth.getVisitante() != null &&
                                            idVisitante.equals(auth.getVisitante().getIdVisitante()));
    }

    /**
     * Obtiene el nombre completo del interno.
     *
     * @return apellido, nombre
     */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    /**
     * Obtiene la ubicación completa del interno.
     *
     * @return pabellón y piso en formato legible
     */
    public String getUbicacionCompleta() {
        return "Pabellón " + pabellonActual + " - Piso " + pisoActual;
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdInterno() {
        return idInterno;
    }

    public void setIdInterno(Long idInterno) {
        this.idInterno = idInterno;
    }

    public String getNumeroLegajo() {
        return numeroLegajo;
    }

    public void setNumeroLegajo(String numeroLegajo) {
        this.numeroLegajo = numeroLegajo;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public String getPabellonActual() {
        return pabellonActual;
    }

    public void setPabellonActual(String pabellonActual) {
        this.pabellonActual = pabellonActual;
    }

    public int getPisoActual() {
        return pisoActual;
    }

    public void setPisoActual(int pisoActual) {
        this.pisoActual = pisoActual;
    }

    public Date getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(Date fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public String getUnidadProcedencia() {
        return unidadProcedencia;
    }

    public void setUnidadProcedencia(String unidadProcedencia) {
        this.unidadProcedencia = unidadProcedencia;
    }

    public SituacionProcesal getSituacionProcesal() {
        return situacionProcesal;
    }

    public void setSituacionProcesal(SituacionProcesal situacionProcesal) {
        this.situacionProcesal = situacionProcesal;
    }

    public EstadoInterno getEstado() {
        return estado;
    }

    public void setEstado(EstadoInterno estado) {
        this.estado = estado;
    }

    public List<Autorizacion> getAutorizaciones() {
        return autorizaciones;
    }

    public void setAutorizaciones(List<Autorizacion> autorizaciones) {
        this.autorizaciones = autorizaciones;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interno interno = (Interno) o;
        // Basado en numeroLegajo que es UNIQUE en la base de datos
        return Objects.equals(numeroLegajo, interno.numeroLegajo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numeroLegajo);
    }

    @Override
    public String toString() {
        return "Interno{" +
                "idInterno=" + idInterno +
                ", numeroLegajo='" + numeroLegajo + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", ubicacion='" + getUbicacionCompleta() + '\'' +
                ", estado=" + estado +
                ", situacionProcesal=" + situacionProcesal +
                '}';
    }
}
