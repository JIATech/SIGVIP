# SIGVIP - Sistema Integral de Gestión de Visitas Penitenciarias

**Proyecto académico** - Seminario de Práctica de Informática (INF275-11265)
**Universidad Siglo 21**

Estudiante: Arnaboldi, Juan Ignacio (VINF06364)
Profesor: Marcos Darío Aranda

## 📋 Descripción

SIGVIP es un sistema de escritorio desarrollado en Java para la gestión integral de visitas a establecimientos penitenciarios. Implementa control de acceso, registro de visitantes, autorizaciones y reportes, cumpliendo con restricciones académicas estrictas.

## 🎯 Estado del Proyecto

### ✅ Proyecto Completado (100% del MVP)

**Backend (100%)**:
- ✅ 8 entidades del modelo con lógica de negocio (incluye ReporteGenerado)
- ✅ 11 enums para estados y tipos
- ✅ 8 DAOs completos con JDBC (incluye ReporteDAO)
- ✅ 6 controladores MVC (Visitantes, Acceso, Reportes, Autorizaciones, Internos, Usuarios)
- ✅ Servicios de validación y utilidades
- ✅ Base de datos MySQL completa (9 tablas)

**Frontend (100%)**:
- ✅ Login con autenticación SHA-256
- ✅ Menú principal con navegación por roles
- ✅ **VistaControlAcceso** (RF003/RF004) - **FUNCIONALIDAD PRINCIPAL**
- ✅ **VistaRegistroVisitante** (RF001) - Completa con carga de fotos
- ✅ **VistaAutorizaciones** (RF002) - Completa con gestión de estados
- ✅ **VistaGestionInternos** (RF006) - Completa con traslados y ubicaciones
- ✅ **VistaReportes** (RF007) - Completa con reportes HTML y persistencia
- ✅ **VistaGestionUsuarios** (RF008) - Completa con seguridad triple capa

**Infraestructura**:
- ✅ Configuración de base de datos
- ✅ Datos de prueba
- ✅ Documentación completa

## 🛠️ Tecnologías

- **Java SE 8+** (JDK 24 Temurin configurado)
- **MySQL 8.0** (charset utf8mb4)
- **Swing** (GUI - sin dependencias externas)
- **JDBC** puro (sin ORM - restricción académica)
- **MySQL Connector/J 9.4.0**

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

1. **Java JDK 8+** instalado (verificar con `java -version`)
2. **MySQL 8.0** instalado y ejecutándose
3. **IntelliJ IDEA** (recomendado) o cualquier IDE Java
4. **Git** (opcional, para clonar el repositorio)

### Paso 1: Obtener el Proyecto

```bash
# Opción A: Clonar desde GitHub
git clone https://github.com/JIATech/sigvip.git
cd sigvip

# Opción B: Descargar ZIP desde GitHub y extraer
```

### Paso 2: Configurar Base de Datos

```bash
# 1. Asegurarse de que MySQL esté corriendo
# Windows: Verificar servicio "MySQL80" en services.msc
# Linux/Mac: sudo systemctl status mysql

# 2. Conectar a MySQL como root
mysql -u root -p

# 3. Crear la base de datos y las tablas
mysql -u root -p < database/sigvip_db.sql

# 4. Cargar datos de prueba (usuarios, visitantes, internos, etc.)
mysql -u root -p sigvip_db < database/datos_de_prueba.sql

# 5. Verificar que se creó correctamente
mysql -u root -p -e "USE sigvip_db; SHOW TABLES;"
```

**Debe mostrar 9 tablas**: visitantes, internos, usuarios, establecimientos, autorizaciones, visitas, restricciones, auditoria, reportes_generados

### Paso 3: Descargar MySQL Connector/J

1. Ir a: https://dev.mysql.com/downloads/connector/j/
2. Seleccionar "Platform Independent" y descargar el ZIP
3. Extraer el archivo `mysql-connector-j-9.4.0.jar` (o versión más reciente)
4. Copiar el JAR a la carpeta `lib/` del proyecto
5. **En IntelliJ IDEA**:
   - Abrir `File → Project Structure → Libraries`
   - Clic en `+` → `Java`
   - Seleccionar el JAR en `lib/`
   - Clic en `Apply` y `OK`

