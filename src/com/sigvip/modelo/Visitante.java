package com.sigvip.modelo;

import com.sigvip.modelo.enums.EstadoVisitante;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un visitante autorizado en el sistema penitenciario.
 * Mapea a la tabla 'visitantes' de la base de datos.
 *
 * <p><b>TP4 - Herencia de Clase Abstracta:</b></p>
 * Esta clase hereda de EntidadBase, demostrando:
 * <ul>
 *   <li>Herencia de campos comunes (fechaCreacion, fechaModificacion, activo)</li>
 *   <li>Implementación de métodos abstractos (validar(), obtenerResumen())</li>
 *   <li>Reutilización de código y cumplimiento del principio DRY</li>
 * </ul>
 *
 * Especificación: PDF Sección 9.2.2, página 13
 */
public class Visitante extends EntidadBase {

    // Atributos según especificación de tabla 'visitantes'
    private Long idVisitante;
    private String dni;
    private String apellido;
    private String nombre;
    private String domicilio;
    private String telefono;
    private String email;
    private Date fechaNacimiento;
    private byte[] foto;
    private EstadoVisitante estado;

    // Relaciones
    private List<Restriccion> restricciones;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     * Llama a super() para inicializar campos de EntidadBase.
     */
    public Visitante() {
        super(); // Inicializa fechaCreacion, fechaModificacion, activo
        this.restricciones = new ArrayList<>();
        this.estado = EstadoVisitante.ACTIVO;
    }

    /**
     * Constructor con datos mínimos requeridos para registro.
     */
    public Visitante(String dni, String apellido, String nombre, Date fechaNacimiento) {
        this();
        this.dni = dni;
        this.apellido = apellido;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
    }

    // ===== MÉTODOS DE VALIDACIÓN DE NEGOCIO =====

    /**
     * Valida el formato del DNI argentino.
     * Debe ser numérico y tener entre 7 y 8 dígitos.
     *
     * @return true si el DNI es válido
     */
    public boolean validarDNI() {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }

        // Eliminar espacios y puntos
        String dniLimpio = dni.replaceAll("[.\\s]", "");

