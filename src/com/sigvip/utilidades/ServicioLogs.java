package com.sigvip.utilidades;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Servicio de logging para operaciones críticas del sistema.
 *
 * Cumple con requisito académico TP4: "Uso de archivos para guardar y recuperar información"
 *
 * Funcionalidades:
 * - Registro de operaciones críticas en archivo de texto (.log)
 * - Lectura y consulta de logs históricos
 * - Formato estructurado: [TIMESTAMP] [NIVEL] [USUARIO] [OPERACION] Mensaje
 * - Búsqueda por nivel, usuario u operación
 *
 * Patrón: Singleton para garantizar acceso único al archivo de logs
 *
 * Ejemplo de uso:
 * <pre>
 * ServicioLogs.getInstancia().info("admin", "INGRESO_VISITA",
 *     "Visitante DNI 12345678 ingresó a visitar interno Legajo 001");
 *
 * List&lt;String&gt; errores = ServicioLogs.getInstancia().buscarPorNivel(NivelLog.ERROR);
 * </pre>
 *
 * @author Juan Ignacio Arnaboldi
 * @version 1.0 - TP4
 */
public class ServicioLogs {

    private static ServicioLogs instancia;
    private static final String ARCHIVO_LOG = "logs/sigvip_operaciones.log";
    private static final String DIRECTORIO_LOGS = "logs";
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * Niveles de log disponibles.
     * Permite clasificar la severidad de los eventos registrados.
     */
    public enum NivelLog {
        INFO,       // Operaciones normales
        WARNING,    // Advertencias (ej: autorización próxima a vencer)
        ERROR,      // Errores recuperables (ej: ingreso rechazado por restricción)
        CRITICO     // Errores críticos del sistema
    }

    /**
     * Constructor privado para implementar patrón Singleton.
     * Inicializa el directorio de logs si no existe.
     */
    private ServicioLogs() {
        inicializarDirectorioLogs();
    }

    /**
     * Obtiene la instancia única del servicio (patrón Singleton).
     *
     * @return instancia única de ServicioLogs
     */
    public static synchronized ServicioLogs getInstancia() {
        if (instancia == null) {
            instancia = new ServicioLogs();
        }
        return instancia;
    }

    /**
     * Crea el directorio de logs si no existe.
     * Se ejecuta automáticamente en el constructor.
     */
    private void inicializarDirectorioLogs() {
        File directorio = new File(DIRECTORIO_LOGS);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }

