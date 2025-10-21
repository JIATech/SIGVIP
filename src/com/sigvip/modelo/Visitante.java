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
 * Especificación: PDF Sección 9.2.2, página 13
 */
public class Visitante {

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
    private Date fechaRegistro;

    // Relaciones
    private List<Restriccion> restricciones;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Visitante() {
        this.restricciones = new ArrayList<>();
        this.estado = EstadoVisitante.ACTIVO;
        this.fechaRegistro = new Date();
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

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
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
