package com.sigvip.controlador;

import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.Rol;
import com.sigvip.persistencia.EstablecimientoDAO;
import com.sigvip.persistencia.UsuarioDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Controlador para gestión de usuarios del sistema.
 * Implementa RF008: Gestionar Usuarios.
 *
 * Responsabilidades:
 * - Coordinar flujo entre Vista y Modelo para gestión de usuarios
 * - Validar datos antes de persistir
 * - Gestionar roles y niveles de acceso
 * - Controlar estados activo/inactivo
 * - Gestionar cambios de contraseña con hash SHA-256
 *
 * Especificación: Fuente de Verdad parte_003 línea 41 (RF008)
 */
public class ControladorUsuarios {

    private final UsuarioDAO usuarioDAO;
    private final EstablecimientoDAO establecimientoDAO;
    private final Usuario usuarioActual;

    /**
     * Constructor con usuario actual para auditoría.
     *
     * @param usuario usuario que opera el sistema
     */
    public ControladorUsuarios(Usuario usuario) {
        this.usuarioDAO = new UsuarioDAO();
        this.establecimientoDAO = new EstablecimientoDAO();
        this.usuarioActual = usuario;
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * Implementa RF008: Gestionar Usuarios - Crear.
     *
     * Validaciones:
     * 1. Solo ADMINISTRADOR puede crear usuarios
     * 2. Nombre de usuario único (no puede haber duplicados)
     * 3. Contraseña mínimo 8 caracteres
     * 4. Rol válido
     * 5. Establecimiento válido
     * 6. Hash SHA-256 automático de contraseña
     *
     * @param nombreUsuario username para login (UNIQUE)
     * @param contrasenaTextoPlano contraseña sin encriptar
     * @param nombreCompleto nombre y apellido
     * @param rol nivel de acceso
     * @param idEstablecimiento ID del establecimiento asignado
     * @return usuario creado con ID asignado
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si el nombre de usuario ya existe
     * @throws SecurityException si el usuario actual no tiene permisos
     */
    public Usuario crearUsuario(
            String nombreUsuario,
            String contrasenaTextoPlano,
            String nombreCompleto,
            Rol rol,
            Long idEstablecimiento) throws SQLException {

        // Validar permisos: solo ADMINISTRADOR puede crear usuarios
        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException(
                "Solo usuarios con rol ADMINISTRADOR pueden crear usuarios.\n" +
                "Su rol actual: " + usuarioActual.getRol()
            );
        }

        // Validar datos obligatorios
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        if (contrasenaTextoPlano == null || contrasenaTextoPlano.length() < 8) {
            throw new IllegalArgumentException(
                "La contraseña debe tener al menos 8 caracteres.\n" +
                "Longitud proporcionada: " + (contrasenaTextoPlano != null ? contrasenaTextoPlano.length() : 0)
            );
        }

        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }

        if (rol == null) {
            throw new IllegalArgumentException("Debe seleccionar un rol");
        }

        // Validar que el nombre de usuario no exista (debe ser único)
        if (usuarioDAO.existeNombreUsuario(nombreUsuario.trim())) {
            throw new IllegalStateException(
                "Ya existe un usuario registrado con el nombre de usuario: " + nombreUsuario
            );
        }

        // Verificar que el establecimiento exista
        Establecimiento establecimiento = null;
        if (idEstablecimiento != null) {
            establecimiento = establecimientoDAO.buscarPorId(idEstablecimiento);
            if (establecimiento == null) {
                throw new IllegalArgumentException("Establecimiento no encontrado con ID: " + idEstablecimiento);
            }
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombreUsuario.trim());
        usuario.setNombreCompleto(nombreCompleto.trim());
        usuario.setRol(rol);
        usuario.setEstablecimiento(establecimiento);
        usuario.setActivo(true);
        usuario.setFechaCreacion(new Date());

        // Hashear contraseña con SHA-256
        usuario.establecerContrasena(contrasenaTextoPlano);

        // Persistir en BD
        Long idGenerado = usuarioDAO.insertar(usuario);
        usuario.setIdUsuario(idGenerado);

