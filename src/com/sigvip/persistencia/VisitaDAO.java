package com.sigvip.persistencia;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.EstadoVisita;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para operaciones CRUD de la entidad Visita.
 * Implementa el acceso a la tabla 'visitas' de la base de datos.
 *
 * <p>Modo Offline: Si no hay conexión a MySQL, usa RepositorioMemoria (datos en RAM).
 * Modo Online: Funcionamiento normal con JDBC y MySQL.
 *
 * Especificación: PDF Sección 11.2.3 - Capa de Persistencia
 * Crítico para: RF003 (Control de Ingreso), RF004 (Control de Egreso)
 */
public class VisitaDAO implements IBaseDAO<Visita> {

    private ConexionBD conexionBD;

    public VisitaDAO() {
        this.conexionBD = ConexionBD.getInstancia();
    }

    /**
     * Inserta una nueva visita en la base de datos.
     *
     * @param visita visita a insertar
     * @return ID generado
     * @throws SQLException si ocurre un error
     */
    @Override
    public Long insertar(Visita visita) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria (CRÍTICO RF003/RF004)
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().insertarVisita(visita);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "INSERT INTO visitas (id_visitante, id_interno, fecha_visita, " +
                    "hora_ingreso, hora_egreso, estado_visita, id_operador_ingreso, " +
                    "id_operador_egreso, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, visita.getVisitante().getIdVisitante());
            stmt.setLong(2, visita.getInterno().getIdInterno());
            stmt.setDate(3, visita.getFechaVisita() != null ?
                           new java.sql.Date(visita.getFechaVisita().getTime()) : null);
            stmt.setTime(4, visita.getHoraIngreso() != null ?
                                new Time(visita.getHoraIngreso().getTime()) : null);
            stmt.setTime(5, visita.getHoraEgreso() != null ?
                                new Time(visita.getHoraEgreso().getTime()) : null);
            stmt.setString(6, visita.getEstadoVisita() != null ?
                            visita.getEstadoVisita().name() : null);
            stmt.setObject(7, visita.getOperadorIngreso() != null ?
                            visita.getOperadorIngreso().getIdUsuario() : null);
            stmt.setObject(8, visita.getOperadorEgreso() != null ?
                            visita.getOperadorEgreso().getIdUsuario() : null);
            stmt.setString(9, visita.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("Error al insertar visita, ninguna fila afectada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    visita.setIdVisita(id);
                    return id;
                } else {
                    throw new SQLException("Error al insertar visita, no se obtuvo ID");
                }
            }
        }
    }

    /**
     * Busca una visita por su ID.
     *
     * @param id identificador de la visita
     * @return visita encontrada o null
     * @throws SQLException si ocurre un error
     */
@Override
    public Visita buscarPorId(Long id) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().buscarVisitaPorId(id);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT * FROM visitas WHERE id_visita = ?";

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
     * Actualiza los datos de una visita existente.
     *
     * @param visita visita con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre un error
     */
@Override
    public boolean actualizar(Visita visita) throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria (CRÍTICO RF003/RF004)
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().actualizarVisita(visita);
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "UPDATE visitas SET id_visitante = ?, id_interno = ?, fecha_visita = ?, " +
                    "hora_ingreso = ?, hora_egreso = ?, estado_visita = ?, " +
                    "id_operador_ingreso = ?, id_operador_egreso = ?, observaciones = ? " +
                    "WHERE id_visita = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, visita.getVisitante().getIdVisitante());
            stmt.setLong(2, visita.getInterno().getIdInterno());
            stmt.setDate(3, visita.getFechaVisita() != null ?
                           new java.sql.Date(visita.getFechaVisita().getTime()) : null);
            stmt.setTime(4, visita.getHoraIngreso() != null ?
                                new Time(visita.getHoraIngreso().getTime()) : null);
            stmt.setTime(5, visita.getHoraEgreso() != null ?
                                new Time(visita.getHoraEgreso().getTime()) : null);
            stmt.setString(6, visita.getEstadoVisita() != null ?
                            visita.getEstadoVisita().name() : null);
            stmt.setObject(7, visita.getOperadorIngreso() != null ?
                            visita.getOperadorIngreso().getIdUsuario() : null);
            stmt.setObject(8, visita.getOperadorEgreso() != null ?
                            visita.getOperadorEgreso().getIdUsuario() : null);
            stmt.setString(9, visita.getObservaciones());
            stmt.setLong(10, visita.getIdVisita());

            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Elimina una visita de la base de datos.
     *
     * @param id identificador de la visita
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre un error
     */
