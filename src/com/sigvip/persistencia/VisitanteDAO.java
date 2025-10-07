package com.sigvip.persistencia;

import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.EstadoVisitante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de la entidad Visitante.
 * Implementa el acceso a la tabla 'visitantes' de la base de datos.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Seguridad: Todas las consultas usan PreparedStatement para prevenir SQL Injection
 */
public class VisitanteDAO {

    private ConexionBD conexionBD;

    /**
     * Constructor que obtiene la instancia del gestor de conexión.
     */
    public VisitanteDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta un nuevo visitante en la base de datos.
     *
     * @param visitante visitante a insertar
     * @return ID generado para el visitante
     * @throws SQLException si ocurre un error en la inserción
     */
    public Long insertar(Visitante visitante) throws SQLException {
        String sql = "INSERT INTO visitantes (dni, apellido, nombre, fecha_nacimiento, " +
                    "telefono, domicilio, foto, estado, fecha_registro) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, visitante.getDni());
            stmt.setString(2, visitante.getApellido());
            stmt.setString(3, visitante.getNombre());
            stmt.setDate(4, visitante.getFechaNacimiento() != null ?
                           new java.sql.Date(visitante.getFechaNacimiento().getTime()) : null);
            stmt.setString(5, visitante.getTelefono());
            stmt.setString(6, visitante.getDomicilio());
            stmt.setBytes(7, visitante.getFoto());
            stmt.setString(8, visitante.getEstado() != null ? visitante.getEstado().name() : null);
            stmt.setDate(9, visitante.getFechaRegistro() != null ?
                            new java.sql.Date(visitante.getFechaRegistro().getTime()) : null);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar visitante, ninguna fila afectada");
            }

            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    visitante.setIdVisitante(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar visitante, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca un visitante por su ID.
     *
     * @param id identificador del visitante
     * @return visitante encontrado o null si no existe
     * @throws SQLException si ocurre un error en la consulta
     */
    public Visitante buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM visitantes WHERE id_visitante = ?";

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
     * Busca un visitante por su DNI.
     * Método crítico para RF003: Control de Ingreso.
     *
     * @param dni documento del visitante
     * @return visitante encontrado o null si no existe
     * @throws SQLException si ocurre un error en la consulta
     */
    public Visitante buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT * FROM visitantes WHERE dni = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Actualiza los datos de un visitante existente.
     *
     * @param visitante visitante con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error en la actualización
     */
    public boolean actualizar(Visitante visitante) throws SQLException {
        String sql = "UPDATE visitantes SET dni = ?, apellido = ?, nombre = ?, " +
                    "fecha_nacimiento = ?, telefono = ?, domicilio = ?, " +
                    "foto = ?, estado = ? WHERE id_visitante = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, visitante.getDni());
            stmt.setString(2, visitante.getApellido());
            stmt.setString(3, visitante.getNombre());
            stmt.setDate(4, visitante.getFechaNacimiento() != null ?
                           new java.sql.Date(visitante.getFechaNacimiento().getTime()) : null);
            stmt.setString(5, visitante.getTelefono());
            stmt.setString(6, visitante.getDomicilio());
            stmt.setBytes(7, visitante.getFoto());
            stmt.setString(8, visitante.getEstado() != null ? visitante.getEstado().name() : null);
            stmt.setLong(9, visitante.getIdVisitante());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina un visitante de la base de datos.
     * NOTA: Preferir inactivación (cambiar estado) en lugar de eliminación física.
     *
     * @param id identificador del visitante
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error en la eliminación
     */
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM visitantes WHERE id_visitante = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todos los visitantes registrados.
     *
     * @return lista de todos los visitantes
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<Visitante> obtenerTodos() throws SQLException {
        String sql = "SELECT * FROM visitantes ORDER BY apellido, nombre";
        List<Visitante> visitantes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                visitantes.add(mapearResultSet(rs));
            }
        }

        return visitantes;
    }

    /**
     * Obtiene visitantes filtrados por estado.
     *
     * @param estado estado a filtrar
     * @return lista de visitantes con ese estado
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<Visitante> buscarPorEstado(EstadoVisitante estado) throws SQLException {
        String sql = "SELECT * FROM visitantes WHERE estado = ? ORDER BY apellido, nombre";
        List<Visitante> visitantes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitantes.add(mapearResultSet(rs));
                }
            }
        }

        return visitantes;
    }

    /**
     * Busca visitantes por apellido (búsqueda parcial).
     *
     * @param apellido apellido o parte del apellido a buscar
     * @return lista de visitantes que coinciden
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<Visitante> buscarPorApellido(String apellido) throws SQLException {
        String sql = "SELECT * FROM visitantes WHERE apellido LIKE ? ORDER BY apellido, nombre";
        List<Visitante> visitantes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + apellido + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitantes.add(mapearResultSet(rs));
                }
            }
        }

        return visitantes;
    }

    /**
     * Cuenta el total de visitantes registrados.
     *
     * @return número total de visitantes
     * @throws SQLException si ocurre un error en la consulta
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM visitantes";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Visitante.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Visitante con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Visitante mapearResultSet(ResultSet rs) throws SQLException {
        Visitante visitante = new Visitante();

        visitante.setIdVisitante(rs.getLong("id_visitante"));
        visitante.setDni(rs.getString("dni"));
        visitante.setApellido(rs.getString("apellido"));
        visitante.setNombre(rs.getString("nombre"));

        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            visitante.setFechaNacimiento(new java.util.Date(fechaNacimiento.getTime()));
        }

        visitante.setTelefono(rs.getString("telefono"));
        visitante.setDomicilio(rs.getString("domicilio"));
        visitante.setFoto(rs.getBytes("foto"));

        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            visitante.setEstado(EstadoVisitante.valueOf(estadoStr));
        }

        Date fechaRegistro = rs.getDate("fecha_registro");
        if (fechaRegistro != null) {
            visitante.setFechaRegistro(new java.util.Date(fechaRegistro.getTime()));
        }

        return visitante;
    }
}
