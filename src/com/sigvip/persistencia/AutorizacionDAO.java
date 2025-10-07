package com.sigvip.persistencia;

import com.sigvip.modelo.Autorizacion;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.EstadoAutorizacion;
import com.sigvip.modelo.enums.TipoRelacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de la entidad Autorizacion.
 * Implementa el acceso a la tabla 'autorizaciones' de la base de datos.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Crítico para: RF003 (Control de Ingreso) - validación de autorización
 */
public class AutorizacionDAO {

    private ConexionBD conexionBD;

    public AutorizacionDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta una nueva autorización en la base de datos.
     *
     * @param autorizacion autorización a insertar
     * @return ID generado
     * @throws SQLException si ocurre un error
     */
    public Long insertar(Autorizacion autorizacion) throws SQLException {
        String sql = "INSERT INTO autorizaciones (id_visitante, id_interno, tipo_relacion, " +
                    "descripcion_relacion, fecha_autorizacion, fecha_vencimiento, estado, " +
                    "id_autorizado_por, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, autorizacion.getVisitante().getIdVisitante());
            stmt.setLong(2, autorizacion.getInterno().getIdInterno());
            stmt.setString(3, autorizacion.getTipoRelacion() != null ?
                            autorizacion.getTipoRelacion().name() : null);
            stmt.setString(4, autorizacion.getDescripcionRelacion());
            stmt.setDate(5, autorizacion.getFechaAutorizacion() != null ?
                           new java.sql.Date(autorizacion.getFechaAutorizacion().getTime()) : null);
            stmt.setDate(6, autorizacion.getFechaVencimiento() != null ?
                           new java.sql.Date(autorizacion.getFechaVencimiento().getTime()) : null);
            stmt.setString(7, autorizacion.getEstado() != null ?
                            autorizacion.getEstado().name() : null);
            stmt.setObject(8, autorizacion.getAutorizadoPor() != null ?
                            autorizacion.getAutorizadoPor().getIdUsuario() : null);
            stmt.setString(9, autorizacion.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar autorización, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    autorizacion.setIdAutorizacion(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar autorización, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca una autorización por su ID.
     *
     * @param id identificador de la autorización
     * @return autorización encontrada o null
     * @throws SQLException si ocurre un error
     */
    public Autorizacion buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM autorizaciones WHERE id_autorizacion = ?";

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
     * Busca la autorización entre un visitante y un interno.
     * Método CRÍTICO para RF003: Control de Ingreso.
     * Verifica constraint UNIQUE(id_visitante, id_interno).
     *
     * @param idVisitante ID del visitante
     * @param idInterno ID del interno
     * @return autorización encontrada o null
     * @throws SQLException si ocurre un error
     */
    public Autorizacion buscarPorVisitanteInterno(Long idVisitante, Long idInterno)
            throws SQLException {
        String sql = "SELECT * FROM autorizaciones WHERE id_visitante = ? AND id_interno = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);
            stmt.setLong(2, idInterno);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Actualiza los datos de una autorización existente.
     *
     * @param autorizacion autorización con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean actualizar(Autorizacion autorizacion) throws SQLException {
        String sql = "UPDATE autorizaciones SET id_visitante = ?, id_interno = ?, " +
                    "tipo_relacion = ?, descripcion_relacion = ?, fecha_autorizacion = ?, " +
                    "fecha_vencimiento = ?, estado = ?, id_autorizado_por = ?, observaciones = ? " +
                    "WHERE id_autorizacion = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, autorizacion.getVisitante().getIdVisitante());
            stmt.setLong(2, autorizacion.getInterno().getIdInterno());
            stmt.setString(3, autorizacion.getTipoRelacion() != null ?
                            autorizacion.getTipoRelacion().name() : null);
            stmt.setString(4, autorizacion.getDescripcionRelacion());
            stmt.setDate(5, autorizacion.getFechaAutorizacion() != null ?
                           new java.sql.Date(autorizacion.getFechaAutorizacion().getTime()) : null);
            stmt.setDate(6, autorizacion.getFechaVencimiento() != null ?
                           new java.sql.Date(autorizacion.getFechaVencimiento().getTime()) : null);
            stmt.setString(7, autorizacion.getEstado() != null ?
                            autorizacion.getEstado().name() : null);
            stmt.setObject(8, autorizacion.getAutorizadoPor() != null ?
                            autorizacion.getAutorizadoPor().getIdUsuario() : null);
            stmt.setString(9, autorizacion.getObservaciones());
            stmt.setLong(10, autorizacion.getIdAutorizacion());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina una autorización de la base de datos.
     *
     * @param id identificador de la autorización
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM autorizaciones WHERE id_autorizacion = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todas las autorizaciones registradas.
     *
     * @return lista de todas las autorizaciones
     * @throws SQLException si ocurre un error
     */
    public List<Autorizacion> obtenerTodas() throws SQLException {
        String sql = "SELECT * FROM autorizaciones ORDER BY fecha_autorizacion DESC";
        List<Autorizacion> autorizaciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                autorizaciones.add(mapearResultSet(rs));
            }
        }

        return autorizaciones;
    }

    /**
     * Obtiene autorizaciones vigentes para un visitante.
     * Útil para mostrar a quién puede visitar.
     *
     * @param idVisitante ID del visitante
     * @return lista de autorizaciones vigentes
     * @throws SQLException si ocurre un error
     */
    public List<Autorizacion> obtenerVigentesPorVisitante(Long idVisitante) throws SQLException {
        String sql = "SELECT * FROM autorizaciones WHERE id_visitante = ? AND estado = ? " +
                    "ORDER BY fecha_autorizacion DESC";
        List<Autorizacion> autorizaciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);
            stmt.setString(2, EstadoAutorizacion.VIGENTE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autorizaciones.add(mapearResultSet(rs));
                }
            }
        }

        return autorizaciones;
    }

