package com.sigvip.persistencia;

import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO para operaciones CRUD de la entidad Usuario.
 * Implementa el acceso a la tabla 'usuarios' de la base de datos.
 *
 * <p>Modo Offline: Si no hay conexión a MySQL, usa RepositorioMemoria (datos en RAM).
 * Modo Online: Funcionamiento normal con JDBC y MySQL.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Crítico para: RF001 (Autenticación), control de acceso por roles
 */
public class UsuarioDAO implements IBaseDAO<Usuario> {

    private ConexionBD conexionBD;

    public UsuarioDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta un nuevo usuario en la base de datos.
     * IMPORTANTE: La contraseña debe venir ya hasheada desde la capa de negocio.
     *
     * @param usuario usuario a insertar
     * @return ID generado
     * @throws SQLException si ocurre un error
     */
@Override
    public Long insertar(Usuario usuario) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().insertarUsuario(usuario);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "INSERT INTO usuarios (nombre_usuario, contrasena, nombre_completo, " +
                    "rol, id_establecimiento, activo, fecha_creacion, ultimo_acceso) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombreUsuario());
            stmt.setString(2, usuario.getContrasena());  // Ya debe estar hasheada
            stmt.setString(3, usuario.getNombreCompleto());
            stmt.setString(4, usuario.getRol() != null ? usuario.getRol().name() : null);
            stmt.setObject(5, usuario.getEstablecimiento() != null ?
                            usuario.getEstablecimiento().getIdEstablecimiento() : null);
            stmt.setBoolean(6, usuario.isActivo());
            stmt.setTimestamp(7, usuario.getFechaCreacion() != null ?
                                new Timestamp(usuario.getFechaCreacion().getTime()) : null);
            stmt.setTimestamp(8, usuario.getUltimoAcceso() != null ?
                                new Timestamp(usuario.getUltimoAcceso().getTime()) : null);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar usuario, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    usuario.setIdUsuario(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar usuario, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id identificador del usuario
     * @return usuario encontrado o null
     * @throws SQLException si ocurre un error
     */
@Override
    public Usuario buscarPorId(Long id) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarUsuarioPorId(id);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Busca un usuario por su nombre de usuario.
     * Método CRÍTICO para RF001: Autenticación y Login.
     * El nombre de usuario es UNIQUE en la base de datos.
     *
     * @param nombreUsuario username del usuario
     * @return usuario encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Usuario buscarPorNombreUsuario(String nombreUsuario) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria (CRÍTICO PARA LOGIN)
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarUsuarioPorNombre(nombreUsuario);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM usuarios WHERE nombre_usuario = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario usuario con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
@Override
    public boolean actualizar(Usuario usuario) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().actualizarUsuario(usuario);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "UPDATE usuarios SET nombre_usuario = ?, contrasena = ?, " +
                    "nombre_completo = ?, rol = ?, id_establecimiento = ?, activo = ?, " +
                    "fecha_creacion = ?, ultimo_acceso = ? WHERE id_usuario = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombreUsuario());
            stmt.setString(2, usuario.getContrasena());
            stmt.setString(3, usuario.getNombreCompleto());
            stmt.setString(4, usuario.getRol() != null ? usuario.getRol().name() : null);
            stmt.setObject(5, usuario.getEstablecimiento() != null ?
                            usuario.getEstablecimiento().getIdEstablecimiento() : null);
            stmt.setBoolean(6, usuario.isActivo());
            stmt.setTimestamp(7, usuario.getFechaCreacion() != null ?
                                new Timestamp(usuario.getFechaCreacion().getTime()) : null);
            stmt.setTimestamp(8, usuario.getUltimoAcceso() != null ?
                                new Timestamp(usuario.getUltimoAcceso().getTime()) : null);
            stmt.setLong(9, usuario.getIdUsuario());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Actualiza la fecha de último acceso de un usuario.
     * Se ejecuta después de un login exitoso.
     *
     * @param idUsuario ID del usuario
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean actualizarUltimoAcceso(Long idUsuario) throws SQLException {
        // MODO OFFLINE: Actualizar en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            Usuario u = RepositorioMemoria.getInstancia().buscarUsuarioPorId(idUsuario);
            if (u != null) {
                u.setUltimoAcceso(new Date());
                return RepositorioMemoria.getInstancia().actualizarUsuario(u);
            }
            return false;
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "UPDATE usuarios SET ultimo_acceso = NOW() WHERE id_usuario = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idUsuario);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina un usuario de la base de datos.
     * NOTA: Preferir inactivación (cambiar activo a false).
     *
     * @param id identificador del usuario
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
@Override
    public boolean eliminar(Long id) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().eliminarUsuario(id);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todos los usuarios registrados.
     *
     * @return lista de todos los usuarios
     * @throws SQLException si ocurre un error
     */
    public List<Usuario> listarTodos() throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().listarUsuarios();
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM usuarios ORDER BY nombre_completo";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearResultSet(rs));
            }
        }

