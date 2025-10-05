-- ============================================
-- INSERCIÓN DE DATOS DE PRUEBA
-- ============================================

-- Establecimientos
INSERT INTO establecimientos (nombre, nombre_visita, modalidad_visita, dias_habilita, horario_inicio, horario_fin) VALUES
('Unidad Penitenciaria Nº 1 - La Plata', 'UP1 La Plata', 'SECTOR', 'MARTES,JUEVES,SABADO', '09:00', '16:00'),
('Complejo Penitenciario Federal I - Ezeiza', 'CPF I Ezeiza', 'PRESENCIAL', 'MIERCOLES,SABADO,DOMINGO', '08:00', '16:00'),
('Centro de Detención Judicial - Marcos Paz', 'CDJ Marcos Paz', 'MIXTA', 'MARTES,JUEVES,SABADO', '10:00', '15:00');

-- Usuarios del sistema
INSERT INTO usuarios (nombre_usuario, contrasena, nombre_completo, rol, id_establecimiento) VALUES
('admin', SHA2('Admin123!', 256), 'Juan Pérez', 'ADMINISTRADOR', 1),
('operador1', SHA2('Opera123!', 256), 'María González', 'OPERADOR', 1),
('supervisor1', SHA2('Super123!', 256), 'Carlos Rodríguez', 'SUPERVISOR', 1);

-- Visitantes
INSERT INTO visitantes (dni, apellido, nombre, domicilio, telefono, fecha_nacimiento, estado) VALUES
('34567890', 'García', 'Ana María', 'Calle 50 N° 123, La Plata', '221-456-7890', '1988-05-15', 'ACTIVO'),
('28345678', 'Fernández', 'Roberto Carlos', 'Av. 7 N° 456, La Plata', '221-789-0123', '1980-11-23', 'ACTIVO'),
('40123456', 'López', 'Laura Beatriz', 'Calle 10 N° 789, Berisso', '221-234-5678', '1995-03-08', 'ACTIVO'),
('35678901', 'Martínez', 'Jorge Luis', 'Diagonal 74 N° 321, La Plata', '221-567-8901', '1990-07-30', 'ACTIVO');

-- Internos
INSERT INTO internos (numero_legajo, apellido, nombre, dni, id_establecimiento, pabellon_actual, piso_actual, fecha_ingreso, situacion_procesal, estado) VALUES
('LEG-2023-001', 'Sánchez', 'Diego Alberto', '32456789', 1, 'A', 2, '2023-01-15', 'CONDENADO', 'ACTIVO'),
('LEG-2023-045', 'Romero', 'Marcelo Fabián', '29876543', 1, 'B', 1, '2023-03-22', 'PROCESADO', 'ACTIVO'),
('LEG-2024-012', 'Gómez', 'Sebastián Raúl', '31234567', 1, 'C', 3, '2024-02-10', 'PREVENTIVO', 'ACTIVO');

-- Autorizaciones
INSERT INTO autorizaciones (id_visitante, id_interno, tipo_relacion, descripcion_relacion, fecha_autorizacion, fecha_vencimiento, estado, id_autorizado_por) VALUES
(1, 1, 'CONYUGE', 'Esposa', '2023-02-01', NULL, 'VIGENTE', 1),
(2, 1, 'HERMANO_A', 'Hermano', '2023-02-01', '2025-12-31', 'VIGENTE', 1),
(3, 2, 'MADRE', 'Madre del interno', '2023-04-01', NULL, 'VIGENTE', 1),
(4, 3, 'AMIGO', 'Amigo de la infancia', '2024-03-15', '2025-03-15', 'VIGENTE', 1);

-- Visitas (históricas)
INSERT INTO visitas (id_visitante, id_interno, fecha_visita, hora_ingreso, hora_egreso, estado_visita, id_operador_ingreso, id_operador_egreso) VALUES
(1, 1, '2025-09-15', '10:30:00', '14:15:00', 'FINALIZADA', 2, 2),
(1, 1, '2025-09-22', '11:00:00', '15:00:00', 'FINALIZADA', 2, 2),
(2, 1, '2025-09-18', '09:45:00', '13:20:00', 'FINALIZADA', 2, 2),
(3, 2, '2025-09-20', '10:00:00', '14:30:00', 'FINALIZADA', 2, 2),
(4, 3, '2025-09-25', '11:30:00', '15:00:00', 'FINALIZADA', 2, 2);

-- Restricciones
INSERT INTO restricciones (id_visitante, tipo_restriccion, motivo, fecha_inicio, fecha_fin, aplicable_a, activa, id_creado_por) VALUES
(2, 'CONDUCTA', 'Intento de ingreso de elementos prohibidos - Resolución 234/2024', '2024-08-01', '2025-02-01', 'TODOS', FALSE, 3);