package com.sigvip.modelo.enums;

/**
 * Tipos de relación entre visitante e interno.
 * Especificado en tabla 'autorizaciones' de la base de datos.
 */
public enum TipoRelacion {
    PADRE("Padre"),
    MADRE("Madre"),
    HIJO_A("Hijo/a"),
    HERMANO_A("Hermano/a"),
    CONYUGE("Cónyuge"),
    CONCUBINO_A("Concubino/a"),
    AMIGO("Amigo/a"),
    FAMILIAR("Otro familiar"),
    ABOGADO("Abogado/Defensor"),
    OTRO("Otro tipo de relación");

    private final String descripcion;

    TipoRelacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina si la relación es de tipo familiar directo.
     */
    public boolean esFamiliarDirecto() {
        return this == PADRE || this == MADRE || this == HIJO_A ||
               this == HERMANO_A || this == CONYUGE || this == CONCUBINO_A;
    }

    /**
     * Determina si la relación requiere documentación especial.
     */
    public boolean requiereDocumentacionEspecial() {
        return this == ABOGADO;
    }
}
