-- ============================================
-- CONSULTAS SQL OPERATIVAS
-- ============================================

-- 1. Buscar visitante por DNI con autorizaciones vigentes
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
WHERE v.dni = '34567890'
GROUP BY v.id_visitante;

-- 2. Listar autorizaciones vigentes de un visitante específico
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
WHERE a.id_visitante = 1
    AND a.estado = 'VIGENTE'
    AND (a.fecha_vencimiento IS NULL OR a.fecha_vencimiento >= CURDATE());

-- 3. Verificar restricciones activas antes de autorizar ingreso
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
WHERE r.id_visitante = 2
    AND r.activa = TRUE
    AND r.fecha_inicio <= CURDATE()
    AND (r.fecha_fin IS NULL OR r.fecha_fin >= CURDATE());

-- 4. Registrar ingreso de visita
INSERT INTO visitas (
    id_visitante,
    id_interno,
    fecha_visita,
    hora_ingreso,
    estado_visita,
    id_operador_ingreso
) VALUES (
    1,  -- id_visitante
    1,  -- id_interno
    CURDATE(),
    CURTIME(),
    'EN_CURSO',
    2   -- id_operador
);

-- 5. Registrar egreso de visita
UPDATE visitas
SET
    hora_egreso = CURTIME(),
    estado_visita = 'FINALIZADA',
    id_operador_egreso = 2
WHERE id_visita = 10
    AND estado_visita = 'EN_CURSO';

-- 6. Historial completo de visitas de un visitante
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
WHERE v.id_visitante = 1
ORDER BY v.fecha_visita DESC, v.hora_ingreso DESC;

-- 7. Visitas activas en este momento
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