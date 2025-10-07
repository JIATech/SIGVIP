# SIGVIP - Sistema Integral de Gestión de Visitas Penitenciarias

**Proyecto académico** - Seminario de Práctica de Informática (INF275-11265)
**Universidad Siglo 21**

Estudiante: Arnaboldi, Juan Ignacio (VINF06364)
Profesor: Marcos Darío Aranda

## 📋 Descripción

SIGVIP es un sistema de escritorio desarrollado en Java para la gestión integral de visitas a establecimientos penitenciarios. Implementa control de acceso, registro de visitantes, autorizaciones y reportes, cumpliendo con restricciones académicas estrictas.

## 🎯 Estado del Proyecto

### ✅ Completado (95% funcional)

**Backend (100%)**:
- ✅ 7 entidades del modelo con lógica de negocio
- ✅ 10 enums para estados y tipos
- ✅ 7 DAOs completos con JDBC
- ✅ 3 controladores (MVC)
- ✅ Servicios de validación y utilidades
- ✅ Base de datos MySQL completa

**Frontend (80%)**:
- ✅ Login con autenticación SHA-256
- ✅ Menú principal con navegación
- ✅ **VistaControlAcceso** (RF003/RF004) - **CRÍTICO** ✨
- ✅ VistaRegistroVisitante (RF001)
- 🚧 Stubs funcionales: Autorizaciones, Gestión Internos, Reportes

**Infraestructura**:
- ✅ Configuración de base de datos
- ✅ Datos de prueba
- ✅ Documentación completa

### 🚧 Pendiente (Opcional)

- Implementación completa de vistas secundarias (RF002, RF006, RF007)
- Generación de reportes PDF (requiere Apache PDFBox)
- Testing automatizado

## 🛠️ Tecnologías

- **Java SE 8+** (JDK 24 Temurin configurado)
- **MySQL 8.0** (charset utf8mb4)
- **Swing** (GUI - sin dependencias externas)
- **JDBC** puro (sin ORM - restricción académica)
- **MySQL Connector/J 8.0**

## 🏗️ Arquitectura

Patrón **MVC** implementado manualmente (sin frameworks):

```
src/com/sigvip/
├── modelo/           # Entidades + lógica de negocio
├── vista/            # Swing UI
├── controlador/      # Mediadores MVC
├── persistencia/     # DAOs JDBC (parte del Modelo)
└── utilidades/       # Validadores, servicios
```

## 📦 Instalación y Configuración

### Requisitos Previos

1. **Java JDK 8+** instalado
2. **MySQL 8.0** instalado y ejecutándose
3. **IntelliJ IDEA** (o cualquier IDE Java)

### Paso 1: Configurar Base de Datos

```bash
# Ejecutar en MySQL
mysql -u root -p < database/sigvip_db.sql

# Cargar datos de prueba
mysql -u root -p sigvip_db < database/datos_de_prueba.sql
```

### Paso 2: Descargar MySQL Connector/J

1. Descargar de: https://dev.mysql.com/downloads/connector/j/
2. Extraer `mysql-connector-j-8.x.x.jar`
3. Colocar en `lib/`
4. **En IntelliJ IDEA**:
   - `File → Project Structure → Libraries`
   - `+ → Java → seleccionar el JAR`

### Paso 3: Configurar Conexión

Editar `resources/config.properties`:

```properties
db.url=jdbc:mysql://localhost:3306/sigvip_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=UTC
db.usuario=root
db.contrasena=TU_CONTRASEÑA_MYSQL
db.driver=com.mysql.cj.jdbc.Driver
```

### Paso 4: Compilar y Ejecutar

**En IntelliJ IDEA**:
```
Build → Build Project (Ctrl+F9)
Run → Run 'Main' (Shift+F10)
```

**Desde línea de comandos**:
```bash
# Compilar
javac -cp "lib/*" -d out src/com/sigvip/**/*.java

# Ejecutar
java -cp "out;lib/*" com.sigvip.Main
```

## 👤 Usuarios de Prueba

La base de datos incluye usuarios de prueba:

| Usuario      | Contraseña | Rol            |
|--------------|------------|----------------|
| admin        | Admin123!  | ADMINISTRADOR  |
| operador1    | Opera123!  | OPERADOR       |
| supervisor1  | Super123!  | SUPERVISOR     |

## 🔑 Funcionalidades Implementadas

### RF003/RF004: Control de Acceso ⭐ **MÁS IMPORTANTE**

**Validación automática de 6 pasos** al registrar ingreso:
1. ✅ Visitante existe y está ACTIVO
2. ✅ Autorización vigente (no vencida, estado VIGENTE)
3. ✅ Sin restricciones activas
4. ✅ Horario permitido por establecimiento
5. ✅ Interno disponible para visitas
6. ✅ Capacidad no superada

### RF001: Registrar Visitante

- Formulario completo de alta
- Validación de DNI único
- Validación de edad >= 18 años
- Estados: ACTIVO, SUSPENDIDO, INACTIVO

### Otras Funcionalidades

- Login con SHA-256
- Navegación por roles
- Tabla de visitas en curso
- Registro de auditoría (en base de datos)

## 📊 Base de Datos

**8 tablas normalizadas a 3NF**:

- `visitantes` - Registro de visitantes
- `internos` - Registro de internos
- `usuarios` - Usuarios del sistema
- `establecimientos` - Centros penitenciarios
- `autorizaciones` - Permisos visitante-interno
- `visitas` - Eventos de visita
- `restricciones` - Restricciones de acceso
- `auditoria` - Log completo de operaciones

## 🔒 Seguridad

- Contraseñas hasheadas con SHA-256 (RNF003)
- PreparedStatements (prevención SQL Injection)
- Control de permisos por rol
- Auditoría completa de operaciones

## 📁 Estructura del Proyecto

```
SIGVIP/
├── src/com/sigvip/
│   ├── Main.java                      # Punto de entrada
│   ├── modelo/                        # 7 entidades + 10 enums
│   ├── vista/                         # 7 vistas Swing
│   ├── controlador/                   # 3 controladores MVC
│   ├── persistencia/                  # 7 DAOs + ConexionBD
│   └── utilidades/                    # Validadores + servicios
├── resources/
│   └── config.properties              # Configuración BD
├── lib/
│   └── mysql-connector-j-8.x.x.jar    # Driver JDBC
├── database/
│   ├── sigvip_db.sql                  # Schema
│   ├── datos_de_prueba.sql            # Datos iniciales
│   └── consultas_sql_prueba.sql       # Queries de referencia
├── CLAUDE.md                          # Guía para Claude Code
└── README.md                          # Este archivo
```

## 🚀 Próximos Pasos (Opcional)

Para convertir los stubs en funcionalidades completas:

1. **VistaAutorizaciones** (RF002):
   - Formulario de nueva autorización
   - Búsqueda de visitantes e internos
   - Gestión de estados

2. **VistaGestionInternos** (RF006):
   - CRUD de internos
   - Control de ubicaciones
   - Gestión de traslados

3. **VistaReportes** + **GeneradorReportes** (RF007):
   - Agregar Apache PDFBox a dependencias
   - Implementar generación de PDFs
   - Exportación de reportes

## 📞 Soporte

Para consultas sobre el proyecto académico:
- Repositorio: https://github.com/JIATech/sigvip.git
- Documentación completa: `.claude/context/ARNABOLDI-JUAN-AP2.pdf`

## 📄 Licencia

Proyecto académico - Universidad Siglo 21 © 2024
