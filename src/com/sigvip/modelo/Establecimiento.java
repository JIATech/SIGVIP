package com.sigvip.modelo;

import com.sigvip.modelo.enums.ModalidadVisita;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Entidad que representa un establecimiento penitenciario.
 * Mapea a la tabla 'establecimientos' de la base de datos.
 *
 * <p><b>TP4 - Herencia de Clase Abstracta:</b></p>
 * Esta clase hereda de EntidadBase el campo activo.
 *
 * Especificación: PDF Sección 10.3, tabla 'establecimientos'
 * Responsabilidades: Configuración de horarios y modalidades de visita
 */
public class Establecimiento extends EntidadBase {

    // Atributos según especificación de tabla 'establecimientos'
    private Long idEstablecimiento;
    private String nombre;
    private String direccion;
    private String telefono;
    private ModalidadVisita modalidadVisita;
    private String diasHabilita;  // Días separados por coma: "LUNES,MIERCOLES,VIERNES"
    private Date horarioInicio;   // Hora de inicio de visitas (solo componente TIME)
    private Date horarioFin;      // Hora de fin de visitas (solo componente TIME)
    private int capacidadMaxima;
    // activo heredado de EntidadBase

    // Relaciones
    private List<Interno> internos;

    /**
     * Constructor vacío requerido para instanciación en DAOs.
     */
    public Establecimiento() {
        super(); // Inicializa activo=true
        this.internos = new ArrayList<>();
        this.diasHabilita = "LUNES,MARTES,MIERCOLES,JUEVES,VIERNES"; // Default
    }

    /**
     * Constructor con datos mínimos requeridos.
     *
     * @param nombre nombre del establecimiento
     * @param modalidadVisita tipo de régimen de visitas
     */
    public Establecimiento(String nombre, ModalidadVisita modalidadVisita) {
        this();
        this.nombre = nombre;
        this.modalidadVisita = modalidadVisita;
    }

    // ===== MÉTODOS DE VALIDACIÓN Y NEGOCIO =====

    /**
     * Verifica si un horario específico está dentro del horario permitido de visitas.
     * Método crítico para RF003: Control de Ingreso.
     *
     * Validaciones:
     * 1. El día de la semana debe estar en la lista de días habilitados
     * 2. La hora debe estar entre horarioInicio y horarioFin
     *
     * @param fechaHora fecha y hora a validar
     * @return true si el horario permite visitas
     */
    public boolean horarioPermiteVisita(Date fechaHora) {
        if (fechaHora == null || horarioInicio == null || horarioFin == null) {
            return false;
        }

        if (!isActivo()) { // Usa isActivo() heredado de EntidadBase
            return false;
        }

        // Convertir Date a LocalDate y LocalTime
        LocalDate fecha = fechaHora.toInstant()
                                   .atZone(ZoneId.systemDefault())
                                   .toLocalDate();

        LocalTime hora = fechaHora.toInstant()
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalTime();

        // Validar día de la semana
        if (!esDiaHabilitado(fecha.getDayOfWeek())) {
            return false;
        }

        // Convertir horarios de Date a LocalTime
        LocalTime inicio = horarioInicio.toInstant()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalTime();

        LocalTime fin = horarioFin.toInstant()
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalTime();

        // Validar que la hora esté dentro del rango
        return !hora.isBefore(inicio) && !hora.isAfter(fin);
    }

