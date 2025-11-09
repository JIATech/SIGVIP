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
- ✅ EntidadBase (clase abstracta base para todas las entidades - TP4)
- ✅ 8 entidades del modelo con lógica de negocio (Visitante, Interno, Visita, Autorizacion, Usuario, Restriccion, Establecimiento, ReporteGenerado)
- ✅ 11 enums para estados y tipos
- ✅ IBaseDAO<T> (interfaz genérica para todos los DAOs - TP4)
- ✅ 8 DAOs completos con JDBC (VisitanteDAO, InternoDAO, VisitaDAO, AutorizacionDAO, RestriccionDAO, UsuarioDAO, EstablecimientoDAO, ReporteDAO)
- ✅ 7 controladores MVC (Visitantes, Acceso, Reportes, Autorizaciones, Internos, Usuarios, Restricciones)
- ✅ Servicios de validación y utilidades (ValidadorDatos, ServicioValidacionSeguridad, GeneradorReportes, ServicioLogs, ServicioBackup)
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
- ✅ **VistaGestionRestricciones** (RF009) - Completa con alertas automáticas y modo offline

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

## Funcionalidades Implementadas

### RF003/RF004: Control de Acceso

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

### ✅ RF009: Registrar Restricciones

**Menú**: `Administración → Gestión de Restricciones` (ADMINISTRADOR y SUPERVISOR)

- Crear restricciones de acceso con alcance TODOS o INTERNO_ESPECIFICO
- Tipos de restricción: CONDUCTA, JUDICIAL, ADMINISTRATIVA, SEGURIDAD
- Motivo obligatorio (mínimo 10 caracteres)
- Fecha de inicio obligatoria
- Fecha de fin opcional (NULL = indefinida)
- Búsqueda de visitante por DNI
- Búsqueda de interno por legajo (solo para restricciones específicas)
- Acciones: Levantar restricción, Extender fecha, Eliminar (solo ADMINISTRADOR)
- Filtros: por estado (Todas/Activas/Inactivas/Próximas a vencer) y tipo
- Alertas automáticas: widget en header y notificación al login para restricciones que vencen en 7 días
- Integración con RF003: bloqueo automático de ingreso si hay restricciones activas
- Soporte completo de modo offline

**Cómo probar**:
1. Iniciar sesión como `admin` o `supervisor1`
2. Ir a "Administración → Gestión de Restricciones (RF009)"
3. Buscar visitante por DNI: `33333333`
4. Seleccionar tipo de restricción y alcance
5. Ingresar motivo y fechas
6. Crear restricción
7. Intentar registrar ingreso en Control de Acceso (debe bloquearse)

### ⚙️ Otras Funcionalidades

- ✅ Login con autenticación SHA-256
- ✅ Menú principal con navegación por roles
- ✅ Tabla de visitas en curso con actualización manual (botón "Actualizar")
- ✅ Registro de auditoría automático en base de datos

## 🔌 Modo Offline

SIGVIP incluye un **modo offline completo** que permite demostrar todas las funcionalidades sin conexión a MySQL.

### ¿Cuándo se activa?

- **Automáticamente** cuando MySQL no está disponible al iniciar la aplicación
- Se muestra un diálogo ofreciendo dos opciones:
  - **Modo Offline (Limitado)**: Continuar sin base de datos
  - **Reintentar Conexión**: Intentar conectar nuevamente
  - **Configurar Manualmente**: Ajustar parámetros de conexión

### Características del Modo Offline

✅ **Funcionalidades Disponibles** (8 de 10 RF):
- RF001: Registrar Visitante
- RF002: Autorizar Visita
- RF003: Controlar Ingreso
- RF004: Controlar Egreso
- RF005: Consultar Historial
- RF006: Gestionar Internos
- RF008: Gestionar Usuarios
- RF009: Registrar Restricciones

⚠️ **Funcionalidades Limitadas**:
- RF007: Generar Reportes - Los reportes HTML se generan correctamente pero NO se pueden guardar en base de datos

### Almacenamiento en Memoria

- **Datos volátiles**: Se almacenan en memoria RAM usando `RepositorioMemoria`
- **Thread-safe**: Usa `ConcurrentHashMap` y `AtomicLong` para IDs
- **Datos de prueba precargados**:
  - 10 visitantes predefinidos
  - 10 internos predefinidos
  - 3 usuarios (admin, operador1, supervisor1)
  - 1 establecimiento

⚠️ **ADVERTENCIA CRÍTICA**: Todos los datos creados en modo offline **se perderán al cerrar la aplicación**.

### Indicadores Visuales

Cuando está en modo offline, el sistema muestra:
- 🔴 **Banner naranja en todas las ventanas**: "⚠ MODO OFFLINE - Los datos se almacenan solo en memoria y se perderán al cerrar la aplicación"
- 🔴 **Título del menú principal**: Incluye `[MODO OFFLINE]`
- 🔴 **Botones deshabilitados**: Funciones incompatibles (ej: Guardar Reporte en BD)

### Usuarios de Prueba (Modo Offline)

| Usuario | Contraseña | Rol | Establecimiento |
|---------|-----------|-----|-----------------|
| `admin` | `Admin123!` | ADMINISTRADOR | Complejo Penitenciario Central |
| `operador1` | `Opera123!` | OPERADOR | Complejo Penitenciario Central |
| `supervisor1` | `Super123!` | SUPERVISOR | Complejo Penitenciario Central |

