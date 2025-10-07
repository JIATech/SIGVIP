package com.sigvip.modelo.enums;

/**
 * Estados de una autorización de visita.
 * Especificado en tabla 'autorizaciones' de la base de datos.
 */
public enum EstadoAutorizacion {
    VIGENTE("Vigente - Autorización activa y válida"),
    SUSPENDIDA("Suspendida - Temporalmente inactiva"),
    REVOCADA("Revocada - Cancelada permanentemente"),
    VENCIDA("Vencida - Expirada por fecha de vencimiento");

    private final String descripcion;

    EstadoAutorizacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina si la autorización permite realizar visitas.
     */
    public boolean permiteVisitas() {
        return this == VIGENTE;
    }
}
