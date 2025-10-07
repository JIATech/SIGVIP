package com.sigvip.modelo.enums;

/**
 * Roles de usuarios del sistema con diferentes niveles de acceso.
 * Especificado en tabla 'usuarios' de la base de datos.
 */
public enum Rol {
    OPERADOR("Operador", 1, "Registro y control diario de visitas"),
    SUPERVISOR("Supervisor", 2, "Consultas avanzadas, reportes y supervisión"),
    ADMINISTRADOR("Administrador", 3, "Gestión completa del sistema");

    private final String nombre;
    private final int nivelAcceso;
    private final String descripcion;

    Rol(String nombre, int nivelAcceso, String descripcion) {
        this.nombre = nombre;
        this.nivelAcceso = nivelAcceso;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNivelAcceso() {
        return nivelAcceso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si este rol tiene permisos superiores o iguales al rol especificado.
     */
    public boolean tienePermisosDe(Rol otroRol) {
        return this.nivelAcceso >= otroRol.nivelAcceso;
    }
}
