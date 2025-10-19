package com.sigvip.modelo;

import com.sigvip.modelo.enums.TipoReporte;

import java.util.Date;
import java.util.Objects;

/**
 * Entidad que representa un reporte generado en el sistema.
 * Almacena el contenido HTML y metadatos para auditoría.
 *
 * Tabla: reportes_generados
 */
public class ReporteGenerado {

    // Atributos según tabla 'reportes_generados'
    private Long idReporte;
    private TipoReporte tipoReporte;
    private String titulo;
    private String parametrosFiltro;  // JSON con filtros aplicados
    private String contenido;         // HTML del reporte
    private Integer totalRegistros;
    private Date fechaGeneracion;
    private Long idGeneradoPor;

    // Relación con usuario que generó el reporte
    private Usuario usuarioGenerador;

    /**
     * Constructor vacío requerido para DAOs.
     */
    public ReporteGenerado() {
        this.fechaGeneracion = new Date();
        this.totalRegistros = 0;
    }

    /**
     * Constructor con datos básicos.
     */
    public ReporteGenerado(TipoReporte tipoReporte, String titulo, String contenido,
                          Long idGeneradoPor) {
        this();
        this.tipoReporte = tipoReporte;
        this.titulo = titulo;
        this.contenido = contenido;
        this.idGeneradoPor = idGeneradoPor;
    }

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Obtiene el tamaño del contenido en KB.
     */
    public double getTamanoKB() {
        if (contenido == null) return 0;
        return contenido.getBytes().length / 1024.0;
    }

    /**
     * Verifica si el reporte es reciente (generado en las últimas 24 horas).
     */
    public boolean esReciente() {
        long veinticuatroHoras = 24 * 60 * 60 * 1000L;
        return (System.currentTimeMillis() - fechaGeneracion.getTime()) < veinticuatroHoras;
    }

    /**
     * Obtiene un resumen del contenido para mostrar en listados.
     */
    public String getResumenContenido() {
        if (contenido == null || contenido.length() <= 100) {
            return contenido;
        }
        return contenido.substring(0, 100) + "...";
    }

    /**
     * Verifica si el reporte tiene parámetros de filtro.
     */
    public boolean tieneFiltros() {
        return parametrosFiltro != null && !parametrosFiltro.trim().isEmpty();
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(Long idReporte) {
        this.idReporte = idReporte;
    }

    public TipoReporte getTipoReporte() {
        return tipoReporte;
    }

    public void setTipoReporte(TipoReporte tipoReporte) {
        this.tipoReporte = tipoReporte;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getParametrosFiltro() {
        return parametrosFiltro;
    }

    public void setParametrosFiltro(String parametrosFiltro) {
        this.parametrosFiltro = parametrosFiltro;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Integer getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Integer totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public Long getIdGeneradoPor() {
        return idGeneradoPor;
    }

    public void setIdGeneradoPor(Long idGeneradoPor) {
        this.idGeneradoPor = idGeneradoPor;
    }

    public Usuario getUsuarioGenerador() {
        return usuarioGenerador;
    }

    public void setUsuarioGenerador(Usuario usuarioGenerador) {
        this.usuarioGenerador = usuarioGenerador;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReporteGenerado that = (ReporteGenerado) o;
        return Objects.equals(idReporte, that.idReporte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReporte);
    }

    @Override
    public String toString() {
        return "ReporteGenerado{" +
                "idReporte=" + idReporte +
                ", tipoReporte=" + tipoReporte +
                ", titulo='" + titulo + '\'' +
                ", totalRegistros=" + totalRegistros +
                ", fechaGeneracion=" + fechaGeneracion +
                '}';
    }
}