    /**
     * Obtiene autorizaciones vigentes para un interno.
     * Útil para saber quiénes pueden visitarlo.
     *
     * @param idInterno ID del interno
     * @return lista de autorizaciones vigentes
     * @throws SQLException si ocurre un error
     */
    public List<Autorizacion> obtenerVigentesPorInterno(Long idInterno) throws SQLException {
        String sql = "SELECT * FROM autorizaciones WHERE id_interno = ? AND estado = ? " +
                    "ORDER BY fecha_autorizacion DESC";
        List<Autorizacion> autorizaciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idInterno);
            stmt.setString(2, EstadoAutorizacion.VIGENTE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autorizaciones.add(mapearResultSet(rs));
                }
            }
        }

        return autorizaciones;
    }

    /**
     * Obtiene autorizaciones por estado.
     *
     * @param estado estado a filtrar
     * @return lista de autorizaciones con ese estado
     * @throws SQLException si ocurre un error
     */
    public List<Autorizacion> buscarPorEstado(EstadoAutorizacion estado) throws SQLException {
        String sql = "SELECT * FROM autorizaciones WHERE estado = ? " +
                    "ORDER BY fecha_autorizacion DESC";
        List<Autorizacion> autorizaciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autorizaciones.add(mapearResultSet(rs));
                }
            }
        }

        return autorizaciones;
    }

    /**
     * Obtiene autorizaciones próximas a vencer.
     * Útil para alertas (RF009: Consulta de Información).
     *
     * @param diasAntes días de anticipación para considerar "próximo a vencer"
     * @return lista de autorizaciones próximas a vencer
     * @throws SQLException si ocurre un error
     */
    public List<Autorizacion> obtenerProximasAVencer(int diasAntes) throws SQLException {
        String sql = "SELECT * FROM autorizaciones WHERE estado = ? " +
                    "AND fecha_vencimiento IS NOT NULL " +
                    "AND fecha_vencimiento <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                    "AND fecha_vencimiento >= CURDATE() " +
                    "ORDER BY fecha_vencimiento ASC";
        List<Autorizacion> autorizaciones = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EstadoAutorizacion.VIGENTE.name());
            stmt.setInt(2, diasAntes);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autorizaciones.add(mapearResultSet(rs));
                }
            }
        }

        return autorizaciones;
    }

    /**
     * Cuenta autorizaciones vigentes por tipo de relación.
     * Útil para estadísticas (RF007: Reportes).
     *
     * @param tipoRelacion tipo de relación
     * @return número de autorizaciones vigentes de ese tipo
     * @throws SQLException si ocurre un error
     */
    public int contarPorTipoRelacion(TipoRelacion tipoRelacion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM autorizaciones WHERE tipo_relacion = ? AND estado = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipoRelacion.name());
            stmt.setString(2, EstadoAutorizacion.VIGENTE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Autorizacion.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Autorizacion con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Autorizacion mapearResultSet(ResultSet rs) throws SQLException {
        Autorizacion autorizacion = new Autorizacion();

        autorizacion.setIdAutorizacion(rs.getLong("id_autorizacion"));

        // Cargar solo IDs (lazy loading)
        Visitante visitante = new Visitante();
        visitante.setIdVisitante(rs.getLong("id_visitante"));
        autorizacion.setVisitante(visitante);

        Interno interno = new Interno();
        interno.setIdInterno(rs.getLong("id_interno"));
        autorizacion.setInterno(interno);

        String tipoRelacionStr = rs.getString("tipo_relacion");
        if (tipoRelacionStr != null) {
            autorizacion.setTipoRelacion(TipoRelacion.valueOf(tipoRelacionStr));
        }

        autorizacion.setDescripcionRelacion(rs.getString("descripcion_relacion"));

        Date fechaAutorizacion = rs.getDate("fecha_autorizacion");
        if (fechaAutorizacion != null) {
            autorizacion.setFechaAutorizacion(new java.util.Date(fechaAutorizacion.getTime()));
        }

        Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        if (fechaVencimiento != null) {
            autorizacion.setFechaVencimiento(new java.util.Date(fechaVencimiento.getTime()));
        }

        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            autorizacion.setEstado(EstadoAutorizacion.valueOf(estadoStr));
        }

        Long idAutorizadoPor = rs.getLong("id_autorizado_por");
        if (!rs.wasNull()) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(idAutorizadoPor);
            autorizacion.setAutorizadoPor(usuario);
        }

        autorizacion.setObservaciones(rs.getString("observaciones"));

        return autorizacion;
    }
}
