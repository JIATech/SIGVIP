package com.sigvip.modelo.enums;

/**
 * Tipos de restricción que pueden aplicarse a un visitante.
 * Especificado en tabla 'restricciones' de la base de datos.
 */
public enum TipoRestriccion {
    CONDUCTA("Conducta - Por comportamiento inadecuado"),
    JUDICIAL("Judicial - Orden judicial o fiscal"),
    ADMINISTRATIVA("Administrativa - Decisión administrativa del establecimiento"),
    SEGURIDAD("Seguridad - Por razones de seguridad del establecimiento");

    private final String descripcion;

    TipoRestriccion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina si esta restricción requiere aprobación superior para levantarse.
     */
    public boolean requiereAprobacionSuperior() {
        return this == JUDICIAL || this == SEGURIDAD;
    }
}
