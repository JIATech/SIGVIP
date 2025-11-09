package com.sigvip.controlador;

import com.sigvip.modelo.Visitante;
import com.sigvip.modelo.enums.EstadoVisitante;
import com.sigvip.persistencia.VisitanteDAO;
import com.sigvip.utilidades.ValidadorDatos;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Controlador para la gestión de visitantes.
 * Implementa la capa de control en el patrón MVC.
 *
 * Especificación: PDF Sección 11.2.2 - Capa de Control
 * Responsabilidad: Coordinar operaciones CRUD de visitantes con validaciones
 * Funcionalidades: RF002 (Gestión de Visitantes), RF009 (Consulta de Información)
 */
public class ControladorVisitantes {

    private VisitanteDAO visitanteDAO;

    /**
     * Constructor que inicializa el DAO.
     */
    public ControladorVisitantes() {
        this.visitanteDAO = new VisitanteDAO();
    }

    /**
     * Registra un nuevo visitante en el sistema.
     * Implementa RF002: Gestión de Visitantes - Alta.
     *
     * @param dni documento de identidad
     * @param apellido apellido del visitante
     * @param nombre nombre del visitante
     * @param fechaNacimiento fecha de nacimiento
     * @param telefono teléfono de contacto
     * @param email correo electrónico
     * @param domicilio dirección
     * @return ID del visitante creado o null si falló
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Long registrarVisitante(String dni, String apellido, String nombre,
                                  Date fechaNacimiento, String telefono,
                                  String email, String domicilio) {
        // Validaciones de datos
        if (!ValidadorDatos.validarDNI(dni)) {
            throw new IllegalArgumentException(ValidadorDatos.mensajeErrorDNI(dni));
        }

        if (!ValidadorDatos.validarNombre(apellido)) {
            throw new IllegalArgumentException("El apellido no es válido");
        }

        if (!ValidadorDatos.validarNombre(nombre)) {
            throw new IllegalArgumentException("El nombre no es válido");
        }

        if (!ValidadorDatos.validarEdadMinima(fechaNacimiento)) {
            throw new IllegalArgumentException(ValidadorDatos.mensajeErrorEdad(fechaNacimiento));
        }

        if (telefono != null && !telefono.isEmpty() && !ValidadorDatos.validarTelefono(telefono)) {
            throw new IllegalArgumentException("El formato del teléfono no es válido");
        }

        if (email != null && !email.isEmpty() && !ValidadorDatos.validarEmail(email)) {
            throw new IllegalArgumentException(ValidadorDatos.mensajeErrorEmail(email));
        }

        try {
            // Verificar que no exista un visitante con ese DNI
            Visitante existente = visitanteDAO.buscarPorDni(dni);
            if (existente != null) {
                throw new IllegalArgumentException(
                    "Ya existe un visitante registrado con DNI: " + dni);
            }

            // Crear el visitante
            Visitante visitante = new Visitante(
                ValidadorDatos.limpiarDNI(dni),
                ValidadorDatos.normalizarNombre(apellido),
                ValidadorDatos.normalizarNombre(nombre),
                fechaNacimiento
            );

            visitante.setTelefono(telefono != null ? telefono.trim() : null);
            visitante.setEmail(email != null ? ValidadorDatos.normalizarEmail(email) : null);
            visitante.setDomicilio(domicilio != null ? domicilio.trim() : null);
            visitante.setEstado(EstadoVisitante.ACTIVO);
            visitante.setFechaRegistro(new Date());

            // Persistir en la base de datos
            Long id = visitanteDAO.insertar(visitante);

            return id;

        } catch (SQLException e) {
            System.err.println("Error al registrar visitante: " + e.getMessage());
            throw new RuntimeException("Error de base de datos al registrar visitante", e);
        }
    }

    /**
     * Busca un visitante por su DNI.
     * Implementa RF009: Consulta de Información.
     *
     * @param dni documento a buscar
     * @return visitante encontrado o null
     */
    public Visitante buscarPorDni(String dni) {
        if (!ValidadorDatos.validarDNI(dni)) {
            throw new IllegalArgumentException(ValidadorDatos.mensajeErrorDNI(dni));
        }

        try {
            return visitanteDAO.buscarPorDni(ValidadorDatos.limpiarDNI(dni));
        } catch (SQLException e) {
            System.err.println("Error al buscar visitante: " + e.getMessage());
            return null;
        }
    }

