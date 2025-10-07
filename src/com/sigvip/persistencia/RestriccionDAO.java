package com.sigvip.persistencia;

import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Restriccion;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.AplicableA;
import com.sigvip.modelo.enums.TipoRestriccion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de la entidad Restriccion.
 * Implementa el acceso a la tabla 'restricciones' de la base de datos.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Crítico para: RF003 (Control de Ingreso) - validación de restricciones
 */
public class RestriccionDAO {

    private ConexionBD conexionBD;

    public RestriccionDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta una nueva restricción en la base de datos.
     *
     * @param restriccion restricción a insertar
     * @return ID generado
     * @throws SQLException si ocurre un error
     */
    public Long insertar(Restriccion restriccion) throws SQLException {
        String sql = "INSERT INTO restricciones (id_visitante, tipo_restriccion, motivo, " +
                    "fecha_inicio, fecha_fin, aplicable_a, id_interno, activa, id_creado_por) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, restriccion.getVisitante().getIdVisitante());
            stmt.setString(2, restriccion.getTipoRestriccion() != null ?
                            restriccion.getTipoRestriccion().name() : null);
            stmt.setString(3, restriccion.getMotivo());
            stmt.setDate(4, restriccion.getFechaInicio() != null ?
                           new java.sql.Date(restriccion.getFechaInicio().getTime()) : null);
            stmt.setDate(5, restriccion.getFechaFin() != null ?
                           new java.sql.Date(restriccion.getFechaFin().getTime()) : null);
            stmt.setString(6, restriccion.getAplicableA() != null ?
                            restriccion.getAplicableA().name() : null);
            stmt.setObject(7, restriccion.getInterno() != null ?
                            restriccion.getInterno().getIdInterno() : null);
            stmt.setBoolean(8, restriccion.isActiva());
            stmt.setObject(9, restriccion.getCreadoPor() != null ?
                            restriccion.getCreadoPor().getIdUsuario() : null);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar restricción, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    restriccion.setIdRestriccion(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar restricción, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca una restricción por su ID.
     *
     * @param id identificador de la restricción
     * @return restricción encontrada o null
     * @throws SQLException si ocurre un error
     */
    public Restriccion buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM restricciones WHERE id_restriccion = ?";

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
     * Actualiza los datos de una restricción existente.
     *
     * @param restriccion restricción con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean actualizar(Restriccion restriccion) throws SQLException {
        String sql = "UPDATE restricciones SET id_visitante = ?, tipo_restriccion = ?, " +
                    "motivo = ?, fecha_inicio = ?, fecha_fin = ?, aplicable_a = ?, " +
                    "id_interno = ?, activa = ?, id_creado_por = ? WHERE id_restriccion = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, restriccion.getVisitante().getIdVisitante());
            stmt.setString(2, restriccion.getTipoRestriccion() != null ?
                            restriccion.getTipoRestriccion().name() : null);
            stmt.setString(3, restriccion.getMotivo());
            stmt.setDate(4, restriccion.getFechaInicio() != null ?
                           new java.sql.Date(restriccion.getFechaInicio().getTime()) : null);
            stmt.setDate(5, restriccion.getFechaFin() != null ?
                           new java.sql.Date(restriccion.getFechaFin().getTime()) : null);
            stmt.setString(6, restriccion.getAplicableA() != null ?
                            restriccion.getAplicableA().name() : null);
            stmt.setObject(7, restriccion.getInterno() != null ?
                            restriccion.getInterno().getIdInterno() : null);
            stmt.setBoolean(8, restriccion.isActiva());
            stmt.setObject(9, restriccion.getCreadoPor() != null ?
                            restriccion.getCreadoPor().getIdUsuario() : null);
            stmt.setLong(10, restriccion.getIdRestriccion());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina una restricción de la base de datos.
     *
     * @param id identificador de la restricción
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM restricciones WHERE id_restriccion = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todas las restricciones registradas.
     *
     * @return lista de todas las restricciones
     * @throws SQLException si ocurre un error
     */
    public List<Restriccion> obtenerTodas() throws SQLException {
        String sql = "SELECT * FROM restricciones ORDER BY fecha_inicio DESC";
        List<Restriccion> restricciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                restricciones.add(mapearResultSet(rs));
            }
        }

        return restricciones;
    }

    /**
     * Obtiene restricciones activas para un visitante.
     * Método CRÍTICO para RF003: Control de Ingreso.
     *
     * @param idVisitante ID del visitante
     * @return lista de restricciones activas
     * @throws SQLException si ocurre un error
     */
    public List<Restriccion> obtenerActivasPorVisitante(Long idVisitante) throws SQLException {
        String sql = "SELECT * FROM restricciones WHERE id_visitante = ? AND activa = true " +
                    "AND fecha_inicio <= CURDATE() " +
                    "AND (fecha_fin IS NULL OR fecha_fin >= CURDATE()) " +
                    "ORDER BY fecha_inicio DESC";
        List<Restriccion> restricciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restricciones.add(mapearResultSet(rs));
                }
            }
        }

        return restricciones;
    }

    /**
     * Obtiene restricciones activas para un visitante que aplican a un interno específico.
     * Método CRÍTICO para RF003: Control de Ingreso.
     *
     * @param idVisitante ID del visitante
     * @param idInterno ID del interno
     * @return lista de restricciones que bloquean la visita
     * @throws SQLException si ocurre un error
     */
    public List<Restriccion> obtenerRestriccionesAplicables(Long idVisitante, Long idInterno)
            throws SQLException {
        String sql = "SELECT * FROM restricciones WHERE id_visitante = ? AND activa = true " +
                    "AND fecha_inicio <= CURDATE() " +
                    "AND (fecha_fin IS NULL OR fecha_fin >= CURDATE()) " +
                    "AND (aplicable_a = 'TODOS' OR (aplicable_a = 'INTERNO_ESPECIFICO' AND id_interno = ?))";
        List<Restriccion> restricciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);
            stmt.setLong(2, idInterno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restricciones.add(mapearResultSet(rs));
                }
            }
        }

        return restricciones;
    }

    /**
     * Obtiene todas las restricciones de un visitante (activas e inactivas).
     *
     * @param idVisitante ID del visitante
     * @return lista de restricciones
     * @throws SQLException si ocurre un error
     */
    public List<Restriccion> buscarPorVisitante(Long idVisitante) throws SQLException {
        String sql = "SELECT * FROM restricciones WHERE id_visitante = ? " +
                    "ORDER BY fecha_inicio DESC";
        List<Restriccion> restricciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restricciones.add(mapearResultSet(rs));
                }
            }
        }

        return restricciones;
    }

    /**
     * Obtiene restricciones por tipo.
     *
     * @param tipo tipo de restricción
     * @return lista de restricciones de ese tipo
     * @throws SQLException si ocurre un error
     */
    public List<Restriccion> buscarPorTipo(TipoRestriccion tipo) throws SQLException {
        String sql = "SELECT * FROM restricciones WHERE tipo_restriccion = ? " +
                    "ORDER BY fecha_inicio DESC";
        List<Restriccion> restricciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restricciones.add(mapearResultSet(rs));
                }
            }
        }

        return restricciones;
    }

    /**
     * Obtiene restricciones que vencen en un rango de fechas.
     * Útil para alertas de expiración.
     *
     * @param fechaInicio fecha de inicio del rango
     * @param fechaFin fecha de fin del rango
     * @return lista de restricciones próximas a vencer
     * @throws SQLException si ocurre un error
     */
    public List<Restriccion> obtenerProximasAVencer(java.util.Date fechaInicio,
                                                    java.util.Date fechaFin) throws SQLException {
        String sql = "SELECT * FROM restricciones WHERE activa = true " +
                    "AND fecha_fin BETWEEN ? AND ? ORDER BY fecha_fin ASC";
        List<Restriccion> restricciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    restricciones.add(mapearResultSet(rs));
                }
            }
        }

        return restricciones;
    }

    /**
     * Cuenta restricciones activas total.
     *
     * @return número de restricciones activas
     * @throws SQLException si ocurre un error
     */
    public int contarActivas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM restricciones WHERE activa = true " +
                    "AND fecha_inicio <= CURDATE() " +
                    "AND (fecha_fin IS NULL OR fecha_fin >= CURDATE())";

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
     * Mapea un ResultSet a un objeto Restriccion.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Restriccion con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Restriccion mapearResultSet(ResultSet rs) throws SQLException {
        Restriccion restriccion = new Restriccion();

        restriccion.setIdRestriccion(rs.getLong("id_restriccion"));

        // Cargar solo ID del visitante (lazy loading)
        Visitante visitante = new Visitante();
        visitante.setIdVisitante(rs.getLong("id_visitante"));
        restriccion.setVisitante(visitante);

        String tipoStr = rs.getString("tipo_restriccion");
        if (tipoStr != null) {
            restriccion.setTipoRestriccion(TipoRestriccion.valueOf(tipoStr));
        }

        restriccion.setMotivo(rs.getString("motivo"));

        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            restriccion.setFechaInicio(new java.util.Date(fechaInicio.getTime()));
        }

        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            restriccion.setFechaFin(new java.util.Date(fechaFin.getTime()));
        }

        String aplicableAStr = rs.getString("aplicable_a");
        if (aplicableAStr != null) {
            restriccion.setAplicableA(AplicableA.valueOf(aplicableAStr));
        }

        // Interno (solo si es restricción específica)
        Long idInterno = rs.getLong("id_interno");
        if (!rs.wasNull()) {
            Interno interno = new Interno();
            interno.setIdInterno(idInterno);
            restriccion.setInterno(interno);
        }

        restriccion.setActiva(rs.getBoolean("activa"));

        // Usuario que creó la restricción
        Long idCreadoPor = rs.getLong("id_creado_por");
        if (!rs.wasNull()) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idCreadoPor);
            restriccion.setCreadoPor(usuario);
        }

        return restriccion;
    }
}
