-- ============================================
-- SCRIPT DE CREACIÓN BASE DE DATOS SIGVIP
-- Sistema Integral de Gestión de Visitas Penitenciarias
-- Versión: 1.0
-- Fecha: Octubre 2025
-- ============================================

-- Crear base de datos
DROP DATABASE IF EXISTS sigvip_db;
CREATE DATABASE sigvip_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE sigvip_db;

-- ============================================
-- TABLA: establecimientos
-- ============================================
CREATE TABLE establecimientos (
    id_establecimiento INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nombre_visita VARCHAR(100),
    modalidad_visita ENUM('PRESENCIAL','SECTOR','MIXTA') DEFAULT 'SECTOR',
    dias_habilita VARCHAR(50),
    horario_inicio TIME,
    horario_fin TIME NOT NULL DEFAULT '16:00',
    activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB;

-- ============================================
-- TABLA: visitantes
-- ============================================
CREATE TABLE visitantes (
    id_visitante BIGINT AUTO_INCREMENT PRIMARY KEY,
    dni VARCHAR(10) NOT NULL UNIQUE,
    apellido VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    domicilio VARCHAR(255),
    telefono VARCHAR(20),
    fecha_nacimiento DATE NOT NULL,
    foto MEDIUMBLOB,
    estado ENUM('ACTIVO','SUSPENDIDO','INACTIVO') DEFAULT 'ACTIVO',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dni (dni),
    INDEX idx_apellido_nombre (apellido, nombre)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: internos
-- ============================================
CREATE TABLE internos (
    id_interno BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_legajo VARCHAR(20) NOT NULL UNIQUE,
    apellido VARCHAR(100) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    dni VARCHAR(10) NOT NULL,
    id_establecimiento INT NOT NULL,
    pabellon_actual VARCHAR(20) NOT NULL,
    piso_actual TINYINT NOT NULL,
    fecha_ingreso DATE NOT NULL,
    unidad_procedencia VARCHAR(100),
    situacion_procesal ENUM('PROCESADO','CONDENADO','PREVENTIVO') NOT NULL,
    estado ENUM('ACTIVO','TRASLADADO','EGRESADO') DEFAULT 'ACTIVO',
    INDEX idx_legajo (numero_legajo),
    INDEX idx_ubicacion (pabellon_actual, piso_actual),
    FOREIGN KEY (id_establecimiento) REFERENCES establecimientos(id_establecimiento)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: usuarios
-- ============================================
CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    rol ENUM('OPERADOR','SUPERVISOR','ADMINISTRADOR') NOT NULL,
    id_establecimiento INT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP NULL,
    INDEX idx_usuario (nombre_usuario),
    INDEX idx_rol_activo (rol, activo),
    FOREIGN KEY (id_establecimiento) REFERENCES establecimientos(id_establecimiento)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: autorizaciones
-- ============================================
CREATE TABLE autorizaciones (
    id_autorizacion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_visitante BIGINT NOT NULL,
    id_interno BIGINT NOT NULL,
    tipo_relacion ENUM('PADRE','MADRE','HIJO_A','HERMANO_A','CONYUGE',
                       'CONCUBINO_A','AMIGO','FAMILIAR','ABOGADO','OTRO') NOT NULL,
    descripcion_relacion VARCHAR(100),
    fecha_autorizacion DATE NOT NULL,
    fecha_vencimiento DATE,
    estado ENUM('VIGENTE','SUSPENDIDA','REVOCADA','VENCIDA') DEFAULT 'VIGENTE',
    id_autorizado_por BIGINT,
    observaciones TEXT,
    INDEX idx_visitante_interno (id_visitante, id_interno),
    INDEX idx_vigencia (estado, fecha_vencimiento),
    UNIQUE KEY uk_visitante_interno (id_visitante, id_interno),
    FOREIGN KEY (id_visitante) REFERENCES visitantes(id_visitante),
    FOREIGN KEY (id_interno) REFERENCES internos(id_interno),
    FOREIGN KEY (id_autorizado_por) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: visitas
-- ============================================
CREATE TABLE visitas (
    id_visita BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_visitante BIGINT NOT NULL,
    id_interno BIGINT NOT NULL,
    fecha_visita DATE NOT NULL,
    hora_ingreso TIME NOT NULL,
    hora_egreso TIME,
    estado_visita ENUM('PROGRAMADA','EN_CURSO','FINALIZADA','CANCELADA') DEFAULT 'PROGRAMADA',
    id_operador_ingreso BIGINT NOT NULL,
    id_operador_egreso BIGINT,
    observaciones TEXT,
    INDEX idx_fecha (fecha_visita),
    INDEX idx_visitante (id_visitante),
    INDEX idx_interno (id_interno),
    INDEX idx_activas (estado_visita, fecha_visita),
    FOREIGN KEY (id_visitante) REFERENCES visitantes(id_visitante),
    FOREIGN KEY (id_interno) REFERENCES internos(id_interno),
    FOREIGN KEY (id_operador_ingreso) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_operador_egreso) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: restricciones
-- ============================================
CREATE TABLE restricciones (
    id_restriccion BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_visitante BIGINT NOT NULL,
    tipo_restriccion ENUM('CONDUCTA','JUDICIAL','ADMINISTRATIVA','SEGURIDAD') NOT NULL,
    motivo TEXT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE,
    aplicable_a ENUM('TODOS','INTERNO_ESPECIFICO') DEFAULT 'TODOS',
    id_interno BIGINT,
    activa BOOLEAN DEFAULT TRUE,
    id_creado_por BIGINT,
    INDEX idx_visitante_activa (id_visitante, activa),
    INDEX idx_vigencia (fecha_inicio, fecha_fin),
    FOREIGN KEY (id_visitante) REFERENCES visitantes(id_visitante),
    FOREIGN KEY (id_interno) REFERENCES internos(id_interno),
    FOREIGN KEY (id_creado_por) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: auditoria
-- ============================================
CREATE TABLE auditoria (
    id_auditoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    tipo_operacion ENUM('INSERT','UPDATE','DELETE','LOGIN','LOGOUT') NOT NULL,
    tabla_afectada VARCHAR(50),
    id_registro BIGINT,
    datos_anteriores TEXT,
    datos_nuevos TEXT,
    timestamp_operacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_origen VARCHAR(45),
    INDEX idx_usuario_fecha (id_usuario, timestamp_operacion),
    INDEX idx_tabla_operacion (tabla_afectada, tipo_operacion),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

-- ============================================
-- TABLA: reportes_generados
-- ============================================
CREATE TABLE reportes_generados (
    id_reporte BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_reporte ENUM('VISITAS_FECHA','VISITAS_VISITANTE','VISITAS_INTERNO',
                      'ESTADISTICAS','RESTRICCIONES_ACTIVAS','AUTORIZACIONES_VIGENTES') NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    parametros_filtro TEXT,
    contenido LONGTEXT NOT NULL,
    total_registros INT DEFAULT 0,
    fecha_generacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_generado_por BIGINT NOT NULL,
    INDEX idx_tipo_fecha (tipo_reporte, fecha_generacion),
    INDEX idx_usuario_generador (id_generado_por),
    INDEX idx_fecha_generacion (fecha_generacion),
    FOREIGN KEY (id_generado_por) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;