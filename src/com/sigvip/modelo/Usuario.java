package com.sigvip.modelo;

import com.sigvip.modelo.enums.Rol;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Objects;

/**
 * Entidad que representa un usuario del sistema con acceso al SIGVIP.
 * Mapea a la tabla 'usuarios' de la base de datos.
 *
 * <p><b>TP4 - Herencia de Clase Abstracta:</b></p>
 * Esta clase hereda de EntidadBase los campos activo y fechaCreacion.
 *
 * Especificación: PDF Sección 10.3, tabla 'usuarios'
 * Seguridad: Contraseñas almacenadas con hash SHA2-256
 */
public class Usuario extends EntidadBase {

    // Atributos según especificación de tabla 'usuarios'
    private Long idUsuario;
    private String nombreUsuario;
    private String contrasena;  // Hash SHA-256
    private String nombreCompleto;
    private Rol rol;
    private Establecimiento establecimiento;
    // activo y fechaCreacion heredados de EntidadBase
    private Date ultimoAcceso;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Usuario() {
        super(); // Inicializa activo=true y fechaCreacion=now()
    }

    /**
     * Constructor con datos mínimos requeridos.
     *
     * @param nombreUsuario username para login
     * @param nombreCompleto nombre y apellido
     * @param rol nivel de acceso
     */
    public Usuario(String nombreUsuario, String nombreCompleto, Rol rol) {
        this();
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
    }

    // ===== MÉTODOS DE SEGURIDAD =====

    /**
     * Genera un hash SHA-256 de una contraseña.
     * Método estático para uso en registro y login.
     *
     * @param contrasenaTextoPlano contraseña sin encriptar
     * @return hash SHA-256 en hexadecimal
     * @throws RuntimeException si el algoritmo SHA-256 no está disponible
     */
    public static String hashearContrasena(String contrasenaTextoPlano) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(contrasenaTextoPlano.getBytes(StandardCharsets.UTF_8));

            // Convertir bytes a hexadecimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear contraseña: algoritmo SHA-256 no disponible", e);
        }
    }

    /**
     * Valida las credenciales del usuario.
     * Compara el hash de la contraseña proporcionada con el almacenado.
     *
     * @param contrasenaTextoPlano contraseña a validar
     * @return true si la contraseña coincide
     */
    public boolean validarCredenciales(String contrasenaTextoPlano) {
        if (contrasenaTextoPlano == null || contrasenaTextoPlano.isEmpty()) {
            return false;
        }

        if (!activo) {
            return false;  // Usuario inactivo no puede autenticarse
        }

        String hashProporcionado = hashearContrasena(contrasenaTextoPlano);
        return this.contrasena != null && this.contrasena.equals(hashProporcionado);
    }

    /**
     * Cambia la contraseña del usuario.
     *
     * @param contrasenaActual contraseña actual para verificación
     * @param contrasenaNueva nueva contraseña
     * @return true si el cambio fue exitoso
     */
    public boolean cambiarContrasena(String contrasenaActual, String contrasenaNueva) {
        if (!validarCredenciales(contrasenaActual)) {
            return false;
        }

        if (contrasenaNueva == null || contrasenaNueva.length() < 8) {
            throw new IllegalArgumentException(
                "La nueva contraseña debe tener al menos 8 caracteres");
        }

        this.contrasena = hashearContrasena(contrasenaNueva);
        return true;
    }

    /**
     * Establece una contraseña inicial (sin validar contraseña anterior).
     * Solo debe usarse en creación de usuarios.
     *
     * @param contrasenaTextoPlano contraseña en texto plano
     */
    public void establecerContrasena(String contrasenaTextoPlano) {
        if (contrasenaTextoPlano == null || contrasenaTextoPlano.length() < 8) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos 8 caracteres");
        }

        this.contrasena = hashearContrasena(contrasenaTextoPlano);
    }

    /**
     * Registra el acceso del usuario al sistema.
     */
    public void registrarAcceso() {
        this.ultimoAcceso = new Date();
    }

    /**
     * Verifica si el usuario tiene permisos de al menos cierto rol.
     *
     * @param rolRequerido rol mínimo requerido
     * @return true si tiene los permisos necesarios
     */
    public boolean tienePermisoDe(Rol rolRequerido) {
        return rol != null && rol.tienePermisosDe(rolRequerido);
    }

    // ===== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS DE EntidadBase =====

    /**
     * Valida las reglas de negocio para un usuario.
     *
     * @return true si todas las validaciones pasan
     * @throws IllegalStateException si hay errores críticos de validación
     */
    @Override
    public boolean validar() throws IllegalStateException {
        StringBuilder errores = new StringBuilder();

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            errores.append("Nombre de usuario obligatorio. ");
        }

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            errores.append("Nombre completo obligatorio. ");
        }

        if (rol == null) {
            errores.append("Rol obligatorio. ");
        }

        if (contrasena == null || contrasena.trim().isEmpty()) {
            errores.append("Contraseña obligatoria. ");
        }

        if (errores.length() > 0) {
            throw new IllegalStateException("Usuario inválido: " + errores.toString());
        }

        return true;
    }

    /**
     * Obtiene un resumen textual del usuario para logs y auditoría.
     *
     * @return String con resumen de la entidad
     */
    @Override
    public String obtenerResumen() {
        return String.format("Usuario[ID=%d, Username=%s, Nombre=%s, Rol=%s, Activo=%s]",
                idUsuario != null ? idUsuario : 0L,
                nombreUsuario != null ? nombreUsuario : "N/A",
                nombreCompleto != null ? nombreCompleto : "N/A",
                rol != null ? rol : "N/A",
                isActivo() ? "SÍ" : "NO");
    }

    /**
     * Verifica si el usuario es nuevo (aún no persistido).
     *
     * @return true si idUsuario es null
     */
    @Override
    public boolean esNuevo() {
        return idUsuario == null;
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        // IMPORTANTE: Solo debe recibir hashes, no texto plano
        this.contrasena = contrasena;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public Establecimiento getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    // isActivo() y setActivo() heredados de EntidadBase
    // getFechaCreacion() y setFechaCreacion() heredados de EntidadBase

    public Date getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(Date ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        // Basado en nombreUsuario que es UNIQUE en la base de datos
        return Objects.equals(nombreUsuario, usuario.nombreUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreUsuario);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + idUsuario +
                ", nombreUsuario='" + nombreUsuario + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                '}';
    }
}
