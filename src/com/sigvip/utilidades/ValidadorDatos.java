package com.sigvip.utilidades;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Clase de utilidad para validación de datos de entrada.
 * Proporciona métodos estáticos para validar formatos y reglas de negocio.
 *
 * Especificación: PDF Sección 12 - Anexos (validaciones)
 * Responsabilidad: Validación de datos antes de persistencia
 */
public class ValidadorDatos {

    // Patrones regex para validación
    private static final Pattern PATRON_DNI = Pattern.compile("^\\d{7,8}$");
    private static final Pattern PATRON_EMAIL = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PATRON_TELEFONO = Pattern.compile(
        "^[\\d\\s\\-\\(\\)]{7,15}$");
    private static final Pattern PATRON_LEGAJO = Pattern.compile("^[A-Z0-9]{4,12}$");

    // Constantes de validación
    private static final int EDAD_MINIMA_VISITANTE = 18;
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    /**
     * Constructor privado para evitar instanciación.
     */
    private ValidadorDatos() {
        throw new UnsupportedOperationException("Clase de utilidad no instanciable");
    }

    // ===== VALIDACIONES DE FORMATO =====

    /**
     * Valida el formato de un DNI argentino.
     * Formato: 7 u 8 dígitos numéricos.
     *
     * @param dni DNI a validar
     * @return true si el formato es válido
     */
    public static boolean validarDNI(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return PATRON_DNI.matcher(dni.trim()).matches();
    }

