-- ============================================
-- INSERCIÓN DE DATOS DE PRUEBA ACTUALIZADOS
-- Unidad Penitenciaria Única: Lisandro Olmos
-- ============================================

-- Limpieza de datos existentes
DELETE FROM autorizaciones;
DELETE FROM visitas;
DELETE FROM restricciones;
DELETE FROM internos;
DELETE FROM visitantes;
DELETE FROM usuarios;
DELETE FROM establecimientos;
-- Resetear auto_increment
ALTER TABLE autorizaciones AUTO_INCREMENT = 1;
ALTER TABLE visitas AUTO_INCREMENT = 1;
ALTER TABLE restricciones AUTO_INCREMENT = 1;
ALTER TABLE internos AUTO_INCREMENT = 1;
ALTER TABLE visitantes AUTO_INCREMENT = 1;
ALTER TABLE usuarios AUTO_INCREMENT = 1;
ALTER TABLE establecimientos AUTO_INCREMENT = 1;

-- ============================================
-- ESTABLECIMIENTO ÚNICO
-- ============================================
INSERT INTO establecimientos (nombre, nombre_visita, modalidad_visita, dias_habilita, horario_inicio, horario_fin) VALUES
('Unidad Nº1 - Lisandro Olmos', 'UP1 Lisandro Olmos', 'MIXTA', 'LUNES,MARTES,MIERCOLES,JUEVES,VIERNES,SABADO,DOMINGO', '07:00', '16:00');

-- ============================================
-- USUARIOS DEL SISTEMA
-- ============================================
INSERT INTO usuarios (nombre_usuario, contrasena, nombre_completo, rol, id_establecimiento) VALUES
('admin', SHA2('Admin123!', 256), 'Juan Pérez', 'ADMINISTRADOR', 1),
('operador1', SHA2('Opera123!', 256), 'María González', 'OPERADOR', 1),
('supervisor1', SHA2('Super123!', 256), 'Carlos Rodríguez', 'SUPERVISOR', 1);

-- ============================================
-- INTERNOS (100 REGISTROS)
-- ============================================
INSERT INTO internos (numero_legajo, apellido, nombre, dni, id_establecimiento, pabellon_actual, piso_actual, fecha_ingreso, situacion_procesal, estado) VALUES
-- Pabellón A - Piso 1 (Legajos 001-007)
('LEG-2024-001', 'García', 'Roberto Carlos', '23456789', 1, 'A', 1, '2023-01-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-002', 'Rodríguez', 'José Luis', '24567890', 1, 'A', 1, '2023-02-20', 'PROCESADO', 'ACTIVO'),
('LEG-2024-003', 'Fernández', 'Miguel Ángel', '25678901', 1, 'A', 1, '2023-03-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-004', 'López', 'Juan Carlos', '26789012', 1, 'A', 1, '2023-04-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-005', 'Martínez', 'Diego Alberto', '27890123', 1, 'A', 1, '2023-05-12', 'CONDENADO', 'ACTIVO'),
('LEG-2024-006', 'Pérez', 'Horacio Raúl', '28901234', 1, 'A', 1, '2023-06-18', 'PROCESADO', 'ACTIVO'),
('LEG-2024-007', 'Gómez', 'Rubén Dario', '29012345', 1, 'A', 1, '2023-07-22', 'CONDENADO', 'ACTIVO'),

-- Pabellón A - Piso 2 (Legajos 008-014)
('LEG-2024-008', 'Sánchez', 'Luis Alberto', '30123456', 1, 'A', 2, '2023-08-14', 'CONDENADO', 'ACTIVO'),
('LEG-2024-009', 'Díaz', 'Jorge Mario', '31234567', 1, 'A', 2, '2023-09-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-010', 'Romero', 'Carlos Eduardo', '32345678', 1, 'A', 2, '2023-10-03', 'PROCESADO', 'ACTIVO'),
('LEG-2024-011', 'Torres', 'Roberto Fabián', '33456789', 1, 'A', 2, '2023-11-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-012', 'Silva', 'Juan Manuel', '34567890', 1, 'A', 2, '2023-12-01', 'CONDENADO', 'ACTIVO'),
('LEG-2024-013', 'Molina', 'Héctor Daniel', '35678901', 1, 'A', 2, '2024-01-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-014', 'Vargas', 'Pedro Antonio', '36789012', 1, 'A', 2, '2024-02-05', 'PROCESADO', 'ACTIVO'),

