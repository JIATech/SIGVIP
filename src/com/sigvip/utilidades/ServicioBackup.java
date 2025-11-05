package com.sigvip.utilidades;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Servicio para realizar backups y restauración de archivos de configuración.
 *
 * Cumple con requisito académico TP4: "Uso de archivos para guardar y recuperar información"
 *
 * Funcionalidades:
 * - Backup de archivo config.properties con timestamp
 * - Restauración desde backup
 * - Listado de backups disponibles
 * - Verificación de integridad de configuración
 * - Limpieza de backups antiguos
 *
 * Patrón: Singleton para garantizar gestión centralizada de backups
 *
 * Ejemplo de uso:
 * <pre>
 * // Crear backup antes de modificar configuración
 * String rutaBackup = ServicioBackup.getInstancia().crearBackupConfiguracion();
 *
 * // Restaurar configuración desde backup
 * String[] backups = ServicioBackup.getInstancia().listarBackups();
 * ServicioBackup.getInstancia().restaurarConfiguracion(backups[0]);
 * </pre>
 *
 * @author Juan Ignacio Arnaboldi
 * @version 1.0 - TP4
 */
public class ServicioBackup {

    private static ServicioBackup instancia;
    private static final String DIRECTORIO_BACKUPS = "backups";
    private static final String ARCHIVO_CONFIG = "resources/config.properties";
    private SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyyMMdd_HHmmss");

    /**
     * Constructor privado para implementar patrón Singleton.
     * Inicializa el directorio de backups si no existe.
     */
    private ServicioBackup() {
        inicializarDirectorioBackups();
    }

    /**
     * Obtiene la instancia única del servicio (patrón Singleton).
     *
     * @return instancia única de ServicioBackup
     */
    public static synchronized ServicioBackup getInstancia() {
        if (instancia == null) {
            instancia = new ServicioBackup();
        }
        return instancia;
    }

    /**
     * Crea el directorio de backups si no existe.
     * Se ejecuta automáticamente en el constructor.
     */
    private void inicializarDirectorioBackups() {
        File directorio = new File(DIRECTORIO_BACKUPS);
        if (!directorio.exists()) {
            directorio.mkdirs();
        }
    }

    /**
     * Crea un backup del archivo de configuración.
     * El nombre del backup incluye timestamp para evitar sobreescrituras.
     * Formato: config_backup_YYYYMMDD_HHMMSS.properties
     *
     * @return ruta completa del archivo de backup creado
     * @throws IOException si ocurre un error al crear el backup
     */
    public String crearBackupConfiguracion() throws IOException {
        String timestamp = formatoFecha.format(new Date());
        String nombreBackup = "config_backup_" + timestamp + ".properties";
        String rutaBackup = DIRECTORIO_BACKUPS + "/" + nombreBackup;

        // Verificar que existe el archivo de configuración
        File archivoConfig = new File(ARCHIVO_CONFIG);
        if (!archivoConfig.exists()) {
            throw new FileNotFoundException("Archivo de configuración no encontrado: " + ARCHIVO_CONFIG);
        }

        // Copiar archivo de configuración a backup
        copiarArchivo(ARCHIVO_CONFIG, rutaBackup);

        // Registrar backup en logs
        ServicioLogs.getInstancia().info("SISTEMA", "BACKUP",
            "Backup de configuración creado: " + nombreBackup);

        return rutaBackup;
    }