    /**
     * Registra una operación en el archivo de logs.
     * Formato: [TIMESTAMP] [NIVEL] [USUARIO] [OPERACION] Mensaje
     *
     * El método es sincronizado para evitar conflictos de escritura
     * cuando múltiples hilos escriben simultáneamente.
     *
     * @param nivel nivel de severidad del log
     * @param usuario nombre del usuario que ejecutó la operación
     * @param operacion tipo de operación (ej: INGRESO_VISITA, LOGIN, etc.)
     * @param mensaje descripción detallada del evento
     */
    public synchronized void registrar(NivelLog nivel, String usuario, String operacion, String mensaje) {
        String timestamp = formatoFecha.format(new Date());
        String lineaLog = String.format("[%s] [%s] [%s] [%s] %s%n",
                timestamp, nivel, usuario, operacion, mensaje);

        // FileWriter con append=true para agregar al final del archivo
        try (FileWriter fw = new FileWriter(ARCHIVO_LOG, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.print(lineaLog);

        } catch (IOException e) {
            // En caso de error al escribir, mostrar en consola como fallback
            System.err.println("Error al escribir en log: " + e.getMessage());
        }
    }

    /**
     * Lee todas las líneas del archivo de logs.
     * Cumple con requisito TP4: recuperar información desde archivo.
     *
     * @return lista con todas las líneas del archivo de logs
     * @throws IOException si ocurre un error al leer el archivo
     */
    public List<String> leerTodos() throws IOException {
        List<String> lineas = new ArrayList<>();
        File archivo = new File(ARCHIVO_LOG);

        if (!archivo.exists()) {
            return lineas; // Retorna lista vacía si no existe el archivo
        }

        try (FileReader fr = new FileReader(archivo);
             BufferedReader br = new BufferedReader(fr)) {

            String linea;
            while ((linea = br.readLine()) != null) {
                lineas.add(linea);
            }
        }

        return lineas;
    }

    /**
     * Busca logs por nivel específico.
     * Útil para filtrar solo errores o advertencias.
     *
     * @param nivel nivel de log a buscar
     * @return lista de logs que coinciden con el nivel
     * @throws IOException si ocurre un error al leer el archivo
     */
    public List<String> buscarPorNivel(NivelLog nivel) throws IOException {
        List<String> resultado = new ArrayList<>();
        List<String> todasLineas = leerTodos();
        String marcador = "[" + nivel + "]";

        for (String linea : todasLineas) {
            if (linea.contains(marcador)) {
                resultado.add(linea);
            }
        }

        return resultado;
    }

    /**
     * Busca logs por usuario específico.
     * Útil para auditoría de acciones de un operador.
     *
     * @param usuario nombre del usuario a buscar
     * @return lista de logs del usuario
     * @throws IOException si ocurre un error al leer el archivo
     */
    public List<String> buscarPorUsuario(String usuario) throws IOException {
        List<String> resultado = new ArrayList<>();
        List<String> todasLineas = leerTodos();

        for (String linea : todasLineas) {
            if (linea.contains("[" + usuario + "]")) {
                resultado.add(linea);
            }
        }

        return resultado;
    }

    /**
     * Busca logs por tipo de operación.
     * Útil para analizar operaciones críticas específicas.
     *
     * @param operacion tipo de operación a buscar
     * @return lista de logs de esa operación
     * @throws IOException si ocurre un error al leer el archivo
     */
    public List<String> buscarPorOperacion(String operacion) throws IOException {
        List<String> resultado = new ArrayList<>();
        List<String> todasLineas = leerTodos();

        for (String linea : todasLineas) {
            if (linea.contains("[" + operacion + "]")) {
                resultado.add(linea);
            }
        }

        return resultado;
    }

    /**
     * Cuenta el número total de logs registrados.
     *
     * @return cantidad de líneas en el archivo de logs
     * @throws IOException si ocurre un error al leer el archivo
     */
    public int contarLogs() throws IOException {
        return leerTodos().size();
    }

    /**
     * Limpia el archivo de logs (usar con precaución).
     * Elimina todo el contenido del archivo de logs.
     *
     * @throws IOException si ocurre un error al limpiar el archivo
     */
    public synchronized void limpiarLogs() throws IOException {
        File archivo = new File(ARCHIVO_LOG);
        if (archivo.exists()) {
            new PrintWriter(archivo).close(); // Trunca el archivo
        }
    }

    /**
     * Exporta los logs a un archivo diferente (útil para backups).
     *
     * @param rutaDestino ruta del archivo destino
     * @throws IOException si ocurre un error al copiar
     */
    public void exportarLogs(String rutaDestino) throws IOException {
        List<String> lineas = leerTodos();

        try (FileWriter fw = new FileWriter(rutaDestino, false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            for (String linea : lineas) {
                out.println(linea);
            }
        }
    }

    // ========== MÉTODOS DE CONVENIENCIA ==========

    /**
     * Registra un log de nivel INFO.
     *
     * @param usuario nombre del usuario
     * @param operacion tipo de operación
     * @param mensaje descripción del evento
     */
    public void info(String usuario, String operacion, String mensaje) {
        registrar(NivelLog.INFO, usuario, operacion, mensaje);
    }

    /**
     * Registra un log de nivel WARNING.
     *
     * @param usuario nombre del usuario
     * @param operacion tipo de operación
     * @param mensaje descripción de la advertencia
     */
    public void warning(String usuario, String operacion, String mensaje) {
        registrar(NivelLog.WARNING, usuario, operacion, mensaje);
    }

    /**
     * Registra un log de nivel ERROR.
     *
     * @param usuario nombre del usuario
     * @param operacion tipo de operación
     * @param mensaje descripción del error
     */
    public void error(String usuario, String operacion, String mensaje) {
        registrar(NivelLog.ERROR, usuario, operacion, mensaje);
    }

    /**
     * Registra un log de nivel CRITICO.
     *
     * @param usuario nombre del usuario
     * @param operacion tipo de operación
     * @param mensaje descripción del error crítico
     */
    public void critico(String usuario, String operacion, String mensaje) {
        registrar(NivelLog.CRITICO, usuario, operacion, mensaje);
    }
}