        return usuario;
    }

    /**
     * Busca un usuario por su nombre de usuario.
     *
     * @param nombreUsuario username
     * @return usuario encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de usuario no puede estar vacío");
        }

        return usuarioDAO.buscarPorNombreUsuario(nombreUsuario.trim());
    }

    /**
     * Busca un usuario por ID.
     *
     * @param id identificador del usuario
     * @return usuario encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Usuario buscarPorId(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID no puede ser nulo");
        }

        return usuarioDAO.buscarPorId(id);
    }

    /**
     * Actualiza los datos de un usuario existente.
     * NO actualiza la contraseña (usar cambiarContrasena).
     *
     * @param usuario usuario con datos actualizados
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws SecurityException si el usuario actual no tiene permisos
     */
    public void actualizarUsuario(Usuario usuario) throws SQLException {
        // Validar permisos: solo ADMINISTRADOR puede modificar usuarios
        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException(
                "Solo usuarios con rol ADMINISTRADOR pueden modificar usuarios.\n" +
                "Su rol actual: " + usuarioActual.getRol()
            );
        }

        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }

        if (usuario.getIdUsuario() == null) {
            throw new IllegalArgumentException("El usuario debe tener un ID asignado");
        }

        // Validar datos obligatorios
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        }

        if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }

        if (usuario.getRol() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }

        // Actualizar en BD
        boolean actualizado = usuarioDAO.actualizar(usuario);

        if (!actualizado) {
            throw new SQLException("No se pudo actualizar el usuario con ID: " + usuario.getIdUsuario());
        }
    }

    /**
     * Cambia la contraseña de un usuario.
     * Requiere contraseña actual para validación.
     *
     * @param idUsuario ID del usuario
     * @param contrasenaActual contraseña actual (para validación)
     * @param contrasenaNueva nueva contraseña (mínimo 8 caracteres)
     * @throws SQLException si ocurre error en BD
     * @throws SecurityException si la contraseña actual no coincide
     */
    public void cambiarContrasena(Long idUsuario, String contrasenaActual, String contrasenaNueva)
            throws SQLException {
        if (idUsuario == null) {
            throw new IllegalArgumentException("ID de usuario no puede ser nulo");
        }

        if (contrasenaNueva == null || contrasenaNueva.length() < 8) {
            throw new IllegalArgumentException(
                "La nueva contraseña debe tener al menos 8 caracteres.\n" +
                "Longitud proporcionada: " + (contrasenaNueva != null ? contrasenaNueva.length() : 0)
            );
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario);
        }

        // Validar contraseña actual
        if (!usuario.validarCredenciales(contrasenaActual)) {
            throw new SecurityException("La contraseña actual no es correcta");
        }

        // Cambiar contraseña
        boolean cambioExitoso = usuario.cambiarContrasena(contrasenaActual, contrasenaNueva);

        if (!cambioExitoso) {
            throw new SecurityException("No se pudo cambiar la contraseña");
        }

        // Actualizar en BD
        usuarioDAO.actualizar(usuario);
    }

    /**
     * Restablece la contraseña de un usuario (solo ADMINISTRADOR).
     * No requiere contraseña actual - fuerza el cambio.
     *
     * @param idUsuario ID del usuario
     * @param contrasenaNueva nueva contraseña
     * @throws SQLException si ocurre error en BD
     * @throws SecurityException si el usuario actual no tiene permisos
     */
    public void restablecerContrasena(Long idUsuario, String contrasenaNueva) throws SQLException {
        // Validar permisos: solo ADMINISTRADOR puede restablecer contraseñas
        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException(
                "Solo usuarios con rol ADMINISTRADOR pueden restablecer contraseñas.\n" +
                "Su rol actual: " + usuarioActual.getRol()
            );
        }

        if (idUsuario == null) {
            throw new IllegalArgumentException("ID de usuario no puede ser nulo");
        }

        if (contrasenaNueva == null || contrasenaNueva.length() < 8) {
            throw new IllegalArgumentException(
                "La nueva contraseña debe tener al menos 8 caracteres.\n" +
                "Longitud proporcionada: " + (contrasenaNueva != null ? contrasenaNueva.length() : 0)
            );
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario);
        }

        // Establecer nueva contraseña (sin validar anterior)
        usuario.establecerContrasena(contrasenaNueva);

        // Actualizar en BD
        usuarioDAO.actualizar(usuario);
    }

    /**
     * Activa un usuario inactivo.
     *
     * @param idUsuario ID del usuario
     * @throws SQLException si ocurre error en BD
     * @throws SecurityException si el usuario actual no tiene permisos
     */
    public void activarUsuario(Long idUsuario) throws SQLException {
        // Validar permisos: solo ADMINISTRADOR puede activar usuarios
        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException(
                "Solo usuarios con rol ADMINISTRADOR pueden activar usuarios.\n" +
                "Su rol actual: " + usuarioActual.getRol()
            );
        }

        if (idUsuario == null) {
            throw new IllegalArgumentException("ID de usuario no puede ser nulo");
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario);
        }

        usuario.setActivo(true);
        usuarioDAO.actualizar(usuario);
    }

    /**
     * Inactiva un usuario (preferir sobre eliminar).
     *
     * @param idUsuario ID del usuario
     * @throws SQLException si ocurre error en BD
     * @throws SecurityException si el usuario actual no tiene permisos
     */
    public void inactivarUsuario(Long idUsuario) throws SQLException {
        // Validar permisos: solo ADMINISTRADOR puede inactivar usuarios
        if (usuarioActual.getRol() != Rol.ADMINISTRADOR) {
            throw new SecurityException(
                "Solo usuarios con rol ADMINISTRADOR pueden inactivar usuarios.\n" +
                "Su rol actual: " + usuarioActual.getRol()
            );
        }

        if (idUsuario == null) {
            throw new IllegalArgumentException("ID de usuario no puede ser nulo");
        }

        // Evitar inactivarse a sí mismo
        if (idUsuario.equals(usuarioActual.getIdUsuario())) {
            throw new IllegalArgumentException("No puede inactivar su propio usuario");
        }

        Usuario usuario = usuarioDAO.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + idUsuario);
        }

        usuario.setActivo(false);
        usuarioDAO.actualizar(usuario);
    }

    /**
     * Lista todos los usuarios registrados.
     *
     * @return lista de todos los usuarios
     * @throws SQLException si ocurre error en BD
     */
    public List<Usuario> listarTodos() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    /**
     * Lista solo usuarios activos.
     *
     * @return lista de usuarios activos
     * @throws SQLException si ocurre error en BD
     */
    public List<Usuario> listarActivos() throws SQLException {
        return usuarioDAO.obtenerActivos();
    }

    /**
     * Lista usuarios filtrados por rol.
     *
     * @param rol rol a filtrar
     * @return lista de usuarios con ese rol
     * @throws SQLException si ocurre error en BD
     */
    public List<Usuario> listarPorRol(Rol rol) throws SQLException {
        if (rol == null) {
            return listarTodos();
        }

        return usuarioDAO.buscarPorRol(rol);
    }

    /**
     * Lista usuarios filtrados por establecimiento.
     *
     * @param idEstablecimiento ID del establecimiento
     * @return lista de usuarios del establecimiento
     * @throws SQLException si ocurre error en BD
     */
    public List<Usuario> listarPorEstablecimiento(Long idEstablecimiento) throws SQLException {
        if (idEstablecimiento == null) {
            return listarTodos();
        }

        return usuarioDAO.buscarPorEstablecimiento(idEstablecimiento);
    }

    /**
     * Obtiene todos los establecimientos disponibles.
     * Útil para combo boxes de selección.
     *
     * @return lista de establecimientos
     * @throws SQLException si ocurre error en BD
     */
    public List<Establecimiento> listarEstablecimientos() throws SQLException {
        return establecimientoDAO.listarTodos();
    }

    /**
     * Obtiene estadísticas de usuarios por rol.
     *
     * @param rol rol a contar
     * @return número de usuarios activos con ese rol
     * @throws SQLException si ocurre error en BD
     */
    public int contarUsuariosPorRol(Rol rol) throws SQLException {
        return usuarioDAO.contarPorRol(rol);
    }
}