-- Pabellón A - Piso 3 (Legajos 015-020)
('LEG-2024-015', 'Castro', 'Gustavo Adrián', '37890123', 1, 'A', 3, '2024-02-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-016', 'Ortega', 'Martín Omar', '38901234', 1, 'A', 3, '2024-03-12', 'CONDENADO', 'ACTIVO'),
('LEG-2024-017', 'Ramírez', 'Sergio Daniel', '39012345', 1, 'A', 3, '2024-04-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-018', 'Morales', 'Fernando Gabriel', '40123456', 1, 'A', 3, '2024-05-01', 'PROCESADO', 'ACTIVO'),
('LEG-2024-019', 'Herrera', 'Juan Pablo', '41234567', 1, 'A', 3, '2024-05-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-020', 'Mendoza', 'Luis Roberto', '42345678', 1, 'A', 3, '2024-06-15', 'CONDENADO', 'ACTIVO'),

-- Pabellón B - Piso 1 (Legajos 021-027)
('LEG-2024-021', 'Cruz', 'Carlos Alberto', '43456789', 1, 'B', 1, '2024-01-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-022', 'Flores', 'José María', '44567890', 1, 'B', 1, '2024-02-12', 'PROCESADO', 'ACTIVO'),
('LEG-2024-023', 'Reyes', 'Roberto Luis', '45678901', 1, 'B', 1, '2024-03-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-024', 'Jiménez', 'Juan Carlos', '46789012', 1, 'B', 1, '2024-04-02', 'CONDENADO', 'ACTIVO'),
('LEG-2024-025', 'Álvarez', 'Miguel Ángel', '47890123', 1, 'B', 1, '2024-04-25', 'CONDENADO', 'ACTIVO'),
('LEG-2024-026', 'Núñez', 'Horacio Raúl', '48901234', 1, 'B', 1, '2024-05-18', 'PROCESADO', 'ACTIVO'),
('LEG-2024-027', 'Rojas', 'Rubén Dario', '49012345', 1, 'B', 1, '2024-06-10', 'CONDENADO', 'ACTIVO'),

-- Pabellón B - Piso 2 (Legajos 028-034)
('LEG-2024-028', 'Gutiérrez', 'Luis Alberto', '50123456', 1, 'B', 2, '2024-01-22', 'CONDENADO', 'ACTIVO'),
('LEG-2024-029', 'Santiago', 'Jorge Mario', '51234567', 1, 'B', 2, '2024-02-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-030', 'Paredes', 'Carlos Eduardo', '52345678', 1, 'B', 2, '2024-03-10', 'PROCESADO', 'ACTIVO'),
('LEG-2024-031', 'Guerra', 'Roberto Fabián', '53456789', 1, 'B', 2, '2024-04-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-032', 'Cortés', 'Juan Manuel', '54567890', 1, 'B', 2, '2024-04-28', 'CONDENADO', 'ACTIVO'),
('LEG-2024-033', 'Rivas', 'Héctor Daniel', '55678901', 1, 'B', 2, '2024-05-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-034', 'Mira', 'Pedro Antonio', '56789012', 1, 'B', 2, '2024-06-12', 'PROCESADO', 'ACTIVO'),

-- Pabellón B - Piso 3 (Legajos 035-040)
('LEG-2024-035', 'Pino', 'Gustavo Adrián', '57890123', 1, 'B', 3, '2024-01-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-036', 'Vega', 'Martín Omar', '58901234', 1, 'B', 3, '2024-02-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-037', 'Fuentes', 'Sergio Daniel', '59012345', 1, 'B', 3, '2024-03-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-038', 'Cordero', 'Fernando Gabriel', '60123456', 1, 'B', 3, '2024-03-28', 'PROCESADO', 'ACTIVO'),
('LEG-2024-039', 'Luna', 'Juan Pablo', '61234567', 1, 'B', 3, '2024-04-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-040', 'Salazar', 'Luis Roberto', '62345678', 1, 'B', 3, '2024-05-15', 'CONDENADO', 'ACTIVO'),

