package com.sigvip.persistencia;

import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.enums.EstadoInterno;
import com.sigvip.modelo.enums.SituacionProcesal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DAO para operaciones CRUD de la entidad Interno.
 * Implementa el acceso a la tabla 'internos' de la base de datos.
 *
 * <p>Modo Offline: Si no hay conexión a MySQL, usa RepositorioMemoria (datos en RAM).
 * Modo Online: Funcionamiento normal con JDBC y MySQL.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Seguridad: Todas las consultas usan PreparedStatement
 */
public class InternoDAO {

    private ConexionBD conexionBD;

    public InternoDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta un nuevo interno en la base de datos.
     *
     * @param interno interno a insertar
     * @return ID generado
     * @throws SQLException si ocurre un error
     */
    public Long insertar(Interno interno) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().insertarInterno(interno);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "INSERT INTO internos (numero_legajo, apellido, nombre, dni, " +
                    "id_establecimiento, pabellon_actual, piso_actual, fecha_ingreso, " +
                    "unidad_procedencia, situacion_procesal, estado) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, interno.getNumeroLegajo());
            stmt.setString(2, interno.getApellido());
            stmt.setString(3, interno.getNombre());
            stmt.setString(4, interno.getDni());
            stmt.setObject(5, interno.getEstablecimiento() != null ?
                             interno.getEstablecimiento().getIdEstablecimiento() : null);
            stmt.setString(6, interno.getPabellonActual());
            stmt.setInt(7, interno.getPisoActual());
            stmt.setDate(8, interno.getFechaIngreso() != null ?
                           new java.sql.Date(interno.getFechaIngreso().getTime()) : null);
            stmt.setString(9, interno.getUnidadProcedencia());
            stmt.setString(10, interno.getSituacionProcesal() != null ?
                              interno.getSituacionProcesal().name() : null);
            stmt.setString(11, interno.getEstado() != null ? interno.getEstado().name() : null);

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar interno, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    interno.setIdInterno(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar interno, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca un interno por su ID.
     *
     * @param id identificador del interno
     * @return interno encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Interno buscarPorId(Long id) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarInternoPorId(id);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM internos WHERE id_interno = ?";

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
     * Busca un interno por su número de legajo.
     * El legajo es UNIQUE en la base de datos.
     *
     * @param numeroLegajo número de legajo del interno
     * @return interno encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Interno buscarPorLegajo(String numeroLegajo) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria (CRÍTICO)
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarInternoPorLegajo(numeroLegajo);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM internos WHERE numero_legajo = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numeroLegajo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
                return null;
            }
        }
    }

    /**
     * Busca un interno por su DNI.
     *
     * @param dni documento del interno
     * @return interno encontrado o null
     * @throws SQLException si ocurre un error
     */
    public Interno buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT * FROM internos WHERE dni = ?";

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
     * Actualiza los datos de un interno existente.
     *
     * @param interno interno con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean actualizar(Interno interno) throws SQLException {
        String sql = "UPDATE internos SET numero_legajo = ?, apellido = ?, nombre = ?, " +
                    "dni = ?, id_establecimiento = ?, pabellon_actual = ?, piso_actual = ?, " +
                    "fecha_ingreso = ?, unidad_procedencia = ?, situacion_procesal = ?, " +
                    "estado = ? WHERE id_interno = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, interno.getNumeroLegajo());
            stmt.setString(2, interno.getApellido());
            stmt.setString(3, interno.getNombre());
            stmt.setString(4, interno.getDni());
            stmt.setObject(5, interno.getEstablecimiento() != null ?
                             interno.getEstablecimiento().getIdEstablecimiento() : null);
            stmt.setString(6, interno.getPabellonActual());
            stmt.setInt(7, interno.getPisoActual());
            stmt.setDate(8, interno.getFechaIngreso() != null ?
                           new java.sql.Date(interno.getFechaIngreso().getTime()) : null);
            stmt.setString(9, interno.getUnidadProcedencia());
            stmt.setString(10, interno.getSituacionProcesal() != null ?
                              interno.getSituacionProcesal().name() : null);
            stmt.setString(11, interno.getEstado() != null ? interno.getEstado().name() : null);
            stmt.setLong(12, interno.getIdInterno());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina un interno de la base de datos.
     *
     * @param id identificador del interno
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM internos WHERE id_interno = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todos los internos registrados.
     *
     * @return lista de todos los internos
     * @throws SQLException si ocurre un error
     */
    public List<Interno> obtenerTodos() throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().listarInternos();
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM internos ORDER BY apellido, nombre";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                internos.add(mapearResultSet(rs));
            }
        }

        return internos;
    }

    /**
     * Obtiene todos los internos (alias de obtenerTodos).
     * Método de conveniencia para consistencia con otros DAOs.
     *
     * @return lista de todos los internos
     * @throws SQLException si ocurre un error
     */
    public List<Interno> listarTodos() throws SQLException {
        return obtenerTodos();
    }

    /**
     * Obtiene internos por establecimiento.
     *
     * @param idEstablecimiento ID del establecimiento
     * @return lista de internos del establecimiento
     * @throws SQLException si ocurre un error
     */
    public List<Interno> buscarPorEstablecimiento(Long idEstablecimiento) throws SQLException {
        String sql = "SELECT * FROM internos WHERE id_establecimiento = ? " +
                    "ORDER BY apellido, nombre";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idEstablecimiento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    internos.add(mapearResultSet(rs));
                }
            }
        }

        return internos;
    }

    /**
     * Obtiene internos activos (disponibles para recibir visitas).
     *
     * @return lista de internos activos
     * @throws SQLException si ocurre un error
     */
    public List<Interno> obtenerActivos() throws SQLException {
        String sql = "SELECT * FROM internos WHERE estado = ? ORDER BY apellido, nombre";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EstadoInterno.ACTIVO.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    internos.add(mapearResultSet(rs));
                }
            }
        }

        return internos;
    }

    /**
     * Busca internos por apellido (búsqueda parcial).
     *
     * @param apellido apellido o parte del apellido
     * @return lista de internos que coinciden
     * @throws SQLException si ocurre un error
     */
    public List<Interno> buscarPorApellido(String apellido) throws SQLException {
        String sql = "SELECT * FROM internos WHERE apellido LIKE ? ORDER BY apellido, nombre";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + apellido + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    internos.add(mapearResultSet(rs));
                }
            }
        }

        return internos;
    }

    /**
     * Obtiene internos por pabellón.
     *
     * @param pabellon nombre del pabellón
     * @return lista de internos del pabellón
     * @throws SQLException si ocurre un error
     */
    public List<Interno> buscarPorPabellon(String pabellon) throws SQLException {
        String sql = "SELECT * FROM internos WHERE pabellon_actual = ? ORDER BY piso_actual, apellido";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, pabellon);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    internos.add(mapearResultSet(rs));
                }
            }
        }

        return internos;
    }

    /**
     * Mapea un ResultSet a un objeto Interno.
     * NOTA: No carga las relaciones (establecimiento, autorizaciones) para evitar N+1.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Interno con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Interno mapearResultSet(ResultSet rs) throws SQLException {
        Interno interno = new Interno();

        interno.setIdInterno(rs.getLong("id_interno"));
        interno.setNumeroLegajo(rs.getString("numero_legajo"));
        interno.setApellido(rs.getString("apellido"));
        interno.setNombre(rs.getString("nombre"));
        interno.setDni(rs.getString("dni"));

        // Establecimiento: solo cargar el ID, no el objeto completo
        Long idEstablecimiento = rs.getLong("id_establecimiento");
        if (!rs.wasNull()) {
            Establecimiento estab = new Establecimiento();
            estab.setIdEstablecimiento(idEstablecimiento);
            interno.setEstablecimiento(estab);
        }

        interno.setPabellonActual(rs.getString("pabellon_actual"));
        interno.setPisoActual(rs.getInt("piso_actual"));

        Date fechaIngreso = rs.getDate("fecha_ingreso");
        if (fechaIngreso != null) {
            interno.setFechaIngreso(new java.util.Date(fechaIngreso.getTime()));
        }

        interno.setUnidadProcedencia(rs.getString("unidad_procedencia"));

        String situacionStr = rs.getString("situacion_procesal");
        if (situacionStr != null) {
            interno.setSituacionProcesal(SituacionProcesal.valueOf(situacionStr));
        }

        String estadoStr = rs.getString("estado");
        if (estadoStr != null) {
            interno.setEstado(EstadoInterno.valueOf(estadoStr));
        }

        return interno;
    }

    /**
     * Obtiene internos por estado.
     *
     * @param estado estado del interno
     * @return lista de internos con ese estado
     * @throws SQLException si ocurre un error
     */
    public List<Interno> buscarPorEstado(EstadoInterno estado) throws SQLException {
        String sql = "SELECT * FROM internos WHERE estado = ? ORDER BY apellido, nombre";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    internos.add(mapearResultSet(rs));
                }
            }
        }
        return internos;
    }

    /**
     * Obtiene internos por situación procesal.
     *
     * @param situacion situación procesal
     * @return lista de internos con esa situación
     * @throws SQLException si ocurre un error
     */
    public List<Interno> buscarPorSituacionProcesal(SituacionProcesal situacion) throws SQLException {
        String sql = "SELECT * FROM internos WHERE situacion_procesal = ? ORDER BY apellido, nombre";
        List<Interno> internos = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, situacion.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    internos.add(mapearResultSet(rs));
                }
            }
        }
        return internos;
    }
}
