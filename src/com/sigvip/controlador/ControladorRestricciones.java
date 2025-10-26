package com.sigvip.controlador;

import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Restriccion;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.AplicableA;
import com.sigvip.modelo.enums.TipoRestriccion;
import com.sigvip.persistencia.InternoDAO;
import com.sigvip.persistencia.RestriccionDAO;
import com.sigvip.persistencia.VisitanteDAO;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Controlador para gestión de restricciones de acceso.
 * Implementa RF009: Registrar Restricciones.
 *
 * Responsabilidades:
 * - Coordinar flujo entre Vista y Modelo para restricciones
 * - Validar datos antes de persistir
 * - Gestionar estados de restricciones (levantar, extender)
 * - Proveer consultas para reportes y alertas
 *
 * Especificación: PDF Sección 7.1 (RF009)
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class ControladorRestricciones {

    private final RestriccionDAO restriccionDAO;
    private final VisitanteDAO visitanteDAO;
    private final InternoDAO internoDAO;
    private final Usuario usuarioActual;

    /**
     * Constructor con usuario actual para auditoría.
     *
     * @param usuario usuario que opera el sistema
     */
    public ControladorRestricciones(Usuario usuario) {
        this.restriccionDAO = new RestriccionDAO();
        this.visitanteDAO = new VisitanteDAO();
        this.internoDAO = new InternoDAO();
        this.usuarioActual = usuario;
    }

    // ===== BÚSQUEDA DE ENTIDADES RELACIONADAS =====

    /**
     * Busca un visitante por DNI.
     *
     * @param dni documento del visitante
     * @return visitante encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Visitante buscarVisitantePorDNI(String dni) throws SQLException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("DNI no puede estar vacío");
        }

        return visitanteDAO.buscarPorDni(dni.trim());
    }

    /**
     * Busca un interno por número de legajo.
     *
     * @param legajo número de legajo del interno
     * @return interno encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Interno buscarInternoPorLegajo(String legajo) throws SQLException {
        if (legajo == null || legajo.trim().isEmpty()) {
            throw new IllegalArgumentException("Legajo no puede estar vacío");
        }

        return internoDAO.buscarPorLegajo(legajo.trim());
    }

    /**
     * Obtiene todos los internos activos.
     * Útil para mostrar lista de selección.
     *
     * @return lista de internos activos
     * @throws SQLException si ocurre error en BD
     */
    public List<Interno> listarInternosActivos() throws SQLException {
        return internoDAO.listarTodos();
    }

    // ===== GESTIÓN DE RESTRICCIONES =====

    /**
     * Crea una nueva restricción de acceso.
     * Implementa RF009: Registrar Restricciones.
     *
     * Validaciones:
     * 1. Visitante debe existir
     * 2. Si alcance es INTERNO_ESPECIFICO, interno debe existir
     * 3. Fechas coherentes (fechaFin >= fechaInicio o NULL)
     * 4. Motivo obligatorio (mínimo 10 caracteres)
     *
     * @param idVisitante ID del visitante restringido
     * @param tipo tipo de restricción
     * @param motivo justificación detallada
     * @param fechaInicio fecha de inicio de vigencia
     * @param fechaFin fecha de fin (NULL = indefinida)
     * @param alcance TODOS o INTERNO_ESPECIFICO
     * @param idInterno ID del interno (solo si alcance es ESPECIFICO)
     * @return ID de la restricción creada
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si datos son inválidos
     */
    public Long crearRestriccion(Long idVisitante, TipoRestriccion tipo, String motivo,
                                  Date fechaInicio, Date fechaFin, AplicableA alcance,
                                  Long idInterno) throws SQLException {
        // Validaciones
        if (idVisitante == null) {
            throw new IllegalArgumentException("Debe seleccionar un visitante");
        }

        if (tipo == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de restricción");
        }

        if (motivo == null || motivo.trim().length() < 10) {
            throw new IllegalArgumentException("El motivo debe tener al menos 10 caracteres");
        }

        if (fechaInicio == null) {
            throw new IllegalArgumentException("Debe especificar fecha de inicio");
        }

        if (fechaFin != null && fechaFin.before(fechaInicio)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        if (alcance == AplicableA.INTERNO_ESPECIFICO && idInterno == null) {
            throw new IllegalArgumentException("Debe seleccionar un interno para restricción específica");
        }

        // Buscar visitante (verificar que existe)
        Visitante visitante = visitanteDAO.buscarPorId(idVisitante);
        if (visitante == null) {
            throw new IllegalArgumentException("El visitante seleccionado no existe");
        }

        // Buscar interno si es restricción específica
        Interno interno = null;
        if (alcance == AplicableA.INTERNO_ESPECIFICO) {
            interno = internoDAO.buscarPorId(idInterno);
            if (interno == null) {
                throw new IllegalArgumentException("El interno seleccionado no existe");
            }
        }

        // Crear restricción
        Restriccion restriccion = new Restriccion(visitante, tipo, motivo.trim());
        restriccion.setFechaInicio(fechaInicio);
        restriccion.setFechaFin(fechaFin);
        restriccion.setAplicableA(alcance);
        restriccion.setInterno(interno);
        restriccion.setActiva(true);
        restriccion.setCreadoPor(usuarioActual);

        // Persistir
        return restriccionDAO.insertar(restriccion);
    }

    /**
     * Actualiza una restricción existente.
     *
     * @param restriccion restricción con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si ocurre error en BD
     */
    public boolean actualizarRestriccion(Restriccion restriccion) throws SQLException {
        if (restriccion == null || restriccion.getIdRestriccion() == null) {
            throw new IllegalArgumentException("Restricción inválida");
        }

        return restriccionDAO.actualizar(restriccion);
    }

    /**
     * Levanta (desactiva) una restricción existente.
     * Registra el motivo del levantamiento en el campo motivo.
     *
     * @param idRestriccion ID de la restricción a levantar
     * @param motivoLevantamiento razón por la cual se levanta
     * @return true si se levantó correctamente
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si datos son inválidos
     */
    public boolean levantarRestriccion(Long idRestriccion, String motivoLevantamiento) throws SQLException {
        if (idRestriccion == null) {
            throw new IllegalArgumentException("Debe especificar la restricción a levantar");
        }

        if (motivoLevantamiento == null || motivoLevantamiento.trim().length() < 10) {
            throw new IllegalArgumentException("Debe especificar un motivo de al menos 10 caracteres");
        }

        // Buscar restricción
        Restriccion restriccion = restriccionDAO.buscarPorId(idRestriccion);
        if (restriccion == null) {
            throw new IllegalArgumentException("La restricción especificada no existe");
        }

        // Levantar usando el método de negocio
        restriccion.levantar(motivoLevantamiento.trim());

        // Persistir cambios
        return restriccionDAO.actualizar(restriccion);
    }

    /**
     * Extiende una restricción estableciendo una nueva fecha de finalización.
     *
     * @param idRestriccion ID de la restricción a extender
     * @param nuevaFechaFin nueva fecha límite (null = indefinida)
     * @return true si se extendió correctamente
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si datos son inválidos
     */
    public boolean extenderRestriccion(Long idRestriccion, Date nuevaFechaFin) throws SQLException {
        if (idRestriccion == null) {
            throw new IllegalArgumentException("Debe especificar la restricción a extender");
        }

        // Buscar restricción
        Restriccion restriccion = restriccionDAO.buscarPorId(idRestriccion);
        if (restriccion == null) {
            throw new IllegalArgumentException("La restricción especificada no existe");
        }

        if (!restriccion.isActiva()) {
            throw new IllegalArgumentException("Solo se pueden extender restricciones activas");
        }

        // Validar que la nueva fecha sea posterior a la actual
        if (nuevaFechaFin != null && restriccion.getFechaFin() != null &&
            nuevaFechaFin.before(restriccion.getFechaFin())) {
            throw new IllegalArgumentException("La nueva fecha debe ser posterior a la fecha de fin actual");
        }

        // Extender usando el método de negocio
        restriccion.extender(nuevaFechaFin);

        // Persistir cambios
        return restriccionDAO.actualizar(restriccion);
    }

    /**
     * Elimina una restricción de la base de datos.
     * SOLO disponible para ADMINISTRADOR.
     *
     * @param idRestriccion ID de la restricción a eliminar
     * @return true si se eliminó correctamente
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si datos son inválidos
     */
    public boolean eliminarRestriccion(Long idRestriccion) throws SQLException {
        if (idRestriccion == null) {
            throw new IllegalArgumentException("Debe especificar la restricción a eliminar");
        }

        return restriccionDAO.eliminar(idRestriccion);
    }

    // ===== CONSULTAS =====

    /**
     * Obtiene todas las restricciones registradas.
     *
     * @return lista de todas las restricciones
     * @throws SQLException si ocurre error en BD
     */
    public List<Restriccion> listarTodasRestricciones() throws SQLException {
        return restriccionDAO.obtenerTodas();
    }

    /**
     * Obtiene todas las restricciones activas.
     *
     * @return lista de restricciones activas
     * @throws SQLException si ocurre error en BD
     */
    public List<Restriccion> listarRestriccionesActivas() throws SQLException {
        return restriccionDAO.obtenerActivas();
    }

    /**
     * Obtiene todas las restricciones de un visitante (activas e inactivas).
     *
     * @param idVisitante ID del visitante
     * @return lista de restricciones
     * @throws SQLException si ocurre error en BD
     */
    public List<Restriccion> listarRestriccionesPorVisitante(Long idVisitante) throws SQLException {
        if (idVisitante == null) {
            throw new IllegalArgumentException("Debe especificar el visitante");
        }

        return restriccionDAO.buscarPorVisitante(idVisitante);
    }

    /**
     * Obtiene restricciones activas de un visitante.
     *
     * @param idVisitante ID del visitante
     * @return lista de restricciones activas
     * @throws SQLException si ocurre error en BD
     */
    public List<Restriccion> listarRestriccionesActivasPorVisitante(Long idVisitante) throws SQLException {
        if (idVisitante == null) {
            throw new IllegalArgumentException("Debe especificar el visitante");
        }

        return restriccionDAO.obtenerActivasPorVisitante(idVisitante);
    }

    /**
     * Obtiene restricciones próximas a vencer en los próximos N días.
     * Útil para alertas.
     *
     * @param dias número de días hacia adelante
     * @return lista de restricciones próximas a vencer
     * @throws SQLException si ocurre error en BD
     */
    public List<Restriccion> obtenerProximasAVencer(int dias) throws SQLException {
        if (dias <= 0) {
            throw new IllegalArgumentException("El número de días debe ser positivo");
        }

        Date hoy = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(hoy);
        cal.add(Calendar.DAY_OF_YEAR, dias);
        Date fechaLimite = cal.getTime();

        return restriccionDAO.obtenerProximasAVencer(hoy, fechaLimite);
    }

    /**
     * Cuenta el total de restricciones activas.
     *
     * @return número de restricciones activas
     * @throws SQLException si ocurre error en BD
     */
    public int contarRestriccionesActivas() throws SQLException {
        return restriccionDAO.contarActivas();
    }

    /**
     * Busca una restricción por ID.
     *
     * @param idRestriccion ID de la restricción
     * @return restricción encontrada o null
     * @throws SQLException si ocurre error en BD
     */
    public Restriccion buscarPorId(Long idRestriccion) throws SQLException {
        if (idRestriccion == null) {
            throw new IllegalArgumentException("Debe especificar el ID de la restricción");
        }

        return restriccionDAO.buscarPorId(idRestriccion);
    }
}