-- Pabellón C - Piso 1 (Legajos 041-047)
('LEG-2024-041', 'Bravo', 'Carlos Alberto', '63456789', 1, 'C', 1, '2023-11-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-042', 'León', 'José María', '64567890', 1, 'C', 1, '2023-12-05', 'PROCESADO', 'ACTIVO'),
('LEG-2024-043', 'Ramos', 'Roberto Luis', '65678901', 1, 'C', 1, '2024-01-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-044', 'Gallo', 'Juan Carlos', '66789012', 1, 'C', 1, '2024-02-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-045', 'Solís', 'Miguel Ángel', '67890123', 1, 'C', 1, '2024-03-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-046', 'Benítez', 'Horacio Raúl', '68901234', 1, 'C', 1, '2024-04-05', 'PROCESADO', 'ACTIVO'),
('LEG-2024-047', 'Ibarra', 'Rubén Dario', '69012345', 1, 'C', 1, '2024-04-28', 'CONDENADO', 'ACTIVO'),

-- Pabellón C - Piso 2 (Legajos 048-054)
('LEG-2024-048', 'Cisneros', 'Luis Alberto', '70123456', 1, 'C', 2, '2024-01-12', 'CONDENADO', 'ACTIVO'),
('LEG-2024-049', 'Ocampo', 'Jorge Mario', '71234567', 1, 'C', 2, '2024-02-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-050', 'Beltrán', 'Carlos Eduardo', '72345678', 1, 'C', 2, '2024-03-05', 'PROCESADO', 'ACTIVO'),
('LEG-2024-051', 'Camacho', 'Roberto Fabián', '73456789', 1, 'C', 2, '2024-03-30', 'CONDENADO', 'ACTIVO'),
('LEG-2024-052', 'Pacheco', 'Juan Manuel', '74567890', 1, 'C', 2, '2024-04-22', 'CONDENADO', 'ACTIVO'),
('LEG-2024-053', 'Lozano', 'Héctor Daniel', '75678901', 1, 'C', 2, '2024-05-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-054', 'Soto', 'Pedro Antonio', '76789012', 1, 'C', 2, '2024-06-08', 'PROCESADO', 'ACTIVO'),

-- Pabellón C - Piso 3 (Legajos 055-060)
('LEG-2024-055', 'Hidalgo', 'Gustavo Adrián', '77890123', 1, 'C', 3, '2024-01-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-056', 'Navarro', 'Martín Omar', '78901234', 1, 'C', 3, '2024-02-02', 'CONDENADO', 'ACTIVO'),
('LEG-2024-057', 'Durán', 'Sergio Daniel', '79012345', 1, 'C', 3, '2024-02-25', 'CONDENADO', 'ACTIVO'),
('LEG-2024-058', 'Cabrera', 'Fernando Gabriel', '80123456', 1, 'C', 3, '2024-03-20', 'PROCESADO', 'ACTIVO'),
('LEG-2024-059', 'Paredes', 'Juan Pablo', '81234567', 1, 'C', 3, '2024-04-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-060', 'Melo', 'Luis Roberto', '82345678', 1, 'C', 3, '2024-05-10', 'CONDENADO', 'ACTIVO'),

-- Pabellón D - Piso 1 (Legajos 061-067)
('LEG-2024-061', 'Acosta', 'Carlos Alberto', '83456789', 1, 'D', 1, '2023-10-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-062', 'Barrera', 'José María', '84567890', 1, 'D', 1, '2023-11-10', 'PROCESADO', 'ACTIVO'),
('LEG-2024-063', 'Coronel', 'Roberto Luis', '85678901', 1, 'D', 1, '2023-12-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-064', 'Delgado', 'Juan Carlos', '86789012', 1, 'D', 1, '2024-01-18', 'CONDENADO', 'ACTIVO'),
('LEG-2024-065', 'Enríquez', 'Miguel Ángel', '87890123', 1, 'D', 1, '2024-02-12', 'CONDENADO', 'ACTIVO'),
('LEG-2024-066', 'Figueroa', 'Horacio Raúl', '88901234', 1, 'D', 1, '2024-03-08', 'PROCESADO', 'ACTIVO'),
('LEG-2024-067', 'Garrido', 'Rubén Dario', '89012345', 1, 'D', 1, '2024-04-02', 'CONDENADO', 'ACTIVO'),

