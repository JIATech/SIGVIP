package com.sigvip.persistencia;

/**
 * Gestor del modo de funcionamiento de la aplicación.
 * Patrón Singleton para mantener estado global del modo (ONLINE/OFFLINE).
 *
 * <p>Cuando está en modo OFFLINE:
 * - Los DAOs usan RepositorioMemoria en lugar de JDBC
 * - Los datos se almacenan solo en RAM (se pierden al cerrar)
 * - Se cargan datos de prueba automáticamente
 *
 * <p>Cuando está en modo ONLINE:
 * - Funcionamiento normal con MySQL
 * - Persistencia completa en base de datos
 *
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class GestorModo {

    private static GestorModo instancia;
    private boolean modoOffline;

    /**
     * Constructor privado para patrón Singleton.
     * Por defecto arranca en modo ONLINE (se espera conexión a BD).
     */
    private GestorModo() {
        this.modoOffline = false;
    }

    /**
     * Obtiene la instancia única del gestor de modo.
     *
     * @return instancia singleton
     */
    public static synchronized GestorModo getInstancia() {
        if (instancia == null) {
            instancia = new GestorModo();
        }
        return instancia;
    }

    /**
     * Activa el modo OFFLINE.
     * Los DAOs comenzarán a usar RepositorioMemoria en lugar de MySQL.
     */
    public void activarModoOffline() {
        this.modoOffline = true;
    }

    /**
     * Activa el modo ONLINE.
     * Los DAOs usarán conexión JDBC a MySQL.
     */
    public void activarModoOnline() {
        this.modoOffline = false;
    }

    /**
     * Verifica si la aplicación está en modo offline.
     *
     * @return true si está en modo offline, false si está online
     */
    public boolean isModoOffline() {
        return modoOffline;
    }

    /**
     * Obtiene una descripción textual del modo actual.
     *
     * @return "OFFLINE" o "ONLINE"
     */
    public String getModoTexto() {
        return modoOffline ? "OFFLINE" : "ONLINE";
    }
}
