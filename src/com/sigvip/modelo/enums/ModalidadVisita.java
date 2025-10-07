package com.sigvip.modelo.enums;

/**
 * Modalidades de organización de visitas en un establecimiento.
 * Especificado en tabla 'establecimientos' de la base de datos.
 */
public enum ModalidadVisita {
    PRESENCIAL("Presencial - Visita directa sin separación física"),
    SECTOR("Sector - Visita organizada por sectores/pabellones"),
    MIXTA("Mixta - Combinación de modalidades según interno");

    private final String descripcion;

    ModalidadVisita(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