### Cómo Probar el Modo Offline

1. **Opción A - Detener MySQL**:
   ```bash
   # Windows
   net stop MySQL80

   # Linux/Mac
   sudo systemctl stop mysql
   ```

2. **Opción B - Configuración inválida**:
   - Modificar `resources/config.properties` con credenciales incorrectas
   - Cambiar el puerto a uno inválido

3. **Ejecutar la aplicación**:
   - Aparecerá el diálogo de conexión fallida
   - Seleccionar "Modo Offline (Limitado)"
   - Confirmar las advertencias
   - Login con usuarios predefinidos

4. **Probar funcionalidades**:
   - Todas las operaciones CRUD funcionan normalmente
   - Los datos se mantienen mientras la aplicación esté abierta
   - ⚠️ Al cerrar, todos los datos se pierden

### Arquitectura Técnica

```
DAO Layer
├── Verifica: GestorModo.isModoOffline()
├── Si OFFLINE → RepositorioMemoria
└── Si ONLINE  → MySQL con JDBC
```

Cada DAO implementa dual-mode:
```java
public Long insertar(Visitante visitante) throws SQLException {
    // MODO OFFLINE: Usar repositorio en memoria
    if (GestorModo.getInstancia().isModoOffline()) {
        return RepositorioMemoria.getInstancia().insertarVisitante(visitante);
    }

    // MODO ONLINE: MySQL con JDBC
    // ... código JDBC normal
}
```

### Limitaciones Conocidas

1. **Persistencia**: Los datos NO sobreviven al cierre de la aplicación
2. **Reportes**: Se generan pero no se guardan en BD (botón "Guardar HTML" deshabilitado)
3. **Auditoría**: No se registra en tabla `auditoria` (solo en modo online)
4. **Concurrencia**: No apto para múltiples instancias simultáneas

### Volver al Modo Online

1. Cerrar la aplicación
2. Iniciar MySQL
3. Re-ejecutar la aplicación
4. El sistema detectará MySQL y usará la base de datos normalmente

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
│   ├── Main.java                      # Punto de entrada de la aplicación
│   ├── modelo/
│   │   ├── EntidadBase.java           # Clase abstracta base (TP4)
│   │   ├── Visitante.java             # Entidad visitante
│   │   ├── Interno.java               # Entidad interno
│   │   ├── Visita.java                # Entidad visita
│   │   ├── Autorizacion.java          # Entidad autorización
│   │   ├── Usuario.java               # Entidad usuario
│   │   ├── Restriccion.java           # Entidad restricción
│   │   ├── Establecimiento.java       # Entidad establecimiento
│   │   ├── ReporteGenerado.java       # Entidad reporte
│   │   └── enums/                     # 11 enumeraciones de estados y tipos
│   ├── vista/                         # 9 interfaces Swing
│   │   ├── VistaLogin.java
│   │   ├── VistaMenuPrincipal.java
│   │   ├── VistaControlAcceso.java
│   │   ├── VistaRegistroVisitante.java
│   │   ├── VistaAutorizaciones.java
│   │   ├── VistaGestionInternos.java
│   │   ├── VistaReportes.java
│   │   ├── VistaGestionUsuarios.java
│   │   └── VistaGestionRestricciones.java
│   ├── controlador/                   # 7 controladores MVC
│   │   ├── ControladorAcceso.java
│   │   ├── ControladorVisitantes.java
│   │   ├── ControladorAutorizaciones.java
│   │   ├── ControladorInternos.java
│   │   ├── ControladorReportes.java
│   │   ├── ControladorUsuarios.java
│   │   └── ControladorRestricciones.java
│   ├── persistencia/                  # Capa de acceso a datos (parte del Modelo)
│   │   ├── IBaseDAO.java              # Interfaz genérica CRUD (TP4)
│   │   ├── ConexionBD.java            # Singleton para gestión de conexiones
│   │   ├── VisitanteDAO.java
│   │   ├── InternoDAO.java
│   │   ├── VisitaDAO.java
│   │   ├── AutorizacionDAO.java
│   │   ├── RestriccionDAO.java
│   │   ├── UsuarioDAO.java
│   │   ├── EstablecimientoDAO.java
│   │   ├── ReporteDAO.java
│   │   ├── RepositorioMemoria.java    # Repositorio en memoria (modo offline)
│   │   └── GestorModo.java            # Gestor de modo online/offline
│   └── utilidades/                    # Servicios y validadores
│       ├── ValidadorDatos.java        # Validaciones de formato
│       ├── ServicioValidacionSeguridad.java  # Validaciones RF003 (6 pasos)
│       ├── GeneradorReportes.java     # Generación de reportes HTML
│       ├── ServicioLogs.java          # Sistema de logging
│       └── ServicioBackup.java        # Servicio de backup (stub)
├── resources/
│   └── config.properties              # Configuración de base de datos
├── lib/
│   └── mysql-connector-j-9.4.0.jar    # Driver JDBC MySQL
├── database/
│   ├── sigvip_db.sql                  # Script de creación de schema
│   ├── datos_de_prueba.sql            # Datos iniciales de prueba
│   └── consultas_sql_prueba.sql       # Queries SQL de referencia
└── README.md                          # Este archivo
```

## 📄 Licencia

Proyecto académico - Universidad Siglo 21 © 2025
