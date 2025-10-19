package com.sigvip.controlador;

import com.sigvip.modelo.Establecimiento;
import com.sigvip.modelo.Interno;
import com.sigvip.modelo.Usuario;
import com.sigvip.modelo.enums.EstadoInterno;
import com.sigvip.modelo.enums.SituacionProcesal;
import com.sigvip.persistencia.EstablecimientoDAO;
import com.sigvip.persistencia.InternoDAO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Controlador para gestión de internos.
 * Implementa RF006: Gestionar Internos.
 *
 * Responsabilidades:
 * - Coordinar flujo entre Vista y Modelo para gestión de internos
 * - Validar datos antes de persistir
 * - Gestionar ubicaciones (pabellón, piso)
 * - Controlar estados y situación procesal
 *
 * Especificación: PDF Sección 7.1 (RF006)
 */
public class ControladorInternos {

    private final InternoDAO internoDAO;
    private final EstablecimientoDAO establecimientoDAO;
    private final Usuario usuarioActual;

    /**
     * Constructor con usuario actual para auditoría.
     *
     * @param usuario usuario que opera el sistema
     */
    public ControladorInternos(Usuario usuario) {
        this.internoDAO = new InternoDAO();
        this.establecimientoDAO = new EstablecimientoDAO();
        this.usuarioActual = usuario;
    }

    /**
     * Registra un nuevo interno en el sistema.
     * Implementa RF006: Gestionar Internos - Registrar.
     *
     * Validaciones:
     * 1. Legajo único (no puede haber duplicados)
     * 2. Datos personales completos (nombre, apellido, DNI)
     * 3. Establecimiento válido
     * 4. Fecha de ingreso no puede ser futura
     *
     * @param legajo número de legajo único
     * @param apellido apellido del interno
     * @param nombre nombre del interno
     * @param dni documento de identidad
     * @param fechaNacimiento fecha de nacimiento
     * @param fechaIngreso fecha de ingreso al establecimiento
     * @param situacionProcesal situación procesal
     * @param pabellonActual ubicación: pabellón
     * @param pisoActual ubicación: piso
     * @param idEstablecimiento ID del establecimiento
     * @param observaciones notas adicionales
     * @return interno creado con ID asignado
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     * @throws IllegalStateException si el legajo ya existe
     */
    public Interno registrarInterno(
            String legajo,
            String apellido,
            String nombre,
            String dni,
            Date fechaNacimiento,
            Date fechaIngreso,
            SituacionProcesal situacionProcesal,
            String pabellonActual,
            String pisoActual,
            Long idEstablecimiento,
            String observaciones) throws SQLException {

        // Validar datos obligatorios
        if (legajo == null || legajo.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de legajo es obligatorio");
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI es obligatorio");
        }

        if (fechaNacimiento == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }

        if (fechaIngreso == null) {
            throw new IllegalArgumentException("La fecha de ingreso es obligatoria");
        }

        if (situacionProcesal == null) {
            throw new IllegalArgumentException("La situación procesal es obligatoria");
        }

        // Validar fecha de ingreso
        if (fechaIngreso.after(new Date())) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser futura");
        }

        // Verificar que el legajo no exista (debe ser único)
        Interno existente = internoDAO.buscarPorLegajo(legajo.trim());
        if (existente != null) {
            throw new IllegalStateException(
                "Ya existe un interno registrado con el legajo: " + legajo +
                "\nNombre: " + existente.getNombreCompleto() +
                "\nEstado: " + existente.getEstado()
            );
        }

        // Verificar que el establecimiento exista
        if (idEstablecimiento != null) {
            Establecimiento establecimiento = establecimientoDAO.buscarPorId(idEstablecimiento);
            if (establecimiento == null) {
                throw new IllegalArgumentException("Establecimiento no encontrado con ID: " + idEstablecimiento);
            }
        }

        // Crear nuevo interno
        Interno interno = new Interno();
        interno.setNumeroLegajo(legajo.trim());
        interno.setApellido(apellido.trim());
        interno.setNombre(nombre.trim());
        interno.setDni(dni.trim());
        interno.setFechaNacimiento(fechaNacimiento);
        interno.setFechaIngreso(fechaIngreso);
        interno.setSituacionProcesal(situacionProcesal);
        interno.setPabellonActual(pabellonActual != null ? pabellonActual.trim() : null);
        interno.setPisoActual(pisoActual != null ? pisoActual.trim() : null);
        interno.setEstado(EstadoInterno.ACTIVO);
        interno.setObservaciones(observaciones != null ? observaciones.trim() : null);