    /**
     * Verifica si un día de la semana está habilitado para visitas.
     *
     * @param dia día de la semana a verificar
     * @return true si el día está habilitado
     */
    public boolean esDiaHabilitado(DayOfWeek dia) {
        if (diasHabilita == null || diasHabilita.isEmpty()) {
            return false;
        }

        // Convertir el día a español para comparar con diasHabilita
        String diaNombre = convertirDiaAEspanol(dia).toUpperCase();

        // Buscar en la lista de días separados por coma
        String[] diasArray = diasHabilita.split(",");
        for (String diaHabilitado : diasArray) {
            if (diaHabilitado.trim().equalsIgnoreCase(diaNombre)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Convierte DayOfWeek a nombre en español.
     *
     * @param dia día de la semana en inglés
     * @return nombre del día en español
     */
    private String convertirDiaAEspanol(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> "LUNES";
            case TUESDAY -> "MARTES";
            case WEDNESDAY -> "MIERCOLES";
            case THURSDAY -> "JUEVES";
            case FRIDAY -> "VIERNES";
            case SATURDAY -> "SABADO";
            case SUNDAY -> "DOMINGO";
        };
    }

    /**
     * Establece los días habilitados para visitas.
     *
     * @param dias array de días en español (ej: ["LUNES", "MIERCOLES", "VIERNES"])
     */
    public void establecerDiasHabilitados(String[] dias) {
        if (dias == null || dias.length == 0) {
            throw new IllegalArgumentException("Debe especificar al menos un día habilitado");
        }

        this.diasHabilita = String.join(",", dias);
    }

    /**
     * Obtiene los días habilitados como array.
     *
     * @return array de días habilitados
     */
    public String[] obtenerDiasHabilitadosArray() {
        if (diasHabilita == null || diasHabilita.isEmpty()) {
            return new String[0];
        }

        return diasHabilita.split(",");
    }

    /**
     * Configura el horario de visitas.
     *
     * @param inicio hora de inicio (ej: 09:00)
     * @param fin hora de fin (ej: 17:00)
     */
    public void establecerHorarioVisitas(Date inicio, Date fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las horas de inicio y fin no pueden ser nulas");
        }

        LocalTime horaInicio = inicio.toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalTime();

        LocalTime horaFin = fin.toInstant()
                              .atZone(ZoneId.systemDefault())
                              .toLocalTime();

        if (horaFin.isBefore(horaInicio)) {
            throw new IllegalArgumentException("La hora de fin no puede ser anterior a la hora de inicio");
        }

        this.horarioInicio = inicio;
        this.horarioFin = fin;
    }

    /**
     * Obtiene el horario de visitas formateado.
     *
     * @return horario en formato "HH:mm a HH:mm"
     */
    public String getHorarioFormateado() {
        if (horarioInicio == null || horarioFin == null) {
            return "No configurado";
        }

        LocalTime inicio = horarioInicio.toInstant()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalTime();

        LocalTime fin = horarioFin.toInstant()
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalTime();

        return String.format("%02d:%02d a %02d:%02d",
                           inicio.getHour(), inicio.getMinute(),
                           fin.getHour(), fin.getMinute());
    }

    /**
     * Obtiene los internos activos del establecimiento.
     *
     * @return lista de internos activos
     */
    public List<Interno> obtenerInternosActivos() {
        if (internos == null || internos.isEmpty()) {
            return new ArrayList<>();
        }

        return internos.stream()
                      .filter(Interno::estaDisponibleParaVisita)
                      .toList();
    }

    /**
     * Verifica si el establecimiento ha alcanzado su capacidad máxima.
     *
     * @param visitasActuales número de visitas en curso
     * @return true si se alcanzó la capacidad máxima
     */
    public boolean capacidadAlcanzada(int visitasActuales) {
        if (capacidadMaxima <= 0) {
            return false;  // Sin límite configurado
        }

        return visitasActuales >= capacidadMaxima;
    }

    // ===== IMPLEMENTACIÓN DE MÉTODOS ABSTRACTOS DE EntidadBase =====

    /**
     * Valida las reglas de negocio para un establecimiento.
     *
     * @return true si todas las validaciones pasan
     * @throws IllegalStateException si hay errores críticos de validación
     */
    @Override
    public boolean validar() throws IllegalStateException {
        StringBuilder errores = new StringBuilder();

        if (nombre == null || nombre.trim().isEmpty()) {
            errores.append("Nombre obligatorio. ");
        }

        if (modalidadVisita == null) {
            errores.append("Modalidad de visita obligatoria. ");
        }

        if (horarioInicio == null) {
            errores.append("Horario de inicio obligatorio. ");
        }

        if (horarioFin == null) {
            errores.append("Horario de fin obligatorio. ");
        }

        if (diasHabilita == null || diasHabilita.trim().isEmpty()) {
            errores.append("Días habilitados obligatorios. ");
        }

        if (errores.length() > 0) {
            throw new IllegalStateException("Establecimiento inválido: " + errores.toString());
        }

        return true;
    }

    /**
     * Obtiene un resumen textual del establecimiento para logs y auditoría.
     *
     * @return String con resumen de la entidad
     */
    @Override
    public String obtenerResumen() {
        return String.format("Establecimiento[ID=%d, Nombre=%s, Modalidad=%s, Horario=%s, Activo=%s]",
                idEstablecimiento != null ? idEstablecimiento : 0L,
                nombre != null ? nombre : "N/A",
                modalidadVisita != null ? modalidadVisita : "N/A",
                getHorarioFormateado(),
                isActivo() ? "SÍ" : "NO");
    }

    /**
     * Verifica si el establecimiento es nuevo (aún no persistido).
     *
     * @return true si idEstablecimiento es null
     */
    @Override
    public boolean esNuevo() {
        return idEstablecimiento == null;
    }

    // ===== GETTERS Y SETTERS =====

    public Long getIdEstablecimiento() {
        return idEstablecimiento;
    }

    public void setIdEstablecimiento(Long idEstablecimiento) {
        this.idEstablecimiento = idEstablecimiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public ModalidadVisita getModalidadVisita() {
        return modalidadVisita;
    }

    public void setModalidadVisita(ModalidadVisita modalidadVisita) {
        this.modalidadVisita = modalidadVisita;
    }

    public String getDiasHabilita() {
        return diasHabilita;
    }

    public void setDiasHabilita(String diasHabilita) {
        this.diasHabilita = diasHabilita;
    }

    public Date getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(Date horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public Date getHorarioFin() {
        return horarioFin;
    }

    public void setHorarioFin(Date horarioFin) {
        this.horarioFin = horarioFin;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    // isActivo() y setActivo() heredados de EntidadBase

    public List<Interno> getInternos() {
        return internos;
    }

    public void setInternos(List<Interno> internos) {
        this.internos = internos;
    }

    // ===== EQUALS, HASHCODE Y TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Establecimiento that = (Establecimiento) o;
        return Objects.equals(idEstablecimiento, that.idEstablecimiento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idEstablecimiento);
    }

    @Override
    public String toString() {
        return "Establecimiento{" +
                "id=" + idEstablecimiento +
                ", nombre='" + nombre + '\'' +
                ", modalidad=" + modalidadVisita +
                ", horario=" + getHorarioFormateado() +
                ", diasHabilita='" + diasHabilita + '\'' +
                ", activo=" + activo +
                '}';
    }
}
