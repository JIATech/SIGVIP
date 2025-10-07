package com.sigvip.modelo.enums;

/**
 * Estados posibles de un visitante en el sistema.
 * Especificado en tabla 'visitantes' de la base de datos.
 */
public enum EstadoVisitante {
    ACTIVO("Activo - Habilitado para visitas"),
    SUSPENDIDO("Suspendido - Temporalmente inhabilitado"),
    INACTIVO("Inactivo - No habilitado para visitas");

    private final String descripcion;

    EstadoVisitante(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