@Override
    public boolean eliminar(Long id) throws SQLException {
        String sql = "DELETE FROM visitas WHERE id_visita = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        }
    }

    /**
     * Obtiene todas las visitas registradas.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @return lista de todas las visitas con datos completos
     * @throws SQLException si ocurre un error
     */
    @Override
    public List<Visita> listarTodos() throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                visitas.add(mapearResultSetCompleto(rs));
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas por estado.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param estado estado a filtrar
     * @return lista de visitas con ese estado con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorEstado(EstadoVisita estado) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.estado_visita = ? " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas por visitante.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param idVisitante ID del visitante
     * @return lista de visitas del visitante con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorVisitante(Long idVisitante) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.id_visitante = ? " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas por interno.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param idInterno ID del interno
     * @return lista de visitas al interno con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorInterno(Long idInterno) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.id_interno = ? " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idInterno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas en curso (actualmente dentro del establecimiento).
     * Crítico para verificar capacidad máxima.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @return lista de visitas en curso con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> obtenerEnCurso() throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().listarVisitasEnCurso();
        }

        // MODO ONLINE: MySQL con JDBC y EAGER LOADING
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.estado_visita = ? " +
                    "ORDER BY v.hora_ingreso ASC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EstadoVisita.EN_CURSO.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas por fecha.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param fecha fecha a buscar
     * @return lista de visitas de esa fecha con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorFecha(java.util.Date fecha) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.fecha_visita = ? " +
                    "ORDER BY v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(fecha.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas en un rango de fechas.
     * Útil para reportes (RF007).
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param fechaInicio fecha de inicio (inclusive)
     * @param fechaFin fecha de fin (inclusive)
     * @return lista de visitas en el rango con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorRangoFechas(java.util.Date fechaInicio,
                                             java.util.Date fechaFin) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.fecha_visita BETWEEN ? AND ? " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(2, new java.sql.Date(fechaFin.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }

        return visitas;
    }

    /**
     * Obtiene visitas de un visitante en un rango de fechas.
     * Combinación de búsqueda por visitante y rango de fechas para reportes.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param idVisitante ID del visitante
     * @param fechaInicio fecha de inicio (inclusive)
     * @param fechaFin fecha de fin (inclusive)
     * @return lista de visitas del visitante en el rango con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorVisitanteRangoFechas(Long idVisitante,
                                                     java.util.Date fechaInicio,
                                                     java.util.Date fechaFin) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.id_visitante = ? AND v.fecha_visita BETWEEN ? AND ? " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idVisitante);
            stmt.setDate(2, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(3, new java.sql.Date(fechaFin.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }
        return visitas;
    }

    /**
     * Obtiene visitas de un interno en un rango de fechas.
     * Combinación de búsqueda por interno y rango de fechas para reportes.
     * EAGER LOADING: Carga datos completos de visitante e interno con JOINs.
     *
     * @param idInterno ID del interno
     * @param fechaInicio fecha de inicio (inclusive)
     * @param fechaFin fecha de fin (inclusive)
     * @return lista de visitas del interno en el rango con datos completos
     * @throws SQLException si ocurre un error
     */
    public List<Visita> buscarPorInternoRangoFechas(Long idInterno,
                                                   java.util.Date fechaInicio,
                                                   java.util.Date fechaFin) throws SQLException {
        String sql = "SELECT v.*, " +
                    "vis.dni AS visitante_dni, vis.apellido AS visitante_apellido, " +
                    "vis.nombre AS visitante_nombre, vis.fecha_nacimiento AS visitante_fecha_nac, " +
                    "i.numero_legajo, i.apellido AS interno_apellido, " +
                    "i.nombre AS interno_nombre, i.pabellon_actual, i.piso_actual " +
                    "FROM visitas v " +
                    "INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante " +
                    "INNER JOIN internos i ON v.id_interno = i.id_interno " +
                    "WHERE v.id_interno = ? AND v.fecha_visita BETWEEN ? AND ? " +
                    "ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC";
        List<Visita> visitas = new ArrayList<>();

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idInterno);
            stmt.setDate(2, new java.sql.Date(fechaInicio.getTime()));
            stmt.setDate(3, new java.sql.Date(fechaFin.getTime()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitas.add(mapearResultSetCompleto(rs));
                }
            }
        }
        return visitas;
    }

    /**
     * Cuenta visitas en curso para verificar capacidad.
     *
     * @return número de visitas actualmente en curso
     * @throws SQLException si ocurre un error
     */
    public int contarVisitasEnCurso() throws SQLException {
        // MODO OFFLINE: Usar repositorio en memoria
        if (GestorModo.getInstancia().isModoOffline()) {
            return RepositorioMemoria.getInstancia().contarVisitasEnCurso();
        }

        // MODO ONLINE: MySQL con JDBC
        String sql = "SELECT COUNT(*) FROM visitas WHERE estado_visita = ?";

        try (Connection conn = conexionBD.getConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, EstadoVisita.EN_CURSO.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        }
    }

    /**
     * Mapea un ResultSet con JOIN a un objeto Visita con datos completos.
     * EAGER LOADING: Incluye datos completos de visitante e interno.
     *
     * @param rs resultado de la consulta SQL con JOIN
     * @return objeto Visita con datos completos
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Visita mapearResultSetCompleto(ResultSet rs) throws SQLException {
        Visita visita = new Visita();

        visita.setIdVisita(rs.getLong("id_visita"));

        // Cargar datos COMPLETOS del visitante (eager loading)
        Visitante visitante = new Visitante();
        visitante.setIdVisitante(rs.getLong("id_visitante"));
        visitante.setDni(rs.getString("visitante_dni"));
        visitante.setApellido(rs.getString("visitante_apellido"));
        visitante.setNombre(rs.getString("visitante_nombre"));

        Date fechaNac = rs.getDate("visitante_fecha_nac");
        if (fechaNac != null) {
            visitante.setFechaNacimiento(new java.util.Date(fechaNac.getTime()));
        }
        visita.setVisitante(visitante);

        // Cargar datos COMPLETOS del interno (eager loading)
        Interno interno = new Interno();
        interno.setIdInterno(rs.getLong("id_interno"));
        interno.setNumeroLegajo(rs.getString("numero_legajo"));
        interno.setApellido(rs.getString("interno_apellido"));
        interno.setNombre(rs.getString("interno_nombre"));
        interno.setPabellonActual(rs.getString("pabellon_actual"));
        interno.setPisoActual(rs.getInt("piso_actual"));
        visita.setInterno(interno);

        // Fecha y horas de la visita
        Date fechaVisita = rs.getDate("fecha_visita");
        if (fechaVisita != null) {
            visita.setFechaVisita(new java.util.Date(fechaVisita.getTime()));
        }

        Time horaIngreso = rs.getTime("hora_ingreso");
        if (horaIngreso != null) {
            visita.setHoraIngreso(new java.util.Date(horaIngreso.getTime()));
        }

        Time horaEgreso = rs.getTime("hora_egreso");
        if (horaEgreso != null) {
            visita.setHoraEgreso(new java.util.Date(horaEgreso.getTime()));
        }

        String estadoStr = rs.getString("estado_visita");
        if (estadoStr != null) {
            visita.setEstadoVisita(EstadoVisita.valueOf(estadoStr));
        }

        // Operadores (lazy loading - no los necesitamos en la tabla)
        Long idOperadorIngreso = rs.getLong("id_operador_ingreso");
        if (!rs.wasNull()) {
            Usuario operador = new Usuario();
            operador.setIdUsuario(idOperadorIngreso);
            visita.setOperadorIngreso(operador);
        }

        Long idOperadorEgreso = rs.getLong("id_operador_egreso");
        if (!rs.wasNull()) {
            Usuario operador = new Usuario();
            operador.setIdUsuario(idOperadorEgreso);
            visita.setOperadorEgreso(operador);
        }

        visita.setObservaciones(rs.getString("observaciones"));

        return visita;
    }

    /**
     * Mapea un ResultSet a un objeto Visita.
     * NOTA: Solo carga IDs de relaciones, no objetos completos.
     *
     * @param rs resultado de la consulta SQL
     * @return objeto Visita con los datos mapeados
     * @throws SQLException si ocurre un error al leer el ResultSet
     */
    private Visita mapearResultSet(ResultSet rs) throws SQLException {
        Visita visita = new Visita();

        visita.setIdVisita(rs.getLong("id_visita"));

        // Cargar solo IDs de visitante e interno (lazy loading)
        Visitante visitante = new Visitante();
        visitante.setIdVisitante(rs.getLong("id_visitante"));
        visita.setVisitante(visitante);

        Interno interno = new Interno();
        interno.setIdInterno(rs.getLong("id_interno"));
        visita.setInterno(interno);

        Date fechaVisita = rs.getDate("fecha_visita");
        if (fechaVisita != null) {
            visita.setFechaVisita(new java.util.Date(fechaVisita.getTime()));
        }

        Time horaIngreso = rs.getTime("hora_ingreso");
        if (horaIngreso != null) {
            visita.setHoraIngreso(new java.util.Date(horaIngreso.getTime()));
        }

        Time horaEgreso = rs.getTime("hora_egreso");
        if (horaEgreso != null) {
            visita.setHoraEgreso(new java.util.Date(horaEgreso.getTime()));
        }

        String estadoStr = rs.getString("estado_visita");
        if (estadoStr != null) {
            visita.setEstadoVisita(EstadoVisita.valueOf(estadoStr));
        }

        // Operadores (lazy loading)
        Long idOperadorIngreso = rs.getLong("id_operador_ingreso");
        if (!rs.wasNull()) {
            Usuario operador = new Usuario();
            operador.setIdUsuario(idOperadorIngreso);
            visita.setOperadorIngreso(operador);
        }

        Long idOperadorEgreso = rs.getLong("id_operador_egreso");
        if (!rs.wasNull()) {
            Usuario operador = new Usuario();
            operador.setIdUsuario(idOperadorEgreso);
            visita.setOperadorEgreso(operador);
        }

        visita.setObservaciones(rs.getString("observaciones"));

        return visita;
    }
}
