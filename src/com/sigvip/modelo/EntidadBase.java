package com.sigvip.modelo;

import java.util.Date;

/**
 * Clase abstracta base para todas las entidades del sistema.
 *
 * <p><b>Justificación para TP4 - Uso de Clases Abstractas:</b></p>
 * Esta clase abstracta centraliza comportamiento común a todas las entidades del sistema,
 * demostrando los siguientes conceptos de POO:
 * <ul>
 *   <li><b>Abstracción</b>: Define el "contrato" que todas las entidades deben cumplir
 *       mediante los métodos abstractos validar() y obtenerResumen()</li>
 *   <li><b>Herencia</b>: Las 7 entidades del sistema (Visitante, Interno, Visita, etc.)
 *       heredan campos y métodos comunes, eliminando duplicación de código</li>
 *   <li><b>Encapsulación</b>: Centraliza la lógica de auditoría (fechas de creación/modificación)
 *       en un solo lugar</li>
 *   <li><b>Reutilización</b>: Implementa el principio DRY (Don't Repeat Yourself) al
 *       evitar repetir los mismos campos en cada entidad</li>
 * </ul>
 *
 * <p><b>Campos comunes heredados:</b></p>
 * - fechaCreacion: timestamp de creación del registro
 * - fechaModificacion: timestamp de última modificación
 * - activo: flag de borrado lógico (soft delete)
 *
 * <p><b>Métodos abstractos (deben implementarse en subclases):</b></p>
 * - validar(): Valida reglas de negocio específicas de cada entidad
 * - obtenerResumen(): Retorna representación textual resumida para logs/auditoría
 *
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 * @since TP4
 */
public abstract class EntidadBase {

    // ===== CAMPOS COMUNES =====

    /**
     * Fecha y hora de creación del registro.
     * Se inicializa automáticamente al crear la instancia.
     */
    protected Date fechaCreacion;

    /**
     * Fecha y hora de última modificación del registro.
     * Se actualiza automáticamente al llamar a marcarModificado().
     */
    protected Date fechaModificacion;

    /**
     * Flag de estado activo para borrado lógico (soft delete).
     * true = registro activo, false = registro eliminado lógicamente.
     */
    protected boolean activo;

    // ===== CONSTRUCTOR =====

    /**
     * Constructor por defecto.
     * Inicializa fechaCreacion con el timestamp actual y marca la entidad como activa.
     */
    public EntidadBase() {
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
        this.activo = true;
    }

    // ===== MÉTODOS ABSTRACTOS (DEBEN IMPLEMENTARSE EN SUBCLASES) =====

    /**
     * Valida las reglas de negocio específicas de cada entidad.
     *
     * <p>Cada subclase debe implementar sus propias validaciones.
     * Por ejemplo:</p>
     * <ul>
     *   <li>Visitante: DNI válido (7-8 dígitos), edad >= 18</li>
     *   <li>Interno: número de legajo único, fecha ingreso <= hoy</li>
     *   <li>Visita: visitante e interno no nulos, horario válido</li>
     * </ul>
     *
     * @return true si la entidad es válida, false en caso contrario
     * @throws IllegalStateException si hay errores de validación críticos
     */
    public abstract boolean validar() throws IllegalStateException;

    /**
     * Obtiene un resumen textual de la entidad para logs y auditoría.
     *
     * <p>Debe retornar una representación concisa de la entidad, útil para:</p>
     * <ul>
     *   <li>Logs de aplicación (ServicioLogs)</li>
     *   <li>Auditoría de operaciones</li>
     *   <li>Debugging y trazabilidad</li>
     * </ul>
     *
     * <p>Ejemplo de formato:</p>
     * <code>"Visitante[DNI=20345678, Nombre=González Ana María]"</code>
     *
     * @return String con resumen de la entidad
     */
    public abstract String obtenerResumen();

    // ===== MÉTODOS CONCRETOS (HEREDADOS POR TODAS LAS SUBCLASES) =====

    /**
     * Marca la entidad como modificada, actualizando el timestamp.
     *
     * <p>Se debe llamar antes de persistir cambios en la base de datos.
     * Útil para auditoría y tracking de cambios.</p>
     */
    public void marcarModificado() {
        this.fechaModificacion = new Date();
    }

    /**
     * Verifica si la entidad es nueva (aún no persistida).
     *
     * <p>Una entidad se considera nueva si su ID es null.
     * Este método debe ser sobrescrito en subclases que tengan campo ID.</p>
     *
     * @return true si la entidad no tiene ID asignado
     */
    public boolean esNuevo() {
        // Método por defecto - las subclases pueden sobrescribirlo
        // para verificar si su campo ID específico es null
        return fechaCreacion.equals(fechaModificacion);
    }

    /**
     * Marca la entidad como inactiva (borrado lógico).
     * No elimina el registro de la base de datos.
     */
    public void marcarInactivo() {
        this.activo = false;
        marcarModificado();
    }

    /**
     * Reactiva una entidad previamente marcada como inactiva.
     */
    public void marcarActivo() {
        this.activo = true;
        marcarModificado();
    }

    // ===== GETTERS Y SETTERS =====

    /**
     * Obtiene la fecha de creación del registro.
     *
     * @return fecha de creación
     */
    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    /**
     * Establece la fecha de creación del registro.
     *
     * <p><b>Advertencia:</b> Este método solo debe usarse al cargar datos
     * desde la base de datos. No modificar en operaciones normales.</p>
     *
     * @param fechaCreacion fecha de creación
     */
    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Obtiene la fecha de última modificación.
     *
     * @return fecha de modificación
     */
    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    /**
     * Establece la fecha de modificación.
     *
     * <p><b>Advertencia:</b> Este método solo debe usarse al cargar datos
     * desde la base de datos. Para marcar modificaciones, usar marcarModificado().</p>
     *
     * @param fechaModificacion fecha de modificación
     */
    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    /**
     * Verifica si la entidad está activa.
     *
     * @return true si está activa, false si está eliminada lógicamente
     */
    public boolean isActivo() {
        return activo;
    }

    /**
     * Establece el estado activo de la entidad.
     *
     * <p><b>Recomendación:</b> Preferir usar marcarActivo() o marcarInactivo()
     * que actualizan automáticamente la fecha de modificación.</p>
     *
     * @param activo true para activo, false para inactivo
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // ===== MÉTODOS DE OBJECT =====

    /**
     * Retorna una representación en String de la entidad.
     * Delega en obtenerResumen() implementado por cada subclase.
     *
     * @return representación textual de la entidad
     */
    @Override
    public String toString() {
        return obtenerResumen();
    }
}