### Paso 4: Configurar Conexión a Base de Datos

Editar el archivo `resources/config.properties` con tus credenciales de MySQL:

```properties
db.url=jdbc:mysql://localhost:3306/sigvip_db?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=UTC
db.usuario=root
db.contrasena=TU_CONTRASEÑA_MYSQL_AQUI
db.driver=com.mysql.cj.jdbc.Driver
```

**⚠️ IMPORTANTE**: Reemplazar `TU_CONTRASEÑA_MYSQL_AQUI` con tu contraseña real de MySQL.

### Paso 5: Compilar y Ejecutar

#### Opción A: Usando IntelliJ IDEA (Recomendado)

1. Abrir el proyecto en IntelliJ IDEA
2. Esperar a que se indexe el proyecto
3. Compilar: `Build → Build Project` (o `Ctrl+F9`)
4. Ejecutar: `Run → Run 'Main'` (o `Shift+F10`)
5. Debe aparecer la ventana de login

#### Opción B: Desde Línea de Comandos

**Windows**:
```bash
# Compilar todas las clases
javac -encoding UTF-8 -cp "lib\*" -d out src\com\sigvip\*.java src\com\sigvip\modelo\*.java src\com\sigvip\modelo\enums\*.java src\com\sigvip\vista\*.java src\com\sigvip\controlador\*.java src\com\sigvip\persistencia\*.java src\com\sigvip\utilidades\*.java

# Ejecutar
java -cp "out;lib\*" com.sigvip.Main
```

**Linux/Mac**:
```bash
# Compilar todas las clases
javac -encoding UTF-8 -cp "lib/*" -d out src/com/sigvip/*.java src/com/sigvip/modelo/*.java src/com/sigvip/modelo/enums/*.java src/com/sigvip/vista/*.java src/com/sigvip/controlador/*.java src/com/sigvip/persistencia/*.java src/com/sigvip/utilidades/*.java

# Ejecutar
java -cp "out:lib/*" com.sigvip.Main
```

### Paso 6: Iniciar Sesión

Una vez que la aplicación se ejecute, verás la ventana de login. Usa uno de estos usuarios de prueba:

| Usuario      | Contraseña | Rol            | Descripción |
|--------------|------------|----------------|-------------|
| **admin**    | Admin123!  | ADMINISTRADOR  | Acceso completo a todas las funciones |
| operador1    | Opera123!  | OPERADOR       | Control de ingreso/egreso de visitas |
| supervisor1  | Super123!  | SUPERVISOR     | Gestión intermedia |

**Recomendación**: Iniciar con el usuario `admin` para explorar todas las funcionalidades.

## 🔧 Resolución de Problemas Comunes

### Error: "No suitable driver found for jdbc:mysql"
**Causa**: MySQL Connector/J no está agregado correctamente al proyecto.
**Solución**:
1. Verificar que el archivo JAR esté en `lib/`
2. En IntelliJ: `File → Project Structure → Libraries` → verificar que el JAR esté listado
3. Limpiar y reconstruir: `Build → Rebuild Project`

### Error: "Access denied for user 'root'@'localhost'"
**Causa**: Contraseña incorrecta en `config.properties`
**Solución**:
1. Verificar contraseña de MySQL: `mysql -u root -p` desde terminal
2. Actualizar `resources/config.properties` con la contraseña correcta
3. Si olvidaste tu contraseña de MySQL, buscar "reset MySQL root password" para tu sistema operativo

### Error: "Unknown database 'sigvip_db'"
**Causa**: No se ejecutó el script de creación de base de datos
**Solución**:
```bash
mysql -u root -p < database/sigvip_db.sql
mysql -u root -p sigvip_db < database/datos_de_prueba.sql
```

### La ventana de login no aparece
**Causa**: Múltiples posibles causas
**Solución**:
1. Verificar que MySQL esté corriendo (verificar servicio)
2. Revisar consola de IntelliJ en busca de errores de conexión
3. Verificar que `config.properties` tenga configuración correcta
4. Probar conexión manual: `mysql -u root -p -e "USE sigvip_db; SELECT COUNT(*) FROM usuarios;"`

