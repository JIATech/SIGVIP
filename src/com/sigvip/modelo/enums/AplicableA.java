package com.sigvip.modelo.enums;

/**
 * Alcance de aplicación de una restricción.
 * Especificado en tabla 'restricciones' de la base de datos.
 */
public enum AplicableA {
    TODOS("Todos los internos"),
    INTERNO_ESPECIFICO("Interno específico únicamente");

    private final String descripcion;

    AplicableA(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
