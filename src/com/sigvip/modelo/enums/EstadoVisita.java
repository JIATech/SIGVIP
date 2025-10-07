package com.sigvip.modelo.enums;

/**
 * Estados del ciclo de vida de una visita.
 * Representa las transiciones: PROGRAMADA → EN_CURSO → FINALIZADA
 * o PROGRAMADA/EN_CURSO → CANCELADA
 * Especificado en tabla 'visitas' de la base de datos.
 */
public enum EstadoVisita {
    PROGRAMADA("Programada - Autorizada pero no iniciada"),
    EN_CURSO("En Curso - Visitante actualmente en el establecimiento"),
    FINALIZADA("Finalizada - Visita completada con egreso registrado"),
    CANCELADA("Cancelada - Visita no realizada");

    private final String descripcion;

    EstadoVisita(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Valida si es posible transicionar del estado actual al nuevo estado.
     */
    public boolean puedeTransicionarA(EstadoVisita nuevoEstado) {
        return switch (this) {
            case PROGRAMADA -> nuevoEstado == EN_CURSO || nuevoEstado == CANCELADA;
            case EN_CURSO -> nuevoEstado == FINALIZADA || nuevoEstado == CANCELADA;
            case FINALIZADA, CANCELADA -> false; // Estados terminales
        };
    }
}
