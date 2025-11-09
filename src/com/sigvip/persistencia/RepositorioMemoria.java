package com.sigvip.persistencia;

import com.sigvip.modelo.*;
import com.sigvip.modelo.enums.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Repositorio en memoria para modo offline.
 * Patrón Singleton - almacena todas las entidades en HashMaps durante la ejecución.
 *
 * <p><b>ADVERTENCIA</b>: Los datos se pierden al cerrar la aplicación.
 * Solo para demostración sin conexión a MySQL.
 *
 * @author Arnaboldi, Juan Ignacio
 * @version 1.0
 */
public class RepositorioMemoria {

    private static RepositorioMemoria instancia;

    // Almacenamiento en memoria (thread-safe)
    private final Map<Long, Visitante> visitantes;
    private final Map<Long, Interno> internos;
    private final Map<Long, Visita> visitas;
    private final Map<Long, Autorizacion> autorizaciones;
    private final Map<Long, Usuario> usuarios;
    private final Map<Long, Restriccion> restricciones;
    private final Map<Long, Establecimiento> establecimientos;
    private final Map<Long, ReporteGenerado> reportes;

    // Contadores para IDs auto-incrementales
    private final AtomicLong contadorVisitantes;
    private final AtomicLong contadorInternos;
    private final AtomicLong contadorVisitas;
    private final AtomicLong contadorAutorizaciones;
    private final AtomicLong contadorUsuarios;
    private final AtomicLong contadorRestricciones;
    private final AtomicLong contadorEstablecimientos;
    private final AtomicLong contadorReportes;

    private boolean datosInicializados;

    /**
     * Constructor privado para patrón Singleton.
     */
    private RepositorioMemoria() {
        this.visitantes = new ConcurrentHashMap<>();
        this.internos = new ConcurrentHashMap<>();
        this.visitas = new ConcurrentHashMap<>();
        this.autorizaciones = new ConcurrentHashMap<>();
        this.usuarios = new ConcurrentHashMap<>();
        this.restricciones = new ConcurrentHashMap<>();
        this.establecimientos = new ConcurrentHashMap<>();
        this.reportes = new ConcurrentHashMap<>();

        this.contadorVisitantes = new AtomicLong(1);
        this.contadorInternos = new AtomicLong(1);
        this.contadorVisitas = new AtomicLong(1);
        this.contadorAutorizaciones = new AtomicLong(1);
        this.contadorUsuarios = new AtomicLong(1);
        this.contadorRestricciones = new AtomicLong(1);
        this.contadorEstablecimientos = new AtomicLong(1);
        this.contadorReportes = new AtomicLong(1);

        this.datosInicializados = false;
    }

    /**
     * Obtiene la instancia única del repositorio.
     *
     * @return instancia singleton
     */
    public static synchronized RepositorioMemoria getInstancia() {
        if (instancia == null) {
            instancia = new RepositorioMemoria();
        }
        return instancia;
    }

    // ===== MÉTODOS DE INICIALIZACIÓN =====

    /**
     * Verifica si los datos de prueba han sido inicializados.
     * Si no han sido inicializados, los carga automáticamente.
     */
    private void verificarInicializacion() {
        if (!datosInicializados) {
            inicializarDatosPrueba();
        }
    }

    /**
     * Carga datos coherentes para modo offline basados en datos_de_prueba.sql.
     * Solo se ejecuta una vez.
     */
    public void inicializarDatosPrueba() {
        if (datosInicializados) {
            return;
        }

        // 1. Crear establecimiento: Unidad Nº1 - Lisandro Olmos
        crearEstablecimientoLisandroOlmos();

        // 2. Crear usuarios del sistema
        crearUsuariosPrueba();

        // 3. Crear visitantes de prueba
        crearVisitantesPrueba();

        // 4. Crear internos de prueba
        crearInternosPrueba();

        datosInicializados = true;
    }