-- Pabellón D - Piso 2 (Legajos 068-074)
('LEG-2024-068', 'Herrero', 'Luis Alberto', '90123456', 1, 'D', 2, '2024-01-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-069', 'Iglesias', 'Jorge Mario', '91234567', 1, 'D', 2, '2024-01-30', 'CONDENADO', 'ACTIVO'),
('LEG-2024-070', 'Jordán', 'Carlos Eduardo', '92345678', 1, 'D', 2, '2024-02-25', 'PROCESADO', 'ACTIVO'),
('LEG-2024-071', 'Koch', 'Roberto Fabián', '93456789', 1, 'D', 2, '2024-03-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-072', 'Lema', 'Juan Manuel', '94567890', 1, 'D', 2, '2024-04-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-073', 'Montero', 'Héctor Daniel', '95678901', 1, 'D', 2, '2024-05-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-074', 'Naranjo', 'Pedro Antonio', '96789012', 1, 'D', 2, '2024-06-03', 'PROCESADO', 'ACTIVO'),

-- Pabellón D - Piso 3 (Legajos 075-080)
('LEG-2024-075', 'Orellana', 'Gustavo Adrián', '97890123', 1, 'D', 3, '2024-01-02', 'CONDENADO', 'ACTIVO'),
('LEG-2024-076', 'Peña', 'Martín Omar', '98901234', 1, 'D', 3, '2024-01-25', 'CONDENADO', 'ACTIVO'),
('LEG-2024-077', 'Quintana', 'Sergio Daniel', '99012345', 1, 'D', 3, '2024-02-18', 'CONDENADO', 'ACTIVO'),
('LEG-2024-078', 'Rosas', 'Fernando Gabriel', '100123456', 1, 'D', 3, '2024-03-15', 'PROCESADO', 'ACTIVO'),
('LEG-2024-079', 'Sáenz', 'Juan Pablo', '101234567', 1, 'D', 3, '2024-04-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-080', 'Toro', 'Luis Roberto', '102345678', 1, 'D', 3, '2024-05-05', 'CONDENADO', 'ACTIVO'),

-- Pabellón E - Piso 1 (Legajos 081-087)
('LEG-2024-081', 'Urbina', 'Carlos Alberto', '103456789', 1, 'E', 1, '2023-09-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-082', 'Valdez', 'José María', '104567890', 1, 'E', 1, '2023-10-15', 'PROCESADO', 'ACTIVO'),
('LEG-2024-083', 'Wagner', 'Roberto Luis', '105678901', 1, 'E', 1, '2023-11-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-084', 'Xavier', 'Juan Carlos', '106789012', 1, 'E', 1, '2023-12-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-085', 'Yáñez', 'Miguel Ángel', '107890123', 1, 'E', 1, '2024-01-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-086', 'Zamora', 'Horacio Raúl', '108901234', 1, 'E', 1, '2024-02-10', 'PROCESADO', 'ACTIVO'),
('LEG-2024-087', 'Abad', 'Rubén Dario', '109012345', 1, 'E', 1, '2024-03-05', 'CONDENADO', 'ACTIVO'),