    /**
     * Busca visitantes por apellido (búsqueda parcial).
     *
     * @param apellido apellido o parte del apellido
     * @return lista de visitantes que coinciden
     */
    public List<Visitante> buscarPorApellido(String apellido) {
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }

        try {
            return visitanteDAO.buscarPorApellido(apellido.trim());
        } catch (SQLException e) {
            System.err.println("Error al buscar visitantes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Actualiza los datos de un visitante existente.
     * Implementa RF002: Gestión de Visitantes - Modificación.
     *
     * @param visitante visitante con datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarVisitante(Visitante visitante) {
        if (visitante == null || visitante.getIdVisitante() == null) {
            throw new IllegalArgumentException("El visitante debe tener un ID válido");
        }

        // Validar datos
        if (!ValidadorDatos.validarDNI(visitante.getDni())) {
            throw new IllegalArgumentException("DNI inválido");
        }

        if (!ValidadorDatos.validarNombre(visitante.getApellido()) ||
            !ValidadorDatos.validarNombre(visitante.getNombre())) {
            throw new IllegalArgumentException("Nombre o apellido inválido");
        }

        if (!ValidadorDatos.validarEdadMinima(visitante.getFechaNacimiento())) {
            throw new IllegalArgumentException("Edad no válida");
        }

        try {
            boolean actualizado = visitanteDAO.actualizar(visitante);

            return actualizado;

        } catch (SQLException e) {
            System.err.println("Error al actualizar visitante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cambia el estado de un visitante.
     * Implementa RF002: Gestión de Visitantes - Baja/Alta lógica.
     *
     * @param dni DNI del visitante
     * @param nuevoEstado nuevo estado
     * @return true si se cambió correctamente
     */
    public boolean cambiarEstado(String dni, EstadoVisitante nuevoEstado) {
        if (!ValidadorDatos.validarDNI(dni)) {
            throw new IllegalArgumentException("DNI inválido");
        }

        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }

        try {
            Visitante visitante = visitanteDAO.buscarPorDni(ValidadorDatos.limpiarDNI(dni));

            if (visitante == null) {
                System.err.println("No se encontró visitante con DNI: " + dni);
                return false;
            }

            visitante.setEstado(nuevoEstado);
            boolean actualizado = visitanteDAO.actualizar(visitante);

            return actualizado;

        } catch (SQLException e) {
            System.err.println("Error al cambiar estado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Inhabilita un visitante (baja lógica).
     *
     * @param dni DNI del visitante
     * @return true si se inhabilitó correctamente
     */
    public boolean inhabilitarVisitante(String dni) {
        return cambiarEstado(dni, EstadoVisitante.INACTIVO);
    }

    /**
     * Habilita un visitante.
     *
     * @param dni DNI del visitante
     * @return true si se habilitó correctamente
     */
    public boolean habilitarVisitante(String dni) {
        return cambiarEstado(dni, EstadoVisitante.ACTIVO);
    }

    /**
     * Obtiene todos los visitantes registrados.
     *
     * @return lista de todos los visitantes
     */
    public List<Visitante> obtenerTodos() {
        try {
            return visitanteDAO.listarTodos();
        } catch (SQLException e) {
            System.err.println("Error al obtener visitantes: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene visitantes por estado.
     *
     * @param estado estado a filtrar
     * @return lista de visitantes con ese estado
     */
    public List<Visitante> obtenerPorEstado(EstadoVisitante estado) {
        try {
            return visitanteDAO.buscarPorEstado(estado);
        } catch (SQLException e) {
            System.err.println("Error al obtener visitantes por estado: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Obtiene visitantes habilitados.
     *
     * @return lista de visitantes habilitados
     */
    public List<Visitante> obtenerHabilitados() {
        return obtenerPorEstado(EstadoVisitante.ACTIVO);
    }

    /**
     * Cuenta el total de visitantes registrados.
     *
     * @return número total de visitantes
     */
    public int contarTotal() {
        try {
            return visitanteDAO.contarTotal();
        } catch (SQLException e) {
            System.err.println("Error al contar visitantes: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Muestra información detallada de un visitante.
     *
     * @param dni DNI del visitante
     */
    public void mostrarInformacionVisitante(String dni) {
        // Método de utilidad para debugging - sin output en producción
        Visitante visitante = buscarPorDni(dni);
        if (visitante == null) {
            return;
        }
    }
}