        // Cargar establecimiento si se especificó
        if (idEstablecimiento != null) {
            Establecimiento establecimiento = new Establecimiento();
            establecimiento.setIdEstablecimiento(idEstablecimiento);
            interno.setEstablecimiento(establecimiento);
        }

        // Persistir en BD
        Long idGenerado = internoDAO.insertar(interno);
        interno.setIdInterno(idGenerado);

        return interno;
    }

    /**
     * Busca un interno por su número de legajo.
     *
     * @param legajo número de legajo
     * @return interno encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Interno buscarPorLegajo(String legajo) throws SQLException {
        if (legajo == null || legajo.trim().isEmpty()) {
            throw new IllegalArgumentException("Legajo no puede estar vacío");
        }

        return internoDAO.buscarPorLegajo(legajo.trim());
    }

    /**
     * Busca un interno por DNI.
     *
     * @param dni documento de identidad
     * @return interno encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Interno buscarPorDNI(String dni) throws SQLException {
        if (dni == null || dni.trim().isEmpty()) {
            throw new IllegalArgumentException("DNI no puede estar vacío");
        }

        return internoDAO.buscarPorDni(dni.trim());
    }

    /**
     * Busca un interno por ID.
     *
     * @param id identificador del interno
     * @return interno encontrado o null
     * @throws SQLException si ocurre error en BD
     */
    public Interno buscarPorId(Long id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("ID no puede ser nulo");
        }

        return internoDAO.buscarPorId(id);
    }

    /**
     * Actualiza los datos de un interno existente.
     *
     * @param interno interno con datos actualizados
     * @throws SQLException si ocurre error en BD
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public void actualizarInterno(Interno interno) throws SQLException {
        if (interno == null) {
            throw new IllegalArgumentException("Interno no puede ser nulo");
        }

        if (interno.getIdInterno() == null) {
            throw new IllegalArgumentException("El interno debe tener un ID asignado");
        }

        // Validar datos obligatorios
        if (interno.getNumeroLegajo() == null || interno.getNumeroLegajo().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de legajo es obligatorio");
        }

        if (interno.getApellido() == null || interno.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio");
        }

        if (interno.getNombre() == null || interno.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        // Actualizar en BD
        boolean actualizado = internoDAO.actualizar(interno);

        if (!actualizado) {
            throw new SQLException("No se pudo actualizar el interno con ID: " + interno.getIdInterno());
        }
    }

    /**
     * Actualiza la ubicación de un interno (pabellón y piso).
     *
     * @param idInterno ID del interno
     * @param pabellon nuevo pabellón
     * @param piso nuevo piso
     * @throws SQLException si ocurre error en BD
     */
    public void actualizarUbicacion(Long idInterno, String pabellon, String piso) throws SQLException {
        if (idInterno == null) {
            throw new IllegalArgumentException("ID de interno no puede ser nulo");
        }

        Interno interno = internoDAO.buscarPorId(idInterno);
        if (interno == null) {
            throw new IllegalArgumentException("Interno no encontrado con ID: " + idInterno);
        }

        interno.setPabellonActual(pabellon != null ? pabellon.trim() : null);
        interno.setPisoActual(piso != null ? piso.trim() : null);

        internoDAO.actualizar(interno);
    }

    /**
     * Cambia el estado de un interno.
     *
     * @param idInterno ID del interno
     * @param nuevoEstado nuevo estado
     * @param motivo motivo del cambio (para observaciones)
     * @throws SQLException si ocurre error en BD
     */
    public void cambiarEstado(Long idInterno, EstadoInterno nuevoEstado, String motivo) throws SQLException {
        if (idInterno == null) {
            throw new IllegalArgumentException("ID de interno no puede ser nulo");
        }

        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }

        Interno interno = internoDAO.buscarPorId(idInterno);
        if (interno == null) {
            throw new IllegalArgumentException("Interno no encontrado con ID: " + idInterno);
        }

        EstadoInterno estadoAnterior = interno.getEstado();
        interno.setEstado(nuevoEstado);

        // Agregar observación sobre el cambio de estado
        if (motivo != null && !motivo.trim().isEmpty()) {
            String observacionCambio = String.format(
                "Cambio de estado: %s -> %s. Motivo: %s",
                estadoAnterior, nuevoEstado, motivo.trim()
            );

            String observacionesActuales = interno.getObservaciones();
            if (observacionesActuales != null && !observacionesActuales.isEmpty()) {
                interno.setObservaciones(observacionesActuales + "\n" + observacionCambio);
            } else {
                interno.setObservaciones(observacionCambio);
            }
        }

        internoDAO.actualizar(interno);
    }

    /**
     * Lista todos los internos registrados.
     *
     * @return lista de todos los internos
     * @throws SQLException si ocurre error en BD
     */
    public List<Interno> listarTodos() throws SQLException {
        return internoDAO.listarTodos();
    }

    /**
     * Lista internos filtrados por estado.
     *
     * @param estado estado a filtrar
     * @return lista de internos con ese estado
     * @throws SQLException si ocurre error en BD
     */
    public List<Interno> listarPorEstado(EstadoInterno estado) throws SQLException {
        if (estado == null) {
            return listarTodos();
        }

        return internoDAO.buscarPorEstado(estado);
    }

    /**
     * Lista internos filtrados por situación procesal.
     *
     * @param situacion situación procesal a filtrar
     * @return lista de internos con esa situación
     * @throws SQLException si ocurre error en BD
     */
    public List<Interno> listarPorSituacion(SituacionProcesal situacion) throws SQLException {
        if (situacion == null) {
            return listarTodos();
        }

        return internoDAO.buscarPorSituacionProcesal(situacion);
    }

    /**
     * Lista internos activos (estado ACTIVO).
     * Útil para mostrar solo internos disponibles para visitas.
     *
     * @return lista de internos activos
     * @throws SQLException si ocurre error en BD
     */
    public List<Interno> listarActivos() throws SQLException {
        return internoDAO.buscarPorEstado(EstadoInterno.ACTIVO);
    }

    /**
     * Registra el traslado de un interno a otro establecimiento.
     *
     * @param idInterno ID del interno
     * @param idNuevoEstablecimiento ID del establecimiento destino
     * @param motivo motivo del traslado
     * @throws SQLException si ocurre error en BD
     */
    public void registrarTraslado(Long idInterno, Long idNuevoEstablecimiento, String motivo)
            throws SQLException {
        if (idInterno == null) {
            throw new IllegalArgumentException("ID de interno no puede ser nulo");
        }

        if (idNuevoEstablecimiento == null) {
            throw new IllegalArgumentException("Debe especificar el establecimiento destino");
        }

        Interno interno = internoDAO.buscarPorId(idInterno);
        if (interno == null) {
            throw new IllegalArgumentException("Interno no encontrado con ID: " + idInterno);
        }

        Establecimiento nuevoEstablecimiento = establecimientoDAO.buscarPorId(idNuevoEstablecimiento);
        if (nuevoEstablecimiento == null) {
            throw new IllegalArgumentException(
                "Establecimiento destino no encontrado con ID: " + idNuevoEstablecimiento);
        }

        // Actualizar establecimiento
        interno.setEstablecimiento(nuevoEstablecimiento);

        // Cambiar estado a TRASLADADO
        interno.setEstado(EstadoInterno.TRASLADADO);

        // Registrar en observaciones
        String observacionTraslado = String.format(
            "Traslado a: %s. Motivo: %s",
            nuevoEstablecimiento.getNombre(),
            motivo != null ? motivo.trim() : "No especificado"
        );

        String observacionesActuales = interno.getObservaciones();
        if (observacionesActuales != null && !observacionesActuales.isEmpty()) {
            interno.setObservaciones(observacionesActuales + "\n" + observacionTraslado);
        } else {
            interno.setObservaciones(observacionTraslado);
        }

        internoDAO.actualizar(interno);
    }

    /**
     * Registra el egreso de un interno del sistema.
     *
     * @param idInterno ID del interno
     * @param motivo motivo del egreso
     * @throws SQLException si ocurre error en BD
     */
    public void registrarEgreso(Long idInterno, String motivo) throws SQLException {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar el motivo del egreso");
        }

        cambiarEstado(idInterno, EstadoInterno.EGRESADO, motivo);
    }

    /**
     * Obtiene todos los establecimientos disponibles.
     * Útil para combo boxes de selección.
     *
     * @return lista de establecimientos
     * @throws SQLException si ocurre error en BD
     */
    public List<Establecimiento> listarEstablecimientos() throws SQLException {
        return establecimientoDAO.listarTodos();
    }
}