### Error: "Table 'sigvip_db.usuarios' doesn't exist"
**Causa**: Tablas no creadas o base de datos incompleta
**Solución**:
```bash
# Eliminar base de datos existente y recrear
mysql -u root -p -e "DROP DATABASE IF EXISTS sigvip_db;"
mysql -u root -p < database/sigvip_db.sql
mysql -u root -p sigvip_db < database/datos_de_prueba.sql
```

### Error de compilación: "package com.mysql.cj.jdbc does not exist"
**Causa**: MySQL Connector/J no está en el classpath
**Solución**:
- **IntelliJ**: Agregar JAR en `Project Structure → Libraries`
- **Línea de comandos**: Verificar que el JAR esté en `lib/` y usar `-cp "lib/*"` al compilar

### Validaciones no funcionan en Control de Acceso
**Causa**: Datos de prueba no cargados correctamente
**Solución**:
1. Verificar que existan autorizaciones: `mysql -u root -p -e "USE sigvip_db; SELECT * FROM autorizaciones;"`
2. Si está vacía, recargar datos de prueba:
   ```bash
   mysql -u root -p sigvip_db < database/datos_de_prueba.sql
   ```

## 🔑 Funcionalidades Implementadas

### ⭐ RF003/RF004: Control de Acceso

**Menú**: `Control de Acceso → Control de Ingreso/Egreso`

#### Ingreso de Visita (RF003)
Validación automática de 6 pasos críticos:
1. ✅ Visitante existe y estado = ACTIVO
2. ✅ Autorización vigente (estado VIGENTE, no vencida)
3. ✅ Sin restricciones activas para el visitante
4. ✅ Horario dentro del schedule del establecimiento
5. ✅ Interno disponible para recibir visitas
6. ✅ Visitante no tiene otra visita EN_CURSO

**Cómo probar**:
1. Ingresar DNI de visitante: `33333333` (Ana García - en datos de prueba)
2. Seleccionar interno: `María Fernández (Legajo: 1002)`
3. Clic en "Registrar Ingreso"
4. Ver actualización en tabla de visitas activas

#### Egreso de Visita (RF004)
- Seleccionar visita de la tabla de "Visitas en Curso"
- Clic en "Registrar Egreso"
- Se actualiza hora_egreso y estado a FINALIZADA

### ✅ RF001: Registrar Visitante

**Menú**: `Visitantes → Registrar Visitante`

- Formulario completo: DNI, nombre, apellido, fecha de nacimiento, domicilio, teléfono, email
- Validación de DNI único (previene duplicados)
- Validación de edad >= 18 años
- Carga de foto (opcional, almacenada como BLOB)
- Estados: ACTIVO, SUSPENDIDO, INACTIVO

**Cómo probar**:
1. Ir a "Registrar Visitante"
2. Llenar formulario con DNI nuevo (ej: 45678901)
3. Verificar validaciones en tiempo real
4. Clic en "Guardar Visitante"

### ✅ RF002: Autorizar Visita

**Menú**: `Autorizaciones → Nueva Autorización`

- Búsqueda de visitante por DNI
- Búsqueda de interno por número de legajo
- Selección de tipo de relación (PADRE, MADRE, HIJO_A, HERMANO_A, CONYUGE, CONCUBINO_A, AMIGO, FAMILIAR, ABOGADO, OTRO)
- Configuración de fecha de vencimiento (opcional - NULL = indefinida)
- Gestión de estados: VIGENTE, SUSPENDIDA, REVOCADA, VENCIDA
- Acciones: Suspender, Revocar, Renovar, Reactivar autorizaciones
- Tabla con filtros por estado
- Validación de duplicados (constraint UNIQUE en BD)

**Cómo probar**:
1. Ir a "Autorizaciones → Nueva Autorización"
2. Buscar visitante por DNI: `33333333`
3. Buscar interno por legajo: `1002`
4. Seleccionar tipo de relación y crear autorización

### ✅ RF006: Gestionar Internos

**Menú**: `Internos → Gestión de Internos`

