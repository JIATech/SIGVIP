-- ============================================
-- CONSULTAS SQL OPERATIVAS ACTUALIZADAS
-- Coherentes con los nuevos datos de prueba de Lisandro Olmos
-- ============================================

-- 1. Buscar visitante por DNI con autorizaciones vigentes
-- Nota: No hay autorizaciones en los datos de prueba iniciales
SELECT
    v.dni,
    v.apellido,
    v.nombre,
    v.estado,
    COUNT(a.id_autorizacion) as total_autorizaciones
FROM visitantes v
LEFT JOIN autorizaciones a ON v.id_visitante = a.id_visitante
    AND a.estado = 'VIGENTE'
    AND (a.fecha_vencimiento IS NULL OR a.fecha_vencimiento >= CURDATE())
WHERE v.dni = '20345678'  -- DNI del primer visitante de prueba
GROUP BY v.id_visitante;

-- 2. Listar autorizaciones vigentes de un visitante específico
-- Nota: Devuelve vacío porque no hay autorizaciones pre-cargadas
SELECT
    a.id_autorizacion,
    i.numero_legajo,
    CONCAT(i.apellido, ', ', i.nombre) as interno,
    i.pabellon_actual,
    i.piso_actual,
    a.tipo_relacion,
    a.fecha_autorizacion,
    a.fecha_vencimiento,
    a.estado
FROM autorizaciones a
INNER JOIN internos i ON a.id_interno = i.id_interno
WHERE a.id_visitante = 1  -- Primer visitante (González, Ana María)
    AND a.estado = 'VIGENTE'
    AND (a.fecha_vencimiento IS NULL OR a.fecha_vencimiento >= CURDATE());

-- 3. Verificar restricciones activas antes de autorizar ingreso
-- Nota: No hay restricciones en los datos iniciales
SELECT
    r.id_restriccion,
    r.tipo_restriccion,
    r.motivo,
    r.fecha_inicio,
    r.fecha_fin,
    r.aplicable_a,
    CASE
        WHEN r.aplicable_a = 'INTERNO_ESPECIFICO'
        THEN CONCAT(i.apellido, ', ', i.nombre)
        ELSE 'TODOS LOS INTERNOS'
    END as alcance
FROM restricciones r
LEFT JOIN internos i ON r.id_interno = i.id_interno
WHERE r.id_visitante = 1  -- Primer visitante
    AND r.activa = TRUE
    AND r.fecha_inicio <= CURDATE()
    AND (r.fecha_fin IS NULL OR r.fecha_fin >= CURDATE());

-- 4. Registrar ingreso de visita (ejemplo para crear una visita)
-- Nota: Debe existir una autorización vigente primero
INSERT INTO visitas (
    id_visitante,
    id_interno,
    fecha_visita,
    hora_ingreso,
    estado_visita,
    id_operador_ingreso
) VALUES (
    1,  -- id_visitante (González, Ana María)
    1,  -- id_interno (García, Roberto Carlos - LEG-2024-001)
    CURDATE(),
    CURTIME(),
    'EN_CURSO',
    2   -- id_operador (operador1)
);

-- 5. Registrar egreso de visita
UPDATE visitas
SET
    hora_egreso = CURTIME(),
    estado_visita = 'FINALIZADA',
    id_operador_egreso = 2
WHERE id_visita = 1  -- Ajustar al ID de visita creado
    AND estado_visita = 'EN_CURSO';

-- 6. Historial completo de visitas de un visitante
-- Nota: Devuelve vacío porque no hay visitas pre-cargadas
SELECT
    v.fecha_visita,
    v.hora_ingreso,
    v.hora_egreso,
    CONCAT(i.apellido, ', ', i.nombre) as interno_visitado,
    i.pabellon_actual,
    v.estado_visita,
    TIMEDIFF(v.hora_egreso, v.hora_ingreso) as duracion,
    CONCAT(u1.nombre_completo) as operador_ingreso
FROM visitas v
INNER JOIN internos i ON v.id_interno = i.id_interno
INNER JOIN usuarios u1 ON v.id_operador_ingreso = u1.id_usuario
WHERE v.id_visitante = 1  -- Primer visitante (González, Ana María)
ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC;

