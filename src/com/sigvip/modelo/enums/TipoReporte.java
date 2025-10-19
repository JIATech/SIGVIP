package com.sigvip.modelo.enums;

/**
 * Tipos de reportes disponibles en el sistema.
 * Cada tipo corresponde a una consulta específica para RF007.
 */
public enum TipoReporte {
    VISITAS_FECHA("Visitas por Fecha", "Reporte de visitas filtradas por rango de fechas"),
    VISITAS_VISITANTE("Visitas por Visitante", "Historial de visitas de un visitante específico"),
    VISITAS_INTERNO("Visitas por Interno", "Reporte de visitas a un interno específico"),
    ESTADISTICAS("Estadísticas Generales", "Estadísticas y métricas de visitas"),
    RESTRICCIONES_ACTIVAS("Restricciones Activas", "Listado de restricciones vigentes"),
    AUTORIZACIONES_VIGENTES("Autorizaciones Vigentes", "Autorizaciones actualmente vigentes");

    private final String nombre;
    private final String descripcion;

    TipoReporte(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Obtiene el nombre formateado para mostrar en UI.
     */
    public String getNombreFormateado() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }
}