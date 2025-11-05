package com.sigvip.persistencia;

import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.enums.ModalidadVisita;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de la entidad Establecimiento.
 * Implementa el acceso a la tabla 'establecimientos' de la base de datos.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Responsabilidad: Configuración de establecimientos penitenciarios
 */
public class EstablecimientoDAO implements IBaseDAO<Establecimiento> {

    private ConexionBD conexionBD;

    public EstablecimientoDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta un nuevo establecimiento en la base de datos.
     *
     * @param establecimiento establecimiento a insertar
     * @return ID generado
     * @throws SQLException si ocurre un error
     */
@Override
    public Long insertar(Establecimiento establecimiento) throws SQLException {
        String sql = "INSERT INTO establecimientos (nombre, direccion, telefono, " +
                    "modalidad_visita, dias_habilita, horario_inicio, horario_fin, " +
                    "capacidad_maxima, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, establecimiento.getNombre());
            stmt.setString(2, establecimiento.getDireccion());
            stmt.setString(3, establecimiento.getTelefono());
            stmt.setString(4, establecimiento.getModalidadVisita() != null ?
                            establecimiento.getModalidadVisita().name() : null);
            stmt.setString(5, establecimiento.getDiasHabilita());
            stmt.setTime(6, establecimiento.getHorarioInicio() != null ?
                           new Time(establecimiento.getHorarioInicio().getTime()) : null);
            stmt.setTime(7, establecimiento.getHorarioFin() != null ?
                           new Time(establecimiento.getHorarioFin().getTime()) : null);
            stmt.setInt(8, establecimiento.getCapacidadMaxima());
            stmt.setBoolean(9, establecimiento.isActivo());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar establecimiento, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    establecimiento.setIdEstablecimiento(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar establecimiento, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca un establecimiento por su ID.
     *
     * @param id identificador del establecimiento
     * @return establecimiento encontrado o null
     * @throws SQLException si ocurre un error
     */
@Override
    public Establecimiento buscarPorId(Long id) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarEstablecimientoPorId(id);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM establecimientos WHERE id_establecimiento = ?";

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
     * Busca un establecimiento por su nombre.
     *
     * @param nombre nombre del establecimiento
     * @return establecimiento encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Establecimiento buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM establecimientos WHERE nombre = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Actualiza los datos de un establecimiento existente.
     *
     * @param establecimiento establecimiento con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
@Override
    public boolean actualizar(Establecimiento establecimiento) throws SQLException {
        String sql = "UPDATE establecimientos SET nombre = ?, direccion = ?, telefono = ?, " +
                    "modalidad_visita = ?, dias_habilita = ?, horario_inicio = ?, " +
                    "horario_fin = ?, capacidad_maxima = ?, activo = ? " +
                    "WHERE id_establecimiento = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, establecimiento.getNombre());
            stmt.setString(2, establecimiento.getDireccion());
            stmt.setString(3, establecimiento.getTelefono());
            stmt.setString(4, establecimiento.getModalidadVisita() != null ?
                            establecimiento.getModalidadVisita().name() : null);
            stmt.setString(5, establecimiento.getDiasHabilita());
            stmt.setTime(6, establecimiento.getHorarioInicio() != null ?
                           new Time(establecimiento.getHorarioInicio().getTime()) : null);
            stmt.setTime(7, establecimiento.getHorarioFin() != null ?
                           new Time(establecimiento.getHorarioFin().getTime()) : null);
            stmt.setInt(8, establecimiento.getCapacidadMaxima());
            stmt.setBoolean(9, establecimiento.isActivo());
            stmt.setLong(10, establecimiento.getIdEstablecimiento());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina un establecimiento de la base de datos.
     *
     * @param id identificador del establecimiento
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
@Override
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM establecimientos WHERE id_establecimiento = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todos los establecimientos registrados.
     *
     * @return lista de todos los establecimientos
     * @throws SQLException si ocurre un error
     */
    public List<Establecimiento> listarTodos() throws SQLException {
        String sql = "SELECT * FROM establecimientos ORDER BY nombre";
        List<Establecimiento> establecimientos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                establecimientos.add(mapearResultSet(rs));
            }
        }

        return establecimientos;
    }

    /**
    /**
     * Obtiene establecimientos activos.
     *
     * @return lista de establecimientos activos
     * @throws SQLException si ocurre un error
     */
    public List<Establecimiento> obtenerActivos() throws SQLException {
        String sql = "SELECT * FROM establecimientos WHERE activo = true ORDER BY nombre";
        List<Establecimiento> establecimientos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                establecimientos.add(mapearResultSet(rs));
            }
        }

        return establecimientos;
    }

    /**
     * Obtiene establecimientos por modalidad de visita.
     *
     * @param modalidad modalidad a filtrar
     * @return lista de establecimientos con esa modalidad
     * @throws SQLException si ocurre un error
     */
    public List<Establecimiento> buscarPorModalidad(ModalidadVisita modalidad) throws SQLException {
        String sql = "SELECT * FROM establecimientos WHERE modalidad_visita = ? ORDER BY nombre";
        List<Establecimiento> establecimientos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modalidad.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    establecimientos.add(mapearResultSet(rs));
                }
            }
        }

        return establecimientos;
    }

    /**
     * Obtiene establecimientos que permiten visitas en un día específico.
     *
     * @param dia nombre del día en español (ej: "LUNES", "MARTES")
     * @return lista de establecimientos que permiten visitas ese día
     * @throws SQLException si ocurre un error
     */
    public List<Establecimiento> buscarPorDiaHabilitado(String dia) throws SQLException {
        String sql = "SELECT * FROM establecimientos WHERE dias_habilita LIKE ? AND activo = true " +
                    "ORDER BY nombre";
        List<Establecimiento> establecimientos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + dia + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    establecimientos.add(mapearResultSet(rs));
                }
            }
        }

        return establecimientos;
    }

    /**
     * Cuenta el total de establecimientos activos.
     *
     * @return número de establecimientos activos
     * @throws SQLException si ocurre un error
     */
    public int contarActivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM establecimientos WHERE activo = true";

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
     * Mapea un ResultSet a un objeto Establecimiento.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Establecimiento con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Establecimiento mapearResultSet(ResultSet rs) throws SQLException {
        Establecimiento establecimiento = new Establecimiento();

        establecimiento.setIdEstablecimiento(rs.getLong("id_establecimiento"));
        establecimiento.setNombre(rs.getString("nombre"));
        establecimiento.setDireccion(rs.getString("direccion"));
        establecimiento.setTelefono(rs.getString("telefono"));

        String modalidadStr = rs.getString("modalidad_visita");
        if (modalidadStr != null) {
            establecimiento.setModalidadVisita(ModalidadVisita.valueOf(modalidadStr));
        }

        establecimiento.setDiasHabilita(rs.getString("dias_habilita"));

        Time horarioInicio = rs.getTime("horario_inicio");
        if (horarioInicio != null) {
            establecimiento.setHorarioInicio(new java.util.Date(horarioInicio.getTime()));
        }

        Time horarioFin = rs.getTime("horario_fin");
        if (horarioFin != null) {
            establecimiento.setHorarioFin(new java.util.Date(horarioFin.getTime()));
        }

        establecimiento.setCapacidadMaxima(rs.getInt("capacidad_maxima"));
        establecimiento.setActivo(rs.getBoolean("activo"));

        return establecimiento;
    }
}