        return usuarios;
    }

    /**
     * Obtiene usuarios activos.
     *
     * @return lista de usuarios activos
     * @throws SQLException si ocurre un error
     */
    public List<Usuario> obtenerActivos() throws SQLException {
        // MODO OFFLINE: Filtrar en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().listarUsuarios().stream()
                    .filter(Usuario::isActivo)
                    .collect(Collectors.toList());
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM usuarios WHERE activo = true ORDER BY nombre_completo";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapearResultSet(rs));
            }
        }

        return usuarios;
    }

    /**
     * Obtiene usuarios por rol.
     *
     * @param rol rol a filtrar
     * @return lista de usuarios con ese rol
     * @throws SQLException si ocurre un error
     */
    public List<Usuario> buscarPorRol(Rol rol) throws SQLException {
        // MODO OFFLINE: Filtrar en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().listarUsuarios().stream()
                    .filter(u -> u.getRol() == rol)
                    .collect(Collectors.toList());
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM usuarios WHERE rol = ? ORDER BY nombre_completo";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearResultSet(rs));
                }
            }
        }

        return usuarios;
    }

    /**
     * Obtiene usuarios por establecimiento.
     *
     * @param idEstablecimiento ID del establecimiento
     * @return lista de usuarios del establecimiento
     * @throws SQLException si ocurre un error
     */
    public List<Usuario> buscarPorEstablecimiento(Long idEstablecimiento) throws SQLException {
        // MODO OFFLINE: Filtrar en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().listarUsuarios().stream()
                    .filter(u -> u.getEstablecimiento() != null &&
                                 u.getEstablecimiento().getIdEstablecimiento().equals(idEstablecimiento))
                    .collect(Collectors.toList());
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM usuarios WHERE id_establecimiento = ? " +
                    "ORDER BY nombre_completo";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idEstablecimiento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearResultSet(rs));
                }
            }
        }

        return usuarios;
    }

    /**
     * Verifica si existe un usuario con ese nombre de usuario.
     * Útil para validar al crear nuevos usuarios.
     *
     * @param nombreUsuario nombre de usuario a verificar
     * @return true si ya existe
     * @throws SQLException si ocurre un error
     */
    public boolean existeNombreUsuario(String nombreUsuario) throws SQLException {
        // MODO OFFLINE: Buscar en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarUsuarioPorNombre(nombreUsuario) != null;
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombreUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }
    }

    /**
     * Cuenta usuarios activos por rol.
     * Útil para estadísticas (RF007: Reportes).
     *
     * @param rol rol a contar
     * @return número de usuarios activos con ese rol
     * @throws SQLException si ocurre un error
     */
    public int contarPorRol(Rol rol) throws SQLException {
        // MODO OFFLINE: Contar en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return (int) RepositorioMemoria.getInstancia().listarUsuarios().stream()
                    .filter(u -> u.getRol() == rol && u.isActivo())
                    .count();
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol = ? AND activo = true";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rol.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Usuario.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Usuario con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();

        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setContrasena(rs.getString("contrasena"));  // Hash SHA-256
        usuario.setNombreCompleto(rs.getString("nombre_completo"));

        String rolStr = rs.getString("rol");
        if (rolStr != null) {
            usuario.setRol(Rol.valueOf(rolStr));
        }

        // Establecimiento (lazy loading)
        Long idEstablecimiento = rs.getLong("id_establecimiento");
        if (!rs.wasNull()) {
            Establecimiento establecimiento = new Establecimiento();
            establecimiento.setIdEstablecimiento(idEstablecimiento);
            usuario.setEstablecimiento(establecimiento);
        }

        usuario.setActivo(rs.getBoolean("activo"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(new java.util.Date(fechaCreacion.getTime()));
        }

        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(new java.util.Date(ultimoAcceso.getTime()));
        }

        return usuario;
    }
}