-- 7. Visitas activas en este momento
-- Nota: Devuelve vacío porque no hay visitas en curso
SELECT
    v.id_visita,
    CONCAT(vis.apellido, ', ', vis.nombre) as visitante,
    vis.dni,
    CONCAT(i.apellido, ', ', i.nombre) as interno,
    i.pabellon_actual,
    i.piso_actual,
    v.hora_ingreso,
    TIMEDIFF(CURTIME(), v.hora_ingreso) as tiempo_transcurrido
FROM visitas v
INNER JOIN visitantes vis ON v.id_visitante = vis.id_visitante
INNER JOIN internos i ON v.id_interno = i.id_interno
WHERE v.estado_visita = 'EN_CURSO'
    AND v.fecha_visita = CURDATE()
ORDER BY v.hora_ingreso;

-- 8. Estadísticas mensuales de visitas por interno
-- Nota: Devuelve ceros porque no hay visitas registradas
SELECT
    CONCAT(i.apellido, ', ', i.nombre) as interno,
    i.numero_legajo,
    i.pabellon_actual,
    COUNT(v.id_visita) as total_visitas,
    COUNT(DISTINCT v.id_visitante) as visitantes_diferentes,
    AVG(TIMESTAMPDIFF(MINUTE, v.hora_ingreso, v.hora_egreso)) as duracion_promedio_minutos
FROM internos i
LEFT JOIN visitas v ON i.id_interno = v.id_interno
    AND v.fecha_visita >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
    AND v.estado_visita = 'FINALIZADA'
WHERE i.estado = 'ACTIVO'
GROUP BY i.id_interno
ORDER BY total_visitas DESC;

-- 9. Detectar patrones anómalos (visitantes con muchos internos)
-- Nota: Devuelve vacío porque no hay autorizaciones pre-cargadas
SELECT
    CONCAT(v.apellido, ', ', v.nombre) as visitante,
    v.dni,
    COUNT(DISTINCT a.id_interno) as cantidad_internos,
    GROUP_CONCAT(
        CONCAT(i.apellido, ', ', i.nombre)
        ORDER BY i.apellido
        SEPARATOR '; '
    ) as lista_internos
FROM visitantes v
INNER JOIN autorizaciones a ON v.id_visitante = a.id_visitante
INNER JOIN internos i ON a.id_interno = i.id_interno
WHERE a.estado = 'VIGENTE'
GROUP BY v.id_visitante
HAVING cantidad_internos > 2
ORDER BY cantidad_internos DESC;

-- 10. Auditoría de operaciones críticas del día
-- Nota: Puede mostrar operaciones de creación de los datos de prueba
SELECT
    a.timestamp_operacion,
    u.nombre_completo as usuario,
    a.tipo_operacion,
    a.tabla_afectada,
    a.id_registro,
    a.ip_origen
FROM auditoria a
INNER JOIN usuarios u ON a.id_usuario = u.id_usuario
WHERE DATE(a.timestamp_operacion) = CURDATE()
ORDER BY a.timestamp_operacion DESC;

-- ============================================
-- CONSULTAS ÚTILES PARA LOS NUEVOS DATOS
-- ============================================

-- 11. Listar todos los internos por pabellón
SELECT
    i.numero_legajo,
    CONCAT(i.apellido, ', ', i.nombre) as interno,
    i.dni,
    i.pabellon_actual,
    i.piso_actual,
    i.situacion_procesal,
    i.fecha_ingreso
FROM internos i
WHERE i.estado = 'ACTIVO'
ORDER BY i.pabellon_actual, i.piso_actual, i.apellido, i.nombre;

-- 12. Contar internos por pabellón y situación procesal
SELECT
    pabellon_actual,
    situacion_procesal,
    COUNT(*) as total_internos
FROM internos
WHERE estado = 'ACTIVO'
GROUP BY pabellon_actual, situacion_procesal
ORDER BY pabellon_actual, situacion_procesal;