    /**
     * Restaura la configuración desde un backup específico.
     * Antes de restaurar, crea un backup de seguridad de la configuración actual.
     *
     * @param nombreBackup nombre del archivo de backup (sin ruta)
     * @throws IOException si ocurre un error al restaurar
     */
    public void restaurarConfiguracion(String nombreBackup) throws IOException {
        String rutaBackup = DIRECTORIO_BACKUPS + "/" + nombreBackup;
        File backup = new File(rutaBackup);

        if (!backup.exists()) {
            throw new FileNotFoundException("Backup no encontrado: " + nombreBackup);
        }

        // Crear backup de seguridad antes de restaurar
        String backupSeguridad = crearBackupConfiguracion();

        try {
            // Restaurar desde backup
            copiarArchivo(rutaBackup, ARCHIVO_CONFIG);

            ServicioLogs.getInstancia().info("SISTEMA", "RESTORE",
                "Configuración restaurada desde: " + nombreBackup);

        } catch (IOException e) {
            // Si falla la restauración, registrar error
            ServicioLogs.getInstancia().error("SISTEMA", "RESTORE",
                "Error al restaurar configuración: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Lista todos los backups disponibles en el directorio de backups.
     * Filtra solo archivos .properties.
     *
     * @return array con nombres de archivos de backup (ordenados alfabéticamente)
     */
    public String[] listarBackups() {
        File directorio = new File(DIRECTORIO_BACKUPS);

        // Filtrar solo archivos .properties (uso de lambda - TP4)
        String[] backups = directorio.list((dir, name) -> name.endsWith(".properties"));

        return backups != null ? backups : new String[0];
    }

    /**
     * Copia un archivo de origen a destino usando streams buffered.
     * Utiliza buffer de 4KB para eficiencia.
     *
     * @param origen ruta del archivo origen
     * @param destino ruta del archivo destino
     * @throws IOException si ocurre un error al copiar
     */
    private void copiarArchivo(String origen, String destino) throws IOException {
        // Try-with-resources para cierre automático de streams
        try (FileInputStream fis = new FileInputStream(origen);
             FileOutputStream fos = new FileOutputStream(destino);
             BufferedInputStream bis = new BufferedInputStream(fis);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            byte[] buffer = new byte[4096]; // Buffer de 4KB
            int bytesLeidos;

            while ((bytesLeidos = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesLeidos);
            }
        }
    }

    /**
     * Lee y verifica la configuración actual.
     * Útil para validar integridad del archivo de configuración.
     *
     * @return objeto Properties con la configuración cargada
     * @throws IOException si ocurre un error al leer la configuración
     */
    public Properties leerConfiguracion() throws IOException {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(ARCHIVO_CONFIG)) {
            props.load(fis);
        }

        return props;
    }

    /**
     * Verifica que la configuración actual tiene las claves requeridas.
     *
     * @param clavesRequeridas array con nombres de propiedades requeridas
     * @return true si todas las claves existen
     * @throws IOException si ocurre un error al leer la configuración
     */
    public boolean verificarConfiguracion(String[] clavesRequeridas) throws IOException {
        Properties props = leerConfiguracion();

        for (String clave : clavesRequeridas) {
            if (!props.containsKey(clave)) {
                ServicioLogs.getInstancia().warning("SISTEMA", "CONFIG_CHECK",
                    "Falta clave de configuración: " + clave);
                return false;
            }
        }

        return true;
    }

    /**
     * Elimina backups antiguos (más de N días).
     * Útil para evitar acumulación excesiva de backups.
     *
     * @param diasRetencion número de días de retención de backups
     * @return cantidad de backups eliminados
     */
    public int limpiarBackupsAntiguos(int diasRetencion) {
        File directorio = new File(DIRECTORIO_BACKUPS);
        File[] backups = directorio.listFiles((dir, name) -> name.endsWith(".properties"));

        if (backups == null) {
            return 0;
        }

        // Calcular timestamp límite (milisegundos)
        long tiempoLimite = System.currentTimeMillis() - (diasRetencion * 24L * 60 * 60 * 1000);
        int eliminados = 0;

        // Recorrer backups y eliminar antiguos
        for (File backup : backups) {
            if (backup.lastModified() < tiempoLimite) {
                if (backup.delete()) {
                    eliminados++;
                }
            }
        }

        if (eliminados > 0) {
            ServicioLogs.getInstancia().info("SISTEMA", "CLEANUP",
                "Eliminados " + eliminados + " backups antiguos (>" + diasRetencion + " días)");
        }

        return eliminados;
    }

    /**
     * Cuenta el número total de backups disponibles.
     *
     * @return cantidad de archivos de backup
     */
    public int contarBackups() {
        return listarBackups().length;
    }

    /**
     * Obtiene el tamaño total ocupado por los backups (en bytes).
     *
     * @return tamaño total en bytes
     */
    public long obtenerTamanoTotalBackups() {
        File directorio = new File(DIRECTORIO_BACKUPS);
        File[] backups = directorio.listFiles((dir, name) -> name.endsWith(".properties"));

        if (backups == null) {
            return 0;
        }

        long tamanoTotal = 0;
        for (File backup : backups) {
            tamanoTotal += backup.length();
        }

        return tamanoTotal;
    }

    /**
     * Crea un backup de un archivo arbitrario (no solo configuración).
     *
     * @param rutaArchivo ruta del archivo a respaldar
     * @param nombreBackup nombre para el archivo de backup
     * @return ruta completa del backup creado
     * @throws IOException si ocurre un error al crear el backup
     */
    public String crearBackupPersonalizado(String rutaArchivo, String nombreBackup) throws IOException {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists()) {
            throw new FileNotFoundException("Archivo no encontrado: " + rutaArchivo);
        }

        String rutaBackup = DIRECTORIO_BACKUPS + "/" + nombreBackup;
        copiarArchivo(rutaArchivo, rutaBackup);

        ServicioLogs.getInstancia().info("SISTEMA", "BACKUP_CUSTOM",
            "Backup personalizado creado: " + nombreBackup);

        return rutaBackup;
    }
}