-- Pabellón E - Piso 2 (Legajos 088-094)
('LEG-2024-088', 'Becerra', 'Luis Alberto', '110123456', 1, 'E', 2, '2024-01-08', 'CONDENADO', 'ACTIVO'),
('LEG-2024-089', 'Calderón', 'Jorge Mario', '111234567', 1, 'E', 2, '2024-02-02', 'CONDENADO', 'ACTIVO'),
('LEG-2024-090', 'Domínguez', 'Carlos Eduardo', '112345678', 1, 'E', 2, '2024-02-25', 'PROCESADO', 'ACTIVO'),
('LEG-2024-091', 'Estrada', 'Roberto Fabián', '113456789', 1, 'E', 2, '2024-03-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-092', 'Falla', 'Juan Manuel', '114567890', 1, 'E', 2, '2024-04-15', 'CONDENADO', 'ACTIVO'),
('LEG-2024-093', 'Galeano', 'Héctor Daniel', '115678901', 1, 'E', 2, '2024-05-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-094', 'Hoyos', 'Pedro Antonio', '116789012', 1, 'E', 2, '2024-06-03', 'PROCESADO', 'ACTIVO'),

-- Pabellón E - Piso 3 (Legajos 095-100)
('LEG-2024-095', 'Izarra', 'Gustavo Adrián', '117890123', 1, 'E', 3, '2024-01-05', 'CONDENADO', 'ACTIVO'),
('LEG-2024-096', 'Jaén', 'Martín Omar', '118901234', 1, 'E', 3, '2024-01-28', 'CONDENADO', 'ACTIVO'),
('LEG-2024-097', 'Krause', 'Sergio Daniel', '119012345', 1, 'E', 3, '2024-02-20', 'CONDENADO', 'ACTIVO'),
('LEG-2024-098', 'Lara', 'Fernando Gabriel', '120123456', 1, 'E', 3, '2024-03-15', 'PROCESADO', 'ACTIVO'),
('LEG-2024-099', 'Manso', 'Juan Pablo', '121234567', 1, 'E', 3, '2024-04-10', 'CONDENADO', 'ACTIVO'),
('LEG-2024-100', 'Noriega', 'Luis Roberto', '122345678', 1, 'E', 3, '2024-05-05', 'CONDENADO', 'ACTIVO');

-- ============================================
-- VISITANTES (10 REGISTROS)
-- ============================================
INSERT INTO visitantes (dni, apellido, nombre, domicilio, telefono, fecha_nacimiento, estado) VALUES
('20345678', 'González', 'Ana María', 'Calle 5 N° 1234, Lisandro Olmos', '221-456-7890', '1985-03-15', 'ACTIVO'),
('21456789', 'Fernández', 'Roberto Carlos', 'Av. Centenario N° 567, Lisandro Olmos', '221-567-8901', '1980-07-22', 'ACTIVO'),
('22567890', 'López', 'Laura Beatriz', 'Calle 12 N° 890, Lisandro Olmos', '221-678-9012', '1992-11-08', 'ACTIVO'),
('23678901', 'Martínez', 'Jorge Luis', 'Diagonal 73 N° 345, Lisandro Olmos', '221-789-0123', '1988-05-30', 'ACTIVO'),
('24789012', 'Pérez', 'Mónica Susana', 'Calle 8 N° 678, Lisandro Olmos', '221-890-1234', '1990-09-18', 'ACTIVO'),
('25890123', 'Rodríguez', 'Carlos Alberto', 'Av. 44 N° 123, Gonnet', '221-901-2345', '1975-12-25', 'ACTIVO'),
('26901234', 'Sánchez', 'María del Carmen', 'Calle 15 N° 456, Lisandro Olmos', '221-012-3456', '1983-04-10', 'ACTIVO'),
('27012345', 'Romero', 'Diego Martín', 'Calle 3 N° 789, Lisandro Olmos', '221-123-4567', '1995-08-14', 'ACTIVO'),
('28123456', 'Gómez', 'Silvana Andrea', 'Av. 32 N° 234, Ringuelet', '221-234-5678', '1987-06-20', 'ACTIVO'),
('29234567', 'Díaz', 'Juan Pedro', 'Calle 18 N° 567, Lisandro Olmos', '221-345-6789', '1991-02-28', 'ACTIVO');

-- ============================================
-- FIN DE DATOS DE PRUEBA
-- ============================================
-- Nota: No se incluyen autorizaciones, visitas ni restricciones
-- para que puedan ser creadas desde cero durante las pruebas