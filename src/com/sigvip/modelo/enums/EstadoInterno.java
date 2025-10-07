package com.sigvip.modelo.enums;

/**
 * Estados posibles de un interno en el sistema.
 * Especificado en tabla 'internos' de la base de datos.
 */
public enum EstadoInterno {
    ACTIVO("Activo - Presente en el establecimiento"),
    TRASLADADO("Trasladado - Transferido a otro establecimiento"),
    EGRESADO("Egresado - Liberado o finalizada su permanencia");

    private final String descripcion;

    EstadoInterno(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina si el interno puede recibir visitas.
     */
    public boolean puedeRecibirVisitas() {
        return this == ACTIVO;
    }
}
