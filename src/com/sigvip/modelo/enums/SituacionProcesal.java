package com.sigvip.modelo.enums;

/**
 * Situación procesal de un interno.
 * Especificado en tabla 'internos' de la base de datos.
 */
public enum SituacionProcesal {
    PROCESADO("Procesado - Con causa judicial en trámite"),
    CONDENADO("Condenado - Con sentencia firme");

    private final String descripcion;

    SituacionProcesal(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