    /**
     * Crea el establecimiento Unidad Nº1 - Lisandro Olmos.
     */
    private void crearEstablecimientoLisandroOlmos() {
        Establecimiento est = new Establecimiento("Unidad Nº1 - Lisandro Olmos", ModalidadVisita.MIXTA);
        est.setIdEstablecimiento(contadorEstablecimientos.getAndIncrement());
        est.setDiasHabilita("LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO");

        // Horarios de visita: 07:00 a 16:00
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 7);
        cal.set(Calendar.MINUTE, 0);
        est.setHorarioInicio(cal.getTime());

        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 0);
        est.setHorarioFin(cal.getTime());

        establecimientos.put(est.getIdEstablecimiento(), est);
    }

    /**
     * Crea 3 usuarios de prueba con diferentes roles.
     */
    private void crearUsuariosPrueba() {
        // Obtener el establecimiento Lisandro Olmos
        Establecimiento establecimiento = new ArrayList<>(establecimientos.values()).get(0);

        // Usuario 1: ADMINISTRADOR
        Usuario admin = new Usuario("admin", "Juan Pérez", Rol.ADMINISTRADOR);
        admin.setIdUsuario(contadorUsuarios.getAndIncrement());
        admin.setContrasena(Usuario.hashearContrasena("Admin123!"));
        admin.setEstablecimiento(establecimiento);
        usuarios.put(admin.getIdUsuario(), admin);

        // Usuario 2: OPERADOR
        Usuario oper = new Usuario("operador1", "María González", Rol.OPERADOR);
        oper.setIdUsuario(contadorUsuarios.getAndIncrement());
        oper.setContrasena(Usuario.hashearContrasena("Opera123!"));
        oper.setEstablecimiento(establecimiento);
        usuarios.put(oper.getIdUsuario(), oper);

        // Usuario 3: SUPERVISOR
        Usuario supervisor = new Usuario("supervisor1", "Carlos Rodríguez", Rol.SUPERVISOR);
        supervisor.setIdUsuario(contadorUsuarios.getAndIncrement());
        supervisor.setContrasena(Usuario.hashearContrasena("Super123!"));
        supervisor.setEstablecimiento(establecimiento);
        usuarios.put(supervisor.getIdUsuario(), supervisor);
    }

    /**
     * Crea 10 visitantes de prueba coherentes con datos_de_prueba.sql.
     */
    private void crearVisitantesPrueba() {
        Calendar cal = Calendar.getInstance();

        // 1. González, Ana María - DNI: 20345678
        cal.set(1985, Calendar.MARCH, 15);
        Visitante v1 = new Visitante("20345678", "González", "Ana María", cal.getTime());
        v1.setIdVisitante(contadorVisitantes.getAndIncrement());
        v1.setDomicilio("Calle 5 N° 1234, Lisandro Olmos");
        v1.setTelefono("221-456-7890");
        visitantes.put(v1.getIdVisitante(), v1);

        // 2. Fernández, Roberto Carlos - DNI: 21456789
        cal.set(1980, Calendar.JULY, 22);
        Visitante v2 = new Visitante("21456789", "Fernández", "Roberto Carlos", cal.getTime());
        v2.setIdVisitante(contadorVisitantes.getAndIncrement());
        v2.setDomicilio("Av. Centenario N° 567, Lisandro Olmos");
        v2.setTelefono("221-567-8901");
        visitantes.put(v2.getIdVisitante(), v2);

        // 3. López, Laura Beatriz - DNI: 22567890
        cal.set(1992, Calendar.NOVEMBER, 8);
        Visitante v3 = new Visitante("22567890", "López", "Laura Beatriz", cal.getTime());
        v3.setIdVisitante(contadorVisitantes.getAndIncrement());
        v3.setDomicilio("Calle 12 N° 890, Lisandro Olmos");
        v3.setTelefono("221-678-9012");
        visitantes.put(v3.getIdVisitante(), v3);

        // 4. Martínez, Jorge Luis - DNI: 23678901
        cal.set(1988, Calendar.MAY, 30);
        Visitante v4 = new Visitante("23678901", "Martínez", "Jorge Luis", cal.getTime());
        v4.setIdVisitante(contadorVisitantes.getAndIncrement());
        v4.setDomicilio("Diagonal 73 N° 345, Lisandro Olmos");
        v4.setTelefono("221-789-0123");
        visitantes.put(v4.getIdVisitante(), v4);

        // 5. Pérez, Mónica Susana - DNI: 24789012
        cal.set(1990, Calendar.SEPTEMBER, 18);
        Visitante v5 = new Visitante("24789012", "Pérez", "Mónica Susana", cal.getTime());
        v5.setIdVisitante(contadorVisitantes.getAndIncrement());
        v5.setDomicilio("Calle 8 N° 678, Lisandro Olmos");
        v5.setTelefono("221-890-1234");
        visitantes.put(v5.getIdVisitante(), v5);

        // 6. Rodríguez, Carlos Alberto - DNI: 25890123
        cal.set(1975, Calendar.DECEMBER, 25);
        Visitante v6 = new Visitante("25890123", "Rodríguez", "Carlos Alberto", cal.getTime());
        v6.setIdVisitante(contadorVisitantes.getAndIncrement());
        v6.setDomicilio("Av. 44 N° 123, Gonnet");
        v6.setTelefono("221-901-2345");
        visitantes.put(v6.getIdVisitante(), v6);

        // 7. Sánchez, María del Carmen - DNI: 26901234
        cal.set(1983, Calendar.APRIL, 10);
        Visitante v7 = new Visitante("26901234", "Sánchez", "María del Carmen", cal.getTime());
        v7.setIdVisitante(contadorVisitantes.getAndIncrement());
        v7.setDomicilio("Calle 15 N° 456, Lisandro Olmos");
        v7.setTelefono("221-012-3456");
        visitantes.put(v7.getIdVisitante(), v7);

        // 8. Romero, Diego Martín - DNI: 27012345
        cal.set(1995, Calendar.AUGUST, 14);
        Visitante v8 = new Visitante("27012345", "Romero", "Diego Martín", cal.getTime());
        v8.setIdVisitante(contadorVisitantes.getAndIncrement());
        v8.setDomicilio("Calle 3 N° 789, Lisandro Olmos");
        v8.setTelefono("221-123-4567");
        visitantes.put(v8.getIdVisitante(), v8);

        // 9. Gómez, Silvana Andrea - DNI: 28123456
        cal.set(1987, Calendar.JUNE, 20);
        Visitante v9 = new Visitante("28123456", "Gómez", "Silvana Andrea", cal.getTime());
        v9.setIdVisitante(contadorVisitantes.getAndIncrement());
        v9.setDomicilio("Av. 32 N° 234, Ringuelet");
        v9.setTelefono("221-234-5678");
        visitantes.put(v9.getIdVisitante(), v9);

        // 10. Díaz, Juan Pedro - DNI: 29234567
        cal.set(1991, Calendar.FEBRUARY, 28);
        Visitante v10 = new Visitante("29234567", "Díaz", "Juan Pedro", cal.getTime());
        v10.setIdVisitante(contadorVisitantes.getAndIncrement());
        v10.setDomicilio("Calle 18 N° 567, Lisandro Olmos");
        v10.setTelefono("221-345-6789");
        visitantes.put(v10.getIdVisitante(), v10);
    }

    /**
     * Crea una muestra representativa de internos coherentes con datos_de_prueba.sql.
     * Para modo offline, creamos 10 internos representativos de los 100 totales.
     */
    private void crearInternosPrueba() {
        // Obtener el establecimiento Lisandro Olmos
        Establecimiento establecimiento = new ArrayList<>(establecimientos.values()).get(0);
        Calendar cal = Calendar.getInstance();

        // 1. García, Roberto Carlos - LEG-2024-001 - Pabellón A, Piso 1, CONDENADO
        cal.set(2023, Calendar.JANUARY, 15);
        Interno i1 = new Interno("LEG-2024-001", "García", "Roberto Carlos", "23456789");
        i1.setIdInterno(contadorInternos.getAndIncrement());
        i1.setEstablecimiento(establecimiento);
        i1.setPabellonActual("A");
        i1.setPisoActual(1);
        i1.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i1.setFechaIngreso(cal.getTime());
        internos.put(i1.getIdInterno(), i1);

        // 2. Rodríguez, José Luis - LEG-2024-002 - Pabellón A, Piso 1, PROCESADO
        cal.set(2023, Calendar.FEBRUARY, 20);
        Interno i2 = new Interno("LEG-2024-002", "Rodríguez", "José Luis", "24567890");
        i2.setIdInterno(contadorInternos.getAndIncrement());
        i2.setEstablecimiento(establecimiento);
        i2.setPabellonActual("A");
        i2.setPisoActual(1);
        i2.setSituacionProcesal(SituacionProcesal.PROCESADO);
        i2.setFechaIngreso(cal.getTime());
        internos.put(i2.getIdInterno(), i2);

        // 3. Fernández, Miguel Angel - LEG-2024-003 - Pabellón A, Piso 1, CONDENADO
        cal.set(2023, Calendar.MARCH, 10);
        Interno i3 = new Interno("LEG-2024-003", "Fernandez", "Miguel Angel", "25678901");
        i3.setIdInterno(contadorInternos.getAndIncrement());
        i3.setEstablecimiento(establecimiento);
        i3.setPabellonActual("A");
        i3.setPisoActual(1);
        i3.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i3.setFechaIngreso(cal.getTime());
        internos.put(i3.getIdInterno(), i3);

        // 4. López, Juan Carlos - LEG-2024-004 - Pabellón A, Piso 1, CONDENADO
        cal.set(2023, Calendar.APRIL, 5);
        Interno i4 = new Interno("LEG-2024-004", "López", "Juan Carlos", "26789012");
        i4.setIdInterno(contadorInternos.getAndIncrement());
        i4.setEstablecimiento(establecimiento);
        i4.setPabellonActual("A");
        i4.setPisoActual(1);
        i4.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i4.setFechaIngreso(cal.getTime());
        internos.put(i4.getIdInterno(), i4);

        // 5. Martínez, Diego Alberto - LEG-2024-005 - Pabellón A, Piso 1, CONDENADO
        cal.set(2023, Calendar.MAY, 12);
        Interno i5 = new Interno("LEG-2024-005", "Martínez", "Diego Alberto", "27890123");
        i5.setIdInterno(contadorInternos.getAndIncrement());
        i5.setEstablecimiento(establecimiento);
        i5.setPabellonActual("A");
        i5.setPisoActual(1);
        i5.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i5.setFechaIngreso(cal.getTime());
        internos.put(i5.getIdInterno(), i5);

        // 6. Cruz, Carlos Alberto - LEG-2024-021 - Pabellón B, Piso 1, CONDENADO
        cal.set(2024, Calendar.JANUARY, 5);
        Interno i6 = new Interno("LEG-2024-021", "Cruz", "Carlos Alberto", "43456789");
        i6.setIdInterno(contadorInternos.getAndIncrement());
        i6.setEstablecimiento(establecimiento);
        i6.setPabellonActual("B");
        i6.setPisoActual(1);
        i6.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i6.setFechaIngreso(cal.getTime());
        internos.put(i6.getIdInterno(), i6);

        // 7. Flores, José María - LEG-2024-022 - Pabellón B, Piso 1, PROCESADO
        cal.set(2024, Calendar.FEBRUARY, 12);
        Interno i7 = new Interno("LEG-2024-022", "Flores", "José María", "44567890");
        i7.setIdInterno(contadorInternos.getAndIncrement());
        i7.setEstablecimiento(establecimiento);
        i7.setPabellonActual("B");
        i7.setPisoActual(1);
        i7.setSituacionProcesal(SituacionProcesal.PROCESADO);
        i7.setFechaIngreso(cal.getTime());
        internos.put(i7.getIdInterno(), i7);

        // 8. Reyes, Roberto Luis - LEG-2024-023 - Pabellón B, Piso 1, CONDENADO
        cal.set(2024, Calendar.MARCH, 8);
        Interno i8 = new Interno("LEG-2024-023", "Reyes", "Roberto Luis", "45678901");
        i8.setIdInterno(contadorInternos.getAndIncrement());
        i8.setEstablecimiento(establecimiento);
        i8.setPabellonActual("B");
        i8.setPisoActual(1);
        i8.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i8.setFechaIngreso(cal.getTime());
        internos.put(i8.getIdInterno(), i8);

        // 9. Jiménez, Juan Carlos - LEG-2024-024 - Pabellón B, Piso 1, CONDENADO
        cal.set(2024, Calendar.APRIL, 2);
        Interno i9 = new Interno("LEG-2024-024", "Jiménez", "Juan Carlos", "46789012");
        i9.setIdInterno(contadorInternos.getAndIncrement());
        i9.setEstablecimiento(establecimiento);
        i9.setPabellonActual("B");
        i9.setPisoActual(1);
        i9.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i9.setFechaIngreso(cal.getTime());
        internos.put(i9.getIdInterno(), i9);

        // 10. Alvarez, Miguel Angel - LEG-2024-025 - Pabellón B, Piso 1, CONDENADO
        cal.set(2024, Calendar.APRIL, 25);
        Interno i10 = new Interno("LEG-2024-025", "Alvarez", "Miguel Angel", "47890123");
        i10.setIdInterno(contadorInternos.getAndIncrement());
        i10.setEstablecimiento(establecimiento);
        i10.setPabellonActual("B");
        i10.setPisoActual(1);
        i10.setSituacionProcesal(SituacionProcesal.CONDENADO);
        i10.setFechaIngreso(cal.getTime());
        internos.put(i10.getIdInterno(), i10);
    }

    // En modo offline no creamos autorizaciones pre-cargadas
    // Los usuarios deben crearlas desde cero a través de la interfaz

    /**
     * Limpia todos los datos en memoria.
     * Útil para reiniciar estado en pruebas.
     */
    public void limpiar() {
        visitantes.clear();
        internos.clear();
        visitas.clear();
        autorizaciones.clear();
        usuarios.clear();
        restricciones.clear();
        establecimientos.clear();
        reportes.clear();

        contadorVisitantes.set(1);
        contadorInternos.set(1);
        contadorVisitas.set(1);
        contadorAutorizaciones.set(1);
        contadorUsuarios.set(1);
        contadorRestricciones.set(1);
        contadorEstablecimientos.set(1);
        contadorReportes.set(1);

        datosInicializados = false;
    }

    // ===== MÉTODOS CRUD PARA VISITANTES =====

    public Long insertarVisitante(Visitante v) {
        Long id = contadorVisitantes.getAndIncrement();
        v.setIdVisitante(id);
        visitantes.put(id, v);
        return id;
    }

    public Visitante buscarVisitantePorId(Long id) {
        verificarInicializacion();
        return visitantes.get(id);
    }

    public Visitante buscarVisitantePorDNI(String dni) {
        verificarInicializacion();
        return visitantes.values().stream()
                .filter(v -> v.getDni().equals(dni))
                .findFirst()
                .orElse(null);
    }

    public List<Visitante> listarVisitantes() {
        verificarInicializacion();
        return new ArrayList<>(visitantes.values());
    }

    public boolean actualizarVisitante(Visitante v) {
        if (v.getIdVisitante() != null && visitantes.containsKey(v.getIdVisitante())) {
            visitantes.put(v.getIdVisitante(), v);
            return true;
        }
        return false;
    }

    public boolean eliminarVisitante(Long id) {
        return visitantes.remove(id) != null;
    }

    // ===== MÉTODOS CRUD PARA INTERNOS =====

    public Long insertarInterno(Interno i) {
        Long id = contadorInternos.getAndIncrement();
        i.setIdInterno(id);
        internos.put(id, i);
        return id;
    }

    public Interno buscarInternoPorId(Long id) {
        verificarInicializacion();
        return internos.get(id);
    }

    public Interno buscarInternoPorLegajo(String legajo) {
        verificarInicializacion();
        return internos.values().stream()
                .filter(i -> i.getNumeroLegajo().equals(legajo))
                .findFirst()
                .orElse(null);
    }

    public List<Interno> listarInternos() {
        verificarInicializacion();
        return new ArrayList<>(internos.values());
    }

    public boolean actualizarInterno(Interno i) {
        if (i.getIdInterno() != null && internos.containsKey(i.getIdInterno())) {
            internos.put(i.getIdInterno(), i);
            return true;
        }
        return false;
    }

    public boolean eliminarInterno(Long id) {
        return internos.remove(id) != null;
    }

    // ===== MÉTODOS CRUD PARA VISITAS =====

    public Long insertarVisita(Visita v) {
        Long id = contadorVisitas.getAndIncrement();
        v.setIdVisita(id);
        visitas.put(id, v);
        return id;
    }

    public Visita buscarVisitaPorId(Long id) {
        return visitas.get(id);
    }

    public List<Visita> listarVisitas() {
        return new ArrayList<>(visitas.values());
    }

    public List<Visita> listarVisitasEnCurso() {
        verificarInicializacion();
        return visitas.values().stream()
                .filter(v -> v.getEstadoVisita() == EstadoVisita.EN_CURSO)
                .collect(Collectors.toList());
    }

    public int contarVisitasEnCurso() {
        verificarInicializacion();
        long count = visitas.values().stream()
                .filter(v -> v.getEstadoVisita() == EstadoVisita.EN_CURSO)
                .count();
        return (int) count;
    }

    public List<Visita> buscarVisitasPorVisitante(Long idVisitante) {
        return visitas.values().stream()
                .filter(v -> v.getVisitante() != null && v.getVisitante().getIdVisitante().equals(idVisitante))
                .collect(Collectors.toList());
    }

    public List<Visita> buscarVisitasPorInterno(Long idInterno) {
        return visitas.values().stream()
                .filter(v -> v.getInterno() != null && v.getInterno().getIdInterno().equals(idInterno))
                .collect(Collectors.toList());
    }

    public boolean actualizarVisita(Visita v) {
        verificarInicializacion();
        if (v.getIdVisita() != null && visitas.containsKey(v.getIdVisita())) {
            visitas.put(v.getIdVisita(), v);
            return true;
        }
        return false;
    }

    // ===== MÉTODOS CRUD PARA AUTORIZACIONES =====

    public Long insertarAutorizacion(Autorizacion a) {
        Long id = contadorAutorizaciones.getAndIncrement();
        a.setIdAutorizacion(id);
        autorizaciones.put(id, a);
        return id;
    }

    public Autorizacion buscarAutorizacionPorId(Long id) {
        return autorizaciones.get(id);
    }

    public Autorizacion buscarAutorizacionVigente(Long idVisitante, Long idInterno) {
        verificarInicializacion();

        return autorizaciones.values().stream()
                .filter(a -> a.getVisitante() != null && a.getVisitante().getIdVisitante().equals(idVisitante))
                .filter(a -> a.getInterno() != null && a.getInterno().getIdInterno().equals(idInterno))
                .filter(a -> a.getEstado() == EstadoAutorizacion.VIGENTE)
                .filter(a -> a.getFechaVencimiento() == null || a.getFechaVencimiento().after(new Date()))
                .findFirst()
                .orElse(null);
    }

    public List<Autorizacion> listarAutorizaciones() {
        verificarInicializacion();
        return new ArrayList<>(autorizaciones.values());
    }

    public List<Autorizacion> listarAutorizacionesPorVisitante(Long idVisitante) {
        return autorizaciones.values().stream()
                .filter(a -> a.getVisitante() != null && a.getVisitante().getIdVisitante().equals(idVisitante))
                .collect(Collectors.toList());
    }

    public boolean actualizarAutorizacion(Autorizacion a) {
        if (a.getIdAutorizacion() != null && autorizaciones.containsKey(a.getIdAutorizacion())) {
            autorizaciones.put(a.getIdAutorizacion(), a);
            return true;
        }
        return false;
    }

    public boolean eliminarAutorizacion(Long id) {
        return autorizaciones.remove(id) != null;
    }

    // ===== MÉTODOS CRUD PARA USUARIOS =====

    public Long insertarUsuario(Usuario u) {
        Long id = contadorUsuarios.getAndIncrement();
        u.setIdUsuario(id);
        usuarios.put(id, u);
        return id;
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarios.get(id);
    }

    public Usuario buscarUsuarioPorNombre(String nombreUsuario) {
        return usuarios.values().stream()
                .filter(u -> u.getNombreUsuario().equals(nombreUsuario))
                .findFirst()
                .orElse(null);
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    public boolean actualizarUsuario(Usuario u) {
        if (u.getIdUsuario() != null && usuarios.containsKey(u.getIdUsuario())) {
            usuarios.put(u.getIdUsuario(), u);
            return true;
        }
        return false;
    }

    public boolean eliminarUsuario(Long id) {
        return usuarios.remove(id) != null;
    }

    // ===== MÉTODOS CRUD PARA RESTRICCIONES =====

    public Long insertarRestriccion(Restriccion r) {
        Long id = contadorRestricciones.getAndIncrement();
        r.setIdRestriccion(id);
        restricciones.put(id, r);
        return id;
    }

    public Restriccion buscarRestriccionPorId(Long id) {
        return restricciones.get(id);
    }

    public List<Restriccion> listarRestriccionesAplicables(Long idVisitante, Long idInterno) {
        verificarInicializacion();

        Date hoy = new Date();

        return restricciones.values().stream()
                .filter(r -> r.getVisitante() != null && r.getVisitante().getIdVisitante().equals(idVisitante))
                .filter(r -> r.isActivo())
                .filter(r -> r.getFechaInicio() != null && !r.getFechaInicio().after(hoy))
                .filter(r -> r.getFechaFin() == null || !r.getFechaFin().before(hoy))
                .filter(r -> r.getAplicableA() == com.sigvip.modelo.enums.AplicableA.TODOS ||
                           (r.getAplicableA() == com.sigvip.modelo.enums.AplicableA.INTERNO_ESPECIFICO &&
                            r.getInterno() != null && r.getInterno().getIdInterno().equals(idInterno)))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<Restriccion> listarRestricciones() {
        return new ArrayList<>(restricciones.values());
    }

    public List<Restriccion> listarRestriccionesActivas(Long idVisitante) {
        Date hoy = new Date();
        return restricciones.values().stream()
                .filter(r -> r.isActivo())
                .filter(r -> r.getFechaInicio().before(hoy) || r.getFechaInicio().equals(hoy))
                .filter(r -> r.getFechaFin() == null || r.getFechaFin().after(hoy))
                .filter(r -> r.getVisitante() == null || r.getVisitante().getIdVisitante().equals(idVisitante))
                .collect(Collectors.toList());
    }

    public boolean actualizarRestriccion(Restriccion r) {
        if (r.getIdRestriccion() != null && restricciones.containsKey(r.getIdRestriccion())) {
            restricciones.put(r.getIdRestriccion(), r);
            return true;
        }
        return false;
    }

    public boolean eliminarRestriccion(Long id) {
        return restricciones.remove(id) != null;
    }

    /**
     * Lista todas las restricciones activas (sin filtro por visitante).
     * Método para obtenerActivas() del DAO.
     *
     * @return lista de restricciones activas
     */
    public List<Restriccion> listarRestriccionesActivas() {
        Date hoy = new Date();
        return restricciones.values().stream()
                .filter(r -> r.isActivo())
                .filter(r -> r.getFechaInicio() != null && !r.getFechaInicio().after(hoy))
                .filter(r -> r.getFechaFin() == null || !r.getFechaFin().before(hoy))
                .collect(Collectors.toList());
    }

    /**
     * Busca todas las restricciones de un visitante (activas e inactivas).
     * Método para buscarPorVisitante() del DAO.
     *
     * @param idVisitante ID del visitante
     * @return lista de restricciones
     */
    public List<Restriccion> buscarRestriccionesPorVisitante(Long idVisitante) {
        return restricciones.values().stream()
                .filter(r -> r.getVisitante() != null && r.getVisitante().getIdVisitante().equals(idVisitante))
                .sorted((r1, r2) -> {
                    if (r1.getFechaInicio() == null) return 1;
                    if (r2.getFechaInicio() == null) return -1;
                    return r2.getFechaInicio().compareTo(r1.getFechaInicio());
                })
                .collect(Collectors.toList());
    }

    /**
     * Busca restricciones por tipo.
     * Método para buscarPorTipo() del DAO.
     *
     * @param tipo tipo de restricción
     * @return lista de restricciones de ese tipo
     */
    public List<Restriccion> buscarRestriccionesPorTipo(TipoRestriccion tipo) {
        return restricciones.values().stream()
                .filter(r -> r.getTipoRestriccion() == tipo)
                .sorted((r1, r2) -> {
                    if (r1.getFechaInicio() == null) return 1;
                    if (r2.getFechaInicio() == null) return -1;
                    return r2.getFechaInicio().compareTo(r1.getFechaInicio());
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene restricciones que vencen en un rango de fechas.
     * Método para obtenerProximasAVencer() del DAO.
     *
     * @param fechaInicio fecha de inicio del rango
     * @param fechaFin fecha de fin del rango
     * @return lista de restricciones próximas a vencer
     */
    public List<Restriccion> obtenerRestriccionesProximasAVencer(java.util.Date fechaInicio,
                                                                  java.util.Date fechaFin) {
        return restricciones.values().stream()
                .filter(r -> r.isActivo())
                .filter(r -> r.getFechaFin() != null)
                .filter(r -> !r.getFechaFin().before(fechaInicio) && !r.getFechaFin().after(fechaFin))
                .sorted((r1, r2) -> r1.getFechaFin().compareTo(r2.getFechaFin()))
                .collect(Collectors.toList());
    }

    /**
     * Cuenta restricciones activas.
     * Método para contarActivas() del DAO.
     *
     * @return número de restricciones activas
     */
    public int contarRestriccionesActivas() {
        Date hoy = new Date();
        return (int) restricciones.values().stream()
                .filter(r -> r.isActivo())
                .filter(r -> r.getFechaInicio() != null && !r.getFechaInicio().after(hoy))
                .filter(r -> r.getFechaFin() == null || !r.getFechaFin().before(hoy))
                .count();
    }

    // ===== MÉTODOS CRUD PARA ESTABLECIMIENTOS =====

    public Long insertarEstablecimiento(Establecimiento e) {
        Long id = contadorEstablecimientos.getAndIncrement();
        e.setIdEstablecimiento(id);
        establecimientos.put(id, e);
        return id;
    }

    public Establecimiento buscarEstablecimientoPorId(Long id) {
        verificarInicializacion();
        return establecimientos.get(id);
    }

    public List<Establecimiento> listarEstablecimientos() {
        return new ArrayList<>(establecimientos.values());
    }

    public boolean actualizarEstablecimiento(Establecimiento e) {
        if (e.getIdEstablecimiento() != null && establecimientos.containsKey(e.getIdEstablecimiento())) {
            establecimientos.put(e.getIdEstablecimiento(), e);
            return true;
        }
        return false;
    }

    // ===== MÉTODOS CRUD PARA REPORTES =====

    public Long insertarReporte(ReporteGenerado r) {
        Long id = contadorReportes.getAndIncrement();
        r.setIdReporte(id);
        reportes.put(id, r);
        return id;
    }

    public ReporteGenerado buscarReportePorId(Long id) {
        return reportes.get(id);
    }

    public List<ReporteGenerado> listarReportes() {
        return new ArrayList<>(reportes.values());
    }
}
