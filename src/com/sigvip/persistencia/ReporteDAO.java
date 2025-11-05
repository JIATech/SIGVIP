package com.sigvip.persistencia;

import com.sigvip.modelo.ReporteGenerado;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.TipoReporte;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de reportes generados.
 * Implementa acceso a la tabla 'reportes_generados' de la base de datos.
 *
 * Especificación: RF007 - Generar Reportes (persistencia)
 * Seguridad: Todas las consultas usan PreparedStatement
 */
public class ReporteDAO implements IBaseDAO<ReporteGenerado> {

    private ConexionBD conexionBD;

    /**
     * Constructor que obtiene la instancia del gestor de conexión.
     */
    public ReporteDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta un nuevo reporte generado en la base de datos.
     *
     * @param reporte reporte a insertar
     * @return ID generado para el reporte
     * @throws SQLException si ocurre un error en la inserción
     */
@Override
    public Long insertar(ReporteGenerado reporte) throws SQLException {
        String sql = "INSERT INTO reportes_generados (tipo_reporte, titulo, parametros_filtro, " +
                    "contenido, total_registros, id_generado_por) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, reporte.getTipoReporte().name());
            stmt.setString(2, reporte.getTitulo());
            stmt.setString(3, reporte.getParametrosFiltro());
            stmt.setString(4, reporte.getContenido());
            stmt.setInt(5, reporte.getTotalRegistros());
            stmt.setLong(6, reporte.getIdGeneradoPor());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar reporte, ninguna fila afectada");
            }

            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    reporte.setIdReporte(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar reporte, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca un reporte por su ID.
     *
     * @param id identificador del reporte
     * @return reporte encontrado o null si no existe
     * @throws SQLException si ocurre un error en la consulta
     */
@Override
    public ReporteGenerado buscarPorId(Long id) throws SQLException {
        String sql = "SELECT r.*, u.nombre_completo as nombre_generador " +
                    "FROM reportes_generados r " +
                    "LEFT JOIN usuarios u ON r.id_generado_por = u.id_usuario " +
                    "WHERE r.id_reporte = ?";

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
     * Actualiza un reporte existente.
     * NOTA: Los reportes normalmente no se modifican después de generarse,
     * pero se implementa para cumplir con la interfaz IBaseDAO.
     *
     * @param reporte reporte con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
    @Override
    public boolean actualizar(ReporteGenerado reporte) throws SQLException {
        String sql = "UPDATE reportes_generados SET tipo_reporte = ?, titulo = ?, " +
                    "parametros_filtro = ?, contenido = ?, total_registros = ?, " +
                    "id_generado_por = ? WHERE id_reporte = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reporte.getTipoReporte().name());
            stmt.setString(2, reporte.getTitulo());
            stmt.setString(3, reporte.getParametrosFiltro());
            stmt.setString(4, reporte.getContenido());
            stmt.setInt(5, reporte.getTotalRegistros());
            stmt.setLong(6, reporte.getIdGeneradoPor());
            stmt.setLong(7, reporte.getIdReporte());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina un reporte de la base de datos.
     * Se puede usar para limpiar reportes antiguos o de prueba.
     *
     * @param id identificador del reporte
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
    @Override
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM reportes_generados WHERE id_reporte = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todos los reportes generados, ordenados por fecha descendente.
     *
     * @return lista de todos los reportes
     * @throws SQLException si ocurre un error en la consulta
     */
    @Override
    public List<ReporteGenerado> listarTodos() throws SQLException {
        String sql = "SELECT r.*, u.nombre_completo as nombre_generador " +
                    "FROM reportes_generados r " +
                    "LEFT JOIN usuarios u ON r.id_generado_por = u.id_usuario " +
                    "ORDER BY r.fecha_generacion DESC";
        List<ReporteGenerado> reportes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reportes.add(mapearResultSet(rs));
            }
        }

        return reportes;
    }

    /**
     * Obtiene reportes filtrados por tipo.
     *
     * @param tipoReporte tipo de reporte a filtrar
     * @return lista de reportes de ese tipo
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<ReporteGenerado> obtenerPorTipo(TipoReporte tipoReporte) throws SQLException {
        String sql = "SELECT r.*, u.nombre_completo as nombre_generador " +
                    "FROM reportes_generados r " +
                    "LEFT JOIN usuarios u ON r.id_generado_por = u.id_usuario " +
                    "WHERE r.tipo_reporte = ? " +
                    "ORDER BY r.fecha_generacion DESC";
        List<ReporteGenerado> reportes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoReporte.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reportes.add(mapearResultSet(rs));
                }
            }
        }

        return reportes;
    }

    /**
     * Obtiene reportes generados por un usuario específico.
     *
     * @param idUsuario ID del usuario
     * @return lista de reportes generados por ese usuario
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<ReporteGenerado> obtenerPorUsuario(Long idUsuario) throws SQLException {
        String sql = "SELECT r.*, u.nombre_completo as nombre_generador " +
                    "FROM reportes_generados r " +
                    "LEFT JOIN usuarios u ON r.id_generado_por = u.id_usuario " +
                    "WHERE r.id_generado_por = ? " +
                    "ORDER BY r.fecha_generacion DESC";
        List<ReporteGenerado> reportes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reportes.add(mapearResultSet(rs));
                }
            }
        }

        return reportes;
    }

    /**
     * Obtiene reportes generados en un rango de fechas.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return lista de reportes en ese rango
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<ReporteGenerado> obtenerPorRangoFechas(Date fechaInicio, Date fechaFin)
            throws SQLException {
        String sql = "SELECT r.*, u.nombre_completo as nombre_generador " +
                    "FROM reportes_generados r " +
                    "LEFT JOIN usuarios u ON r.id_generado_por = u.id_usuario " +
                    "WHERE r.fecha_generacion BETWEEN ? AND ? " +
                    "ORDER BY r.fecha_generacion DESC";
        List<ReporteGenerado> reportes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(fechaInicio.getTime()));
            stmt.setTimestamp(2, new Timestamp(fechaFin.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reportes.add(mapearResultSet(rs));
                }
            }
        }

        return reportes;
    }

    /**
     * Obtiene los reportes más recientes (últimos N días).
     *
     * @param dias número de días hacia atrás
     * @return lista de reportes recientes
     * @throws SQLException si ocurre un error en la consulta
     */
    public List<ReporteGenerado> obtenerRecientes(int dias) throws SQLException {
        String sql = "SELECT r.*, u.nombre_completo as nombre_generador " +
                    "FROM reportes_generados r " +
                    "LEFT JOIN usuarios u ON r.id_generado_por = u.id_usuario " +
                    "WHERE r.fecha_generacion >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                    "ORDER BY r.fecha_generacion DESC";
        List<ReporteGenerado> reportes = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dias);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reportes.add(mapearResultSet(rs));
                }
            }
        }

        return reportes;
    }

    /**
     * Cuenta el total de reportes generados.
     *
     * @return número total de reportes
     * @throws SQLException si ocurre un error en la consulta
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reportes_generados";

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
     * Obtiene estadísticas de reportes por tipo.
     *
     * @return mapa con tipo_reporte y cantidad
     * @throws SQLException si ocurre un error en la consulta
     */
    public java.util.Map<String, Integer> obtenerEstadisticasPorTipo() throws SQLException {
        String sql = "SELECT tipo_reporte, COUNT(*) as cantidad " +
                    "FROM reportes_generados " +
                    "GROUP BY tipo_reporte " +
                    "ORDER BY cantidad DESC";
        java.util.Map<String, Integer> estadisticas = new java.util.HashMap<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                estadisticas.put(rs.getString("tipo_reporte"), rs.getInt("cantidad"));
            }
        }

        return estadisticas;
    }

    /**
     * Elimina reportes antiguos para liberar espacio.
     *
     * @param díasAntigüedad eliminar reportes más antiguos que estos días
     * @return número de reportes eliminados
     * @throws SQLException si ocurre un error en la eliminación
     */
    public int limpiarAntiguos(int díasAntigüedad) throws SQLException {
        String sql = "DELETE FROM reportes_generados " +
                    "WHERE fecha_generacion < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, díasAntigüedad);
            return stmt.executeUpdate();
        }
    }

    /**
     * Mapea un ResultSet a un objeto ReporteGenerado.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto ReporteGenerado con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private ReporteGenerado mapearResultSet(ResultSet rs) throws SQLException {
        ReporteGenerado reporte = new ReporteGenerado();

        reporte.setIdReporte(rs.getLong("id_reporte"));

        String tipoStr = rs.getString("tipo_reporte");
        if (tipoStr != null) {
            reporte.setTipoReporte(TipoReporte.valueOf(tipoStr));
        }

        reporte.setTitulo(rs.getString("titulo"));
        reporte.setParametrosFiltro(rs.getString("parametros_filtro"));
        reporte.setContenido(rs.getString("contenido"));
        reporte.setTotalRegistros(rs.getInt("total_registros"));

        Timestamp fechaGen = rs.getTimestamp("fecha_generacion");
        if (fechaGen != null) {
            reporte.setFechaGeneracion(new Date(fechaGen.getTime()));
        }

        reporte.setIdGeneradoPor(rs.getLong("id_generado_por"));

        // Mapear nombre del generador si está disponible
        String nombreGenerador = rs.getString("nombre_generador");
        if (nombreGenerador != null) {
            Usuario generador = new Usuario();
            generador.setIdUsuario(rs.getLong("id_generado_por"));
            generador.setNombreCompleto(nombreGenerador);
            reporte.setUsuarioGenerador(generador);
        }

        return reporte;
    }
}