    /**
     * Valida el formato de un email.
     *
     * @param email email a validar
     * @return true si el formato es válido
     */
    public static boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return PATRON_EMAIL.matcher(email.trim()).matches();
    }

    /**
     * Valida el formato de un teléfono.
     * Acepta números, espacios, guiones y paréntesis.
     *
     * @param telefono teléfono a validar
     * @return true si el formato es válido
     */
    public static boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }
        return PATRON_TELEFONO.matcher(telefono.trim()).matches();
    }

    /**
     * Valida el formato de un número de legajo.
     * Formato: 4-12 caracteres alfanuméricos en mayúscula.
     *
     * @param legajo legajo a validar
     * @return true si el formato es válido
     */
    public static boolean validarLegajo(String legajo) {
        if (legajo == null || legajo.trim().isEmpty()) {
            return false;
        }
        return PATRON_LEGAJO.matcher(legajo.trim()).matches();
    }

    /**
     * Valida que una contraseña cumpla con los requisitos mínimos.
     * Requisito: mínimo 8 caracteres.
     *
     * @param contrasena contraseña a validar
     * @return true si cumple los requisitos
     */
    public static boolean validarContrasena(String contrasena) {
        if (contrasena == null) {
            return false;
        }
        return contrasena.length() >= LONGITUD_MINIMA_CONTRASENA;
    }

    /**
     * Valida que un nombre no esté vacío y tenga longitud razonable.
     *
     * @param nombre nombre a validar
     * @return true si es válido
     */
    public static boolean validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        String nombreLimpio = nombre.trim();
        return nombreLimpio.length() >= 2 && nombreLimpio.length() <= 100;
    }

    // ===== VALIDACIONES DE REGLAS DE NEGOCIO =====

    /**
     * Valida que la edad de una persona sea mayor o igual a la mínima requerida.
     * Requisito: Visitantes deben ser mayores de 18 años.
     *
     * @param fechaNacimiento fecha de nacimiento
     * @return true si cumple con la edad mínima
     */
    public static boolean validarEdadMinima(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return false;
        }

        LocalDate fechaNac = fechaNacimiento.toInstant()
                                           .atZone(ZoneId.systemDefault())
                                           .toLocalDate();

        LocalDate hoy = LocalDate.now();
        int edad = Period.between(fechaNac, hoy).getYears();

        return edad >= EDAD_MINIMA_VISITANTE;
    }

    /**
     * Valida que una fecha no sea futura.
     *
     * @param fecha fecha a validar
     * @return true si no es futura
     */
    public static boolean validarFechaNoFutura(Date fecha) {
        if (fecha == null) {
            return false;
        }

        LocalDate fechaLocal = fecha.toInstant()
                                   .atZone(ZoneId.systemDefault())
                                   .toLocalDate();

        return !fechaLocal.isAfter(LocalDate.now());
    }

    /**
     * Valida que una fecha esté dentro de un rango razonable.
     * Útil para fechas de nacimiento, ingreso, etc.
     *
     * @param fecha fecha a validar
     * @param anosAtras años hacia atrás permitidos
     * @param anosAdelante años hacia adelante permitidos
     * @return true si está en el rango
     */
    public static boolean validarRangoFecha(Date fecha, int anosAtras, int anosAdelante) {
        if (fecha == null) {
            return false;
        }

        LocalDate fechaLocal = fecha.toInstant()
                                   .atZone(ZoneId.systemDefault())
                                   .toLocalDate();

        LocalDate hoy = LocalDate.now();
        LocalDate limiteInferior = hoy.minusYears(anosAtras);
        LocalDate limiteSuperior = hoy.plusYears(anosAdelante);

        return !fechaLocal.isBefore(limiteInferior) && !fechaLocal.isAfter(limiteSuperior);
    }

    /**
     * Valida que fechaInicio sea anterior a fechaFin.
     *
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return true si el orden es correcto
     */
    public static boolean validarOrdenFechas(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            return false;
        }

        return fechaInicio.before(fechaFin);
    }

    /**
     * Valida que un horario esté en formato válido (hora entre 0-23, minuto 0-59).
     *
     * @param hora hora a validar (0-23)
     * @param minuto minuto a validar (0-59)
     * @return true si el horario es válido
     */
    public static boolean validarHorario(int hora, int minuto) {
        return hora >= 0 && hora <= 23 && minuto >= 0 && minuto <= 59;
    }

    /**
     * Valida que un valor entero esté dentro de un rango.
     *
     * @param valor valor a validar
     * @param min mínimo inclusive
     * @param max máximo inclusive
     * @return true si está en el rango
     */
    public static boolean validarRango(int valor, int min, int max) {
        return valor >= min && valor <= max;
    }

    // ===== MÉTODOS DE SANITIZACIÓN =====

    /**
     * Limpia y normaliza un DNI eliminando caracteres no numéricos.
     *
     * @param dni DNI a limpiar
     * @return DNI solo con dígitos
     */
    public static String limpiarDNI(String dni) {
        if (dni == null) {
            return "";
        }
        return dni.replaceAll("[^0-9]", "");
    }

    /**
     * Normaliza un nombre eliminando espacios extras y capitalizando.
     *
     * @param nombre nombre a normalizar
     * @return nombre normalizado
     */
    public static String normalizarNombre(String nombre) {
        if (nombre == null) {
            return "";
        }

        // Eliminar espacios extras
        String limpio = nombre.trim().replaceAll("\\s+", " ");

        // Capitalizar cada palabra
        String[] palabras = limpio.split(" ");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)));
                if (palabra.length() > 1) {
                    resultado.append(palabra.substring(1).toLowerCase());
                }
                resultado.append(" ");
            }
        }

        return resultado.toString().trim();
    }

    /**
     * Normaliza un legajo a mayúsculas.
     *
     * @param legajo legajo a normalizar
     * @return legajo en mayúsculas
     */
    public static String normalizarLegajo(String legajo) {
        if (legajo == null) {
            return "";
        }
        return legajo.trim().toUpperCase();
    }

    /**
     * Valida y normaliza un email.
     *
     * @param email email a normalizar
     * @return email en minúsculas y sin espacios
     */
    public static String normalizarEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    // ===== MENSAJES DE ERROR =====

    /**
     * Obtiene un mensaje de error descriptivo para un DNI inválido.
     *
     * @param dni DNI que falló la validación
     * @return mensaje de error
     */
    public static String mensajeErrorDNI(String dni) {
        if (dni == null || dni.isEmpty()) {
            return "El DNI no puede estar vacío";
        }
        if (!validarDNI(dni)) {
            return "El DNI debe contener 7 u 8 dígitos numéricos";
        }
        return "DNI válido";
    }

    /**
     * Obtiene un mensaje de error descriptivo para un email inválido.
     *
     * @param email email que falló la validación
     * @return mensaje de error
     */
    public static String mensajeErrorEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "El email no puede estar vacío";
        }
        if (!validarEmail(email)) {
            return "El formato del email no es válido (ejemplo: usuario@dominio.com)";
        }
        return "Email válido";
    }

    /**
     * Obtiene un mensaje de error para validación de edad.
     *
     * @param fechaNacimiento fecha de nacimiento
     * @return mensaje de error
     */
    public static String mensajeErrorEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return "Debe especificar la fecha de nacimiento";
        }
        if (!validarEdadMinima(fechaNacimiento)) {
            return "El visitante debe ser mayor de " + EDAD_MINIMA_VISITANTE + " años";
        }
        return "Edad válida";
    }
}
