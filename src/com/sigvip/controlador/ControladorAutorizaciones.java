package com.sigvip.controlador;

import com.sigvip.modelo.Autorizacion;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.EstadoAutorizacion;
import com.sigvip.modelo.enums.TipoRelacion;
import com.sigvip.persistencia.AutorizacionDAO;
import com.sigvip.persistencia.InternoDAO;
import com.sigvip.persistencia.VisitanteDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Controlador para gestión de autorizaciones de visita.
 * Implementa RF002: Autorizar Visita.
 *
 * Responsabilidades:
 * - Coordinar flujo entre Vista y Modelo para autorizaciones
 * - Validar datos antes de persistir
 * - Verificar duplicados (constraint UNIQUE en BD)
 * - Gestionar estados de autorizaciones
 *
 * Especificación: PDF Sección 7.1 (RF002)
 */
public class ControladorAutorizaciones {

    private final AutorizacionDAO autorizacionDAO;
    private final VisitanteDAO visitanteDAO;
    private final InternoDAO internoDAO;
    private final Usuario usuarioActual;

    /**
     * Constructor con usuario actual para auditoría.
     *
     * @param usuario usuario que opera el sistema
     */
    public ControladorAutorizaciones(Usuario usuario) {
        this.autorizacionDAO = new AutorizacionDAO();
        this.visitanteDAO = new VisitanteDAO();
        this.internoDAO = new InternoDAO();
        this.usuarioActual = usuario;
    }

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
        return internoDAO.listarTodos(); // TODO: Filtrar solo ACTIVOS si hay método
    }

    /**
     * Crea una nueva autorización de visita.
     * Implementa RF002: Autorizar Visita.
     *
     * Validaciones:
     * 1. Visitante e interno deben existir
     * 2. No debe existir autorización duplicada (UNIQUE constraint)
     * 3. Tipo de relación obligatorio
     * 4. Fecha de vencimiento puede ser NULL (autorización indefinida)
     *
     * @param idVisitante ID del visitante autorizado
     * @param idInterno ID del interno a visitar
     * @param tipoRelacion tipo de vínculo entre ambos
     * @param fechaVencimiento fecha límite (NULL = indefinida)
     * @param observaciones notas adicionales
     * @return autorización creada con ID asignado
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si ya existe autorización
     */
    public Autorizacion crearAutorizacion(
            Long idVisitante,
            Long idInterno,
            TipoRelacion tipoRelacion,
            Date fechaVencimiento,
            String observaciones) throws SQLException {

        // Validar datos obligatorios
        if (idVisitante == null) {
            throw new IllegalArgumentException("Debe seleccionar un visitante");
        }

        if (idInterno == null) {
            throw new IllegalArgumentException("Debe seleccionar un interno");
        }

        if (tipoRelacion == null) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de relación");
        }

        // Verificar que visitante e interno existan
        Visitante visitante = visitanteDAO.buscarPorId(idVisitante);
        if (visitante == null) {
            throw new IllegalArgumentException("Visitante no encontrado con ID: " + idVisitante);
        }

        Interno interno = internoDAO.buscarPorId(idInterno);
        if (interno == null) {
            throw new IllegalArgumentException("Interno no encontrado con ID: " + idInterno);
        }

        // Verificar duplicados (constraint UNIQUE en BD)
        Autorizacion existente = autorizacionDAO.buscarPorVisitanteInterno(idVisitante, idInterno);
        if (existente != null) {
            throw new IllegalStateException(
                "Ya existe una autorización entre " + visitante.getNombreCompleto() +
                " y " + interno.getNombreCompleto() + " con estado: " + existente.getEstado() +
                ". Puede modificar la autorización existente en lugar de crear una nueva."
            );
        }

        // Crear nueva autorización
        Autorizacion autorizacion = new Autorizacion(visitante, interno, tipoRelacion);
        autorizacion.setFechaVencimiento(fechaVencimiento);
        autorizacion.setObservaciones(observaciones);
        autorizacion.setAutorizadoPor(usuarioActual);
        autorizacion.setEstado(EstadoAutorizacion.VIGENTE);

        // Persistir en BD
        Long idGenerado = autorizacionDAO.insertar(autorizacion);
        autorizacion.setIdAutorizacion(idGenerado);

        return autorizacion;
    }

    /**
     * Obtiene todas las autorizaciones registradas.
     *
     * @return lista completa de autorizaciones
     * @throws SQLException si ocurre error en BD
     */
    public List<Autorizacion> listarTodasAutorizaciones() throws SQLException {
        return autorizacionDAO.obtenerTodas();
    }

    /**
     * Obtiene autorizaciones vigentes de un visitante.
     *
     * @param idVisitante ID del visitante
     * @return lista de autorizaciones vigentes
     * @throws SQLException si ocurre error en BD
     */
    public List<Autorizacion> listarAutorizacionesVigentes(Long idVisitante) throws SQLException {
        return autorizacionDAO.obtenerVigentesPorVisitante(idVisitante);
    }

    /**
     * Obtiene autorizaciones por estado.
     *
     * @param estado estado a filtrar
     * @return lista de autorizaciones con ese estado
     * @throws SQLException si ocurre error en BD
     */
    public List<Autorizacion> listarPorEstado(EstadoAutorizacion estado) throws SQLException {
        return autorizacionDAO.buscarPorEstado(estado);
    }

    /**
     * Suspende temporalmente una autorización.
     *
     * @param idAutorizacion ID de la autorización
     * @param motivo razón de la suspensión
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public void suspenderAutorizacion(Long idAutorizacion, String motivo) throws SQLException {
        if (idAutorizacion == null) {
            throw new IllegalArgumentException("ID de autorización no puede ser nulo");
        }

        Autorizacion autorizacion = autorizacionDAO.buscarPorId(idAutorizacion);
        if (autorizacion == null) {
            throw new IllegalArgumentException("Autorización no encontrada con ID: " + idAutorizacion);
        }

        // Usar lógica de negocio del modelo
        autorizacion.suspender(motivo);

        // Persistir cambio
        autorizacionDAO.actualizar(autorizacion);
    }

    /**
     * Revoca permanentemente una autorización.
     *
     * @param idAutorizacion ID de la autorización
     * @param motivo razón de la revocación
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public void revocarAutorizacion(Long idAutorizacion, String motivo) throws SQLException {
        if (idAutorizacion == null) {
            throw new IllegalArgumentException("ID de autorización no puede ser nulo");
        }

        Autorizacion autorizacion = autorizacionDAO.buscarPorId(idAutorizacion);
        if (autorizacion == null) {
            throw new IllegalArgumentException("Autorización no encontrada con ID: " + idAutorizacion);
        }

        // Usar lógica de negocio del modelo
        autorizacion.revocar(motivo);

        // Persistir cambio
        autorizacionDAO.actualizar(autorizacion);
    }

    /**
     * Reactiva una autorización suspendida.
     *
     * @param idAutorizacion ID de la autorización
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si no se puede reactivar
     */
    public void reactivarAutorizacion(Long idAutorizacion) throws SQLException {
        if (idAutorizacion == null) {
            throw new IllegalArgumentException("ID de autorización no puede ser nulo");
        }

        Autorizacion autorizacion = autorizacionDAO.buscarPorId(idAutorizacion);
        if (autorizacion == null) {
            throw new IllegalArgumentException("Autorización no encontrada con ID: " + idAutorizacion);
        }

        // Usar lógica de negocio del modelo
        autorizacion.reactivar();

        // Persistir cambio
        autorizacionDAO.actualizar(autorizacion);
    }

    /**
     * Renueva una autorización con nueva fecha de vencimiento.
     *
     * @param idAutorizacion ID de la autorización
     * @param nuevaFechaVencimiento nueva fecha (NULL = indefinida)
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si no se puede renovar
     */
    public void renovarAutorizacion(Long idAutorizacion, Date nuevaFechaVencimiento)
            throws SQLException {
        if (idAutorizacion == null) {
            throw new IllegalArgumentException("ID de autorización no puede ser nulo");
        }

        Autorizacion autorizacion = autorizacionDAO.buscarPorId(idAutorizacion);
        if (autorizacion == null) {
            throw new IllegalArgumentException("Autorización no encontrada con ID: " + idAutorizacion);
        }

        // Usar lógica de negocio del modelo
        autorizacion.renovar(nuevaFechaVencimiento);

        // Persistir cambio
        autorizacionDAO.actualizar(autorizacion);
    }

    /**
     * Carga datos completos de visitante e interno en una autorización.
     * El DAO usa lazy loading (solo IDs), este método carga los objetos completos.
     *
     * @param autorizacion autorización con IDs de visitante e interno
     * @throws SQLException si ocurre error en BD
     */
    public void cargarDatosCompletos(Autorizacion autorizacion) throws SQLException {
        if (autorizacion == null) {
            return;
        }

        // Cargar visitante completo
        if (autorizacion.getVisitante() != null &&
            autorizacion.getVisitante().getIdVisitante() != null) {
            Visitante visitanteCompleto = visitanteDAO.buscarPorId(
                autorizacion.getVisitante().getIdVisitante());
            autorizacion.setVisitante(visitanteCompleto);
        }

        // Cargar interno completo
        if (autorizacion.getInterno() != null &&
            autorizacion.getInterno().getIdInterno() != null) {
            Interno internoCompleto = internoDAO.buscarPorId(
                autorizacion.getInterno().getIdInterno());
            autorizacion.setInterno(internoCompleto);
        }
    }

    /**
     * Carga datos completos para una lista de autorizaciones.
     *
     * @param autorizaciones lista de autorizaciones
     * @throws SQLException si ocurre error en BD
     */
    public void cargarDatosCompletos(List<Autorizacion> autorizaciones) throws SQLException {
        if (autorizaciones == null) {
            return;
        }

        for (Autorizacion autorizacion : autorizaciones) {
            cargarDatosCompletos(autorizacion);
        }
    }

    /**
     * Obtiene autorizaciones próximas a vencer.
     * Útil para alertas y notificaciones.
     *
     * @param diasAntes días de anticipación (ej: 30 días)
     * @return lista de autorizaciones próximas a vencer
     * @throws SQLException si ocurre error en BD
     */
    public List<Autorizacion> obtenerProximasAVencer(int diasAntes) throws SQLException {
        return autorizacionDAO.obtenerProximasAVencer(diasAntes);
    }
}