-- 13. Listar todos los visitantes registrados
SELECT
    v.dni,
    CONCAT(v.apellido, ', ', v.nombre) as visitante,
    v.domicilio,
    v.telefono,
    DATE_FORMAT(v.fecha_nacimiento, '%d/%m/%Y') as fecha_nacimiento,
    TIMESTAMPDIFF(YEAR, v.fecha_nacimiento, CURDATE()) as edad,
    v.estado
FROM visitantes v
ORDER BY v.apellido, v.nombre;

-- 14. Buscar interno por legajo
SELECT
    i.numero_legajo,
    CONCAT(i.apellido, ', ', i.nombre) as interno,
    i.dni,
    i.pabellon_actual,
    i.piso_actual,
    i.situacion_procesal,
    i.fecha_ingreso,
    e.nombre as establecimiento
FROM internos i
INNER JOIN establecimientos e ON i.id_establecimiento = e.id_establecimiento
WHERE i.numero_legajo = 'LEG-2024-001';  -- Primer legajo de prueba

-- 15. Consultar horario del establecimiento
SELECT
    nombre,
    nombre_visita,
    modalidad_visita,
    dias_habilita,
    horario_inicio,
    horario_fin
FROM establecimientos
WHERE id_establecimiento = 1;  -- Unidad Lisandro Olmos

-- ============================================
-- EJEMPLOS PARA CREAR DATOS DE PRUEBA
-- ============================================

-- 16. Crear autorización de ejemplo
INSERT INTO autorizaciones (
    id_visitante,
    id_interno,
    tipo_relacion,
    descripcion_relacion,
    fecha_autorizacion,
    fecha_vencimiento,
    estado,
    id_autorizado_por
) VALUES (
    1,  -- González, Ana María
    1,  -- García, Roberto Carlos
    'MADRE',
    'Madre del interno',
    CURDATE(),
    NULL,
    'VIGENTE',
    1   -- admin
);

-- 17. Crear restricción de ejemplo
INSERT INTO restricciones (
    id_visitante,
    tipo_restriccion,
    motivo,
    fecha_inicio,
    fecha_fin,
    aplicable_a,
    activa,
    id_creado_por
) VALUES (
    2,  -- Fernández, Roberto Carlos
    'CONDUCTA',
    'Intento de ingreso de elementos no permitidos - Resolución 123/2024',
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 6 MONTH),
    'TODOS',
    TRUE,
    3   -- supervisor1
);

-- 18. Verificar disponibilidad de interno para visita
SELECT
    i.numero_legajo,
    CONCAT(i.apellido, ', ', i.nombre) as interno,
    i.pabellon_actual,
    i.piso_actual,
    CASE
        WHEN EXISTS (
            SELECT 1 FROM visitas v
            WHERE v.id_interno = i.id_interno
            AND v.fecha_visita = CURDATE()
            AND v.estado_visita = 'EN_CURSO'
        ) THEN 'OCUPADO'
        ELSE 'DISPONIBLE'
    END as estado_actual
FROM internos i
WHERE i.numero_legajo = 'LEG-2024-001';

-- 19. Validar horario de visita (ejemplo para hoy)
SELECT
    e.nombre,
    e.dias_habilita,
    e.horario_inicio,
    e.horario_fin,
    CASE
        WHEN FIND_IN_SET(DAYNAME(CURDATE()), e.dias_habilita) > 0
        THEN 'HABILITADO HOY'
        ELSE 'NO HABILITADO HOY'
    END as estado_dia,
    CASE
        WHEN CURTIME() BETWEEN e.horario_inicio AND e.horario_fin
        THEN 'DENTRO DE HORARIO'
        ELSE 'FUERA DE HORARIO'
    END as estado_horario
FROM establecimientos e
WHERE id_establecimiento = 1;

-- 20. Listar internos por situación procesal
SELECT
    situacion_procesal,
    COUNT(*) as total,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM internos WHERE estado = 'ACTIVO'), 2) as porcentaje
FROM internos
WHERE estado = 'ACTIVO'
GROUP BY situacion_procesal
ORDER BY total DESC;