- Registro completo de internos (legajo único, datos personales, situación procesal)
- Búsqueda por legajo y DNI
- Gestión de ubicación (pabellón, piso)
- Control de estados: ACTIVO, TRASLADADO, EGRESADO
- Situación procesal: PROCESADO, CONDENADO
- Acciones: Actualizar ubicación, cambiar estado, registrar traslado
- Filtros por estado y situación procesal
- Auditoría completa de cambios en observaciones

**Cómo probar**:
1. Ir a "Internos → Gestión de Internos"
2. Registrar nuevo interno con legajo único
3. Modificar ubicación (pabellón/piso)
4. Registrar traslado a otro establecimiento

### ✅ RF007: Generar Reportes

**Menú**: `Reportes → Generar Reportes`

- Reportes en formato HTML (cumple con restricciones académicas - sin frameworks externos)
- Tipos de reporte: Visitas por fecha, por visitante, por interno, estadísticas, restricciones activas, autorizaciones vigentes
- Filtros: rango de fechas, visitante específico, interno específico
- Persistencia en base de datos (tabla reportes_generados)
- Visualización en navegador predeterminado
- Exportación de historial de reportes generados

**Cómo probar**:
1. Ir a "Reportes → Generar Reportes"
2. Seleccionar tipo de reporte
3. Configurar filtros
4. Generar y visualizar reporte HTML

### ✅ RF008: Gestionar Usuarios

**Menú**: `Administración → Gestión de Usuarios` (solo ADMINISTRADOR)

- Crear nuevos usuarios con hash SHA-256 automático
- Modificar datos de usuarios existentes
- Activar/Inactivar usuarios
- Restablecer contraseñas (sin requerir contraseña actual)
- Búsqueda por nombre de usuario
- Filtros por rol (OPERADOR, SUPERVISOR, ADMINISTRADOR) y estado (Activo/Inactivo)
- Validación de nombre de usuario único
- Contraseña mínimo 8 caracteres
- Seguridad triple capa: menú + vista + controlador
- Solo usuarios con rol ADMINISTRADOR pueden gestionar usuarios

**Cómo probar**:
1. Iniciar sesión como `admin` (Admin123!)
2. Ir a "Administración → Gestión de Usuarios (RF008)"
3. Completar formulario para crear nuevo usuario
4. Asignar rol y establecimiento
5. Guardar usuario (contraseña se hashea automáticamente)

### ⚙️ Otras Funcionalidades

- ✅ Login con autenticación SHA-256
- ✅ Menú principal con navegación por roles
- ✅ Tabla de visitas en curso con actualización manual (botón "Actualizar")
- ✅ Registro de auditoría automático en base de datos

## 📊 Base de Datos

**9 tablas normalizadas a 3NF**:

- `visitantes` - Registro de visitantes
- `internos` - Registro de internos
- `usuarios` - Usuarios del sistema
- `establecimientos` - Centros penitenciarios
- `autorizaciones` - Permisos visitante-interno
- `visitas` - Eventos de visita
- `restricciones` - Restricciones de acceso
- `auditoria` - Log completo de operaciones
- `reportes_generados` - Persistencia de reportes HTML (RF007)

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
│   ├── modelo/                        # 8 entidades + 11 enums
│   ├── vista/                         # 8 vistas Swing
│   ├── controlador/                   # 6 controladores MVC
│   ├── persistencia/                  # 8 DAOs + ConexionBD
│   └── utilidades/                    # Validadores + servicios
├── resources/
│   └── config.properties              # Configuración BD
├── lib/
│   └── mysql-connector-j-9.4.0.jar    # Driver JDBC
├── database/
│   ├── sigvip_db.sql                  # Schema
│   ├── datos_de_prueba.sql            # Datos iniciales
│   └── consultas_sql_prueba.sql       # Queries de referencia
└── README.md                          # Este archivo
```

## 🚀 Extensiones Opcionales

Posibles mejoras para versiones futuras:

- Testing automatizado con JUnit
- Optimizaciones de rendimiento y paginación
- RF009: Gestión de Restricciones
- RF010: Validación de Disponibilidad de Internos

## 📞 Soporte

Para consultas sobre el proyecto académico:
- Repositorio: https://github.com/JIATech/sigvip.git

## 📄 Licencia

Proyecto académico - Universidad Siglo 21 © 2025