        // Validar que sea numérico y tenga 7-8 dígitos
        return dniLimpio.matches("\\d{7,8}");
    }

    /**
     * Calcula la edad actual del visitante.
     *
     * @return edad en años, o -1 si la fecha de nacimiento no está definida
     */
    public int calcularEdad() {
        if (fechaNacimiento == null) {
            return -1;
        }

        LocalDate fechaNac = fechaNacimiento.toInstant()
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate();
        LocalDate ahora = LocalDate.now();

        return Period.between(fechaNac, ahora).getYears();
    }

    /**
     * Verifica si el visitante es mayor de edad (18 años o más).
     * Requisito de negocio: solo mayores de edad pueden ser visitantes registrados.
     *
     * @return true si es mayor de 18 años
     */
    public boolean esMayorDeEdad() {
        return calcularEdad() >= 18;
    }

    /**
     * Agrega una restricción a la lista de restricciones del visitante.
     *
     * @param restriccion restricción a agregar
     */
    public void agregarRestriccion(Restriccion restriccion) {
        if (restriccion != null && !this.restricciones.contains(restriccion)) {
            this.restricciones.add(restriccion);
        }
    }

    /**
     * Verifica si el visitante tiene restricciones activas.
     * Método crítico para validación de ingreso (RF003).
     *
     * @return true si tiene al menos una restricción activa
     */
    public boolean tieneRestriccionesActivas() {
        if (restricciones == null || restricciones.isEmpty()) {
            return false;
        }

        return restricciones.stream()
                           .anyMatch(Restriccion::estaActiva);
    }

    /**
     * Verifica si el visitante puede realizar visitas.
     * Debe estar en estado ACTIVO y no tener restricciones activas.
     *
     * @return true si puede visitar
     */
    public boolean puedeRealizarVisitas() {
        return estado == EstadoVisitante.ACTIVO && !tieneRestriccionesActivas();
    }

    /**
     * Obtiene el nombre completo del visitante.
     *
     * @return apellido, nombre
     */
    public String getNombreCompleto() {
        return apellido + ", " + nombre;
    }

    // ===== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS DE EntidadBase =====

    /**
     * Valida las reglas de negocio para un visitante.
     *
     * <p>Reglas de validación:</p>
     * <ul>
     *   <li>DNI válido (7-8 dígitos numéricos)</li>
     *   <li>Mayor de edad (>= 18 años)</li>
     *   <li>Apellido y nombre no vacíos</li>
     *   <li>Fecha de nacimiento definida</li>
     * </ul>
     *
     * @return true si todas las validaciones pasan
     * @throws IllegalStateException si hay errores críticos de validación
     */
    @Override
    public boolean validar() throws IllegalStateException {
        StringBuilder errores = new StringBuilder();

        // Validar DNI
        if (!validarDNI()) {
            errores.append("DNI inválido (debe ser numérico de 7-8 dígitos). ");
        }

        // Validar mayor de edad
        if (!esMayorDeEdad()) {
            errores.append("El visitante debe ser mayor de 18 años. ");
        }

        // Validar datos obligatorios
        if (apellido == null || apellido.trim().isEmpty()) {
            errores.append("Apellido obligatorio. ");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            errores.append("Nombre obligatorio. ");
        }

        if (fechaNacimiento == null) {
            errores.append("Fecha de nacimiento obligatoria. ");
        }

        // Si hay errores, lanzar excepción
        if (errores.length() > 0) {
            throw new IllegalStateException("Visitante inválido: " + errores.toString());
        }

        return true;
    }

    /**
     * Obtiene un resumen textual del visitante para logs y auditoría.
     *
     * @return String con resumen de la entidad
     */
    @Override
    public String obtenerResumen() {
        return String.format("Visitante[DNI=%s, Nombre=%s, Estado=%s, Edad=%d años]",
                dni != null ? dni : "N/A",
                getNombreCompleto(),
                estado != null ? estado : "N/A",
                calcularEdad());
    }

    /**
     * Verifica si el visitante es nuevo (aún no persistido).
     * Sobrescribe el método de EntidadBase para verificar el ID específico.
     *
     * @return true si idVisitante es null
     */
    @Override
    public boolean esNuevo() {
        return idVisitante == null;
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdVisitante() {
        return idVisitante;
    }

    public void setIdVisitante(Long idVisitante) {
        this.idVisitante = idVisitante;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
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

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public EstadoVisitante getEstado() {
        return estado;
    }

    public void setEstado(EstadoVisitante estado) {
        this.estado = estado;
    }

    /**
     * Obtiene la fecha de registro del visitante.
     * Delega a getFechaCreacion() de EntidadBase.
     *
     * @return fecha de registro (equivalente a fechaCreacion)
     */
    public Date getFechaRegistro() {
        return getFechaCreacion();
    }

    /**
     * Establece la fecha de registro del visitante.
     * Delega a setFechaCreacion() de EntidadBase.
     *
     * @param fechaRegistro fecha de registro
     */
    public void setFechaRegistro(Date fechaRegistro) {
        setFechaCreacion(fechaRegistro);
    }

    public List<Restriccion> getRestricciones() {
        return restricciones;
    }

    public void setRestricciones(List<Restriccion> restricciones) {
        this.restricciones = restricciones;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Visitante visitante = (Visitante) o;
        // Basado en DNI que es UNIQUE en la base de datos
        return Objects.equals(dni, visitante.dni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }

    @Override
    public String toString() {
        return "Visitante{" +
                "idVisitante=" + idVisitante +
                ", dni='" + dni + '\'' +
                ", nombreCompleto='" + getNombreCompleto() + '\'' +
                ", estado=" + estado +
                ", edad=" + calcularEdad() +
                '}';
    }
}
