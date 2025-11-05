package com.sigvip.persistencia;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica que define las operaciones CRUD básicas para todos los DAOs.
 *
 * <p><b>Justificación para TP4 - Uso de Interfaces:</b></p>
 * Esta interfaz genérica centraliza las operaciones CRUD, demostrando los siguientes conceptos:
 * <ul>
 *   <li><b>Abstracción</b>: Define el "contrato" que todos los DAOs deben cumplir
 *       sin especificar cómo se implementan las operaciones</li>
 *   <li><b>Polimorfismo</b>: Permite trabajar con cualquier DAO a través de la interfaz,
 *       facilitando el testing y la inyección de dependencias</li>
 *   <li><b>Genéricos (Generics)</b>: El parámetro &lt;T&gt; permite reutilizar la misma interfaz
 *       para diferentes entidades (Visitante, Interno, Visita, etc.)</li>
 *   <li><b>Estandarización</b>: Todas las clases DAO tienen la misma estructura de métodos,
 *       facilitando el mantenimiento y la comprensión del código</li>
 * </ul>
 *
 * <p><b>Ejemplo de uso:</b></p>
 * <pre>{@code
 * // VisitanteDAO implementa IBaseDAO<Visitante>
 * IBaseDAO<Visitante> dao = new VisitanteDAO();
 * Visitante v = dao.buscarPorId(1L);
 * List<Visitante> todos = dao.listarTodos();
 * }</pre>
 *
 * @param <T> Tipo de entidad que el DAO gestiona (debe extender EntidadBase)
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 * @since TP4
 */
public interface IBaseDAO<T> {

    /**
     * Inserta una nueva entidad en la base de datos.
     *
     * <p>Este método debe:</p>
     * <ul>
     *   <li>Abrir una conexión a la base de datos</li>
     *   <li>Ejecutar un INSERT con los datos de la entidad</li>
     *   <li>Obtener el ID generado automáticamente y asignarlo a la entidad</li>
     *   <li>Cerrar los recursos JDBC (Connection, PreparedStatement, ResultSet)</li>
     *   <li>Manejar excepciones SQLException apropiadamente</li>
     * </ul>
     *
     * @param entidad entidad a insertar (no debe ser null)
     * @return ID generado automáticamente por la base de datos
     * @throws SQLException si ocurre un error al insertar en la base de datos
     * @throws IllegalArgumentException si la entidad es null o no es válida
     */
    Long insertar(T entidad) throws SQLException;

    /**
     * Busca una entidad por su ID en la base de datos.
     *
     * <p>Este método debe:</p>
     * <ul>
     *   <li>Ejecutar un SELECT WHERE id = ?</li>
     *   <li>Construir la entidad a partir del ResultSet</li>
     *   <li>Retornar null si no se encuentra ningún registro</li>
     *   <li>Cerrar los recursos JDBC apropiadamente</li>
     * </ul>
     *
     * @param id identificador de la entidad a buscar
     * @return entidad encontrada, o null si no existe
     * @throws SQLException si ocurre un error al consultar la base de datos
     * @throws IllegalArgumentException si el id es null
     */
    T buscarPorId(Long id) throws SQLException;

    /**
     * Lista todas las entidades de este tipo en la base de datos.
     *
     * <p>Este método debe:</p>
     * <ul>
     *   <li>Ejecutar un SELECT * FROM tabla</li>
     *   <li>Construir una lista de entidades a partir del ResultSet</li>
     *   <li>Retornar una lista vacía si no hay registros</li>
     *   <li>Utilizar ArrayList para almacenar los resultados</li>
     *   <li>Cerrar los recursos JDBC apropiadamente</li>
     * </ul>
     *
     * <p><b>Nota TP4:</b> Este método demuestra el uso de ArrayList, uno de los
     * requisitos obligatorios del trabajo práctico.</p>
     *
     * @return lista con todas las entidades (nunca null, puede estar vacía)
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    List<T> listarTodos() throws SQLException;

    /**
     * Actualiza una entidad existente en la base de datos.
     *
     * <p>Este método debe:</p>
     * <ul>
     *   <li>Verificar que la entidad tiene un ID válido</li>
     *   <li>Ejecutar un UPDATE SET ... WHERE id = ?</li>
     *   <li>Retornar true si se actualizó al menos un registro</li>
     *   <li>Retornar false si no se encontró el registro</li>
     *   <li>Cerrar los recursos JDBC apropiadamente</li>
     * </ul>
     *
     * @param entidad entidad con los datos actualizados (debe tener ID válido)
     * @return true si se actualizó exitosamente, false si no se encontró
     * @throws SQLException si ocurre un error al actualizar en la base de datos
     * @throws IllegalArgumentException si la entidad es null o no tiene ID
     */
    boolean actualizar(T entidad) throws SQLException;

    /**
     * Elimina una entidad de la base de datos por su ID.
     *
     * <p>Este método debe:</p>
     * <ul>
     *   <li>Ejecutar un DELETE FROM tabla WHERE id = ?</li>
     *   <li>Retornar true si se eliminó al menos un registro</li>
     *   <li>Retornar false si no se encontró el registro</li>
     *   <li>Cerrar los recursos JDBC apropiadamente</li>
     *   <li><b>IMPORTANTE</b>: Considerar si es mejor hacer borrado lógico (UPDATE activo=false)
     *       en lugar de borrado físico (DELETE) para mantener auditoría</li>
     * </ul>
     *
     * @param id identificador de la entidad a eliminar
     * @return true si se eliminó exitosamente, false si no se encontró
     * @throws SQLException si ocurre un error al eliminar de la base de datos
     * @throws IllegalArgumentException si el id es null
     */
    boolean eliminar(Long id) throws SQLException;
}
