# 🔐 Auth Service - Bike Ecosystem

Microservicio de autenticación e identidad para el ecosistema Bike, implementado con **Java 17**, **Spring Boot 3**, **PostgreSQL** y arquitectura **Hexagonal + DDD**.

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Ejemplos de Uso](#-ejemplos-de-uso)
- [Variables de Entorno](#-variables-de-entorno)
- [Comandos Útiles](#-comandos-útiles)
- [Seguridad](#-seguridad)
- [Tests](#-tests)
- [Producción](#-producción)
- [Solución de Problemas](#-solución-de-problemas)

---

## 📖 Descripción

Este microservicio gestiona toda la autenticación y autorización del ecosistema Bike, proporcionando:

- ✅ Registro de usuarios con email/password
- ✅ Login tradicional con credenciales
- ✅ Login social (Google, Facebook, Apple, Instagram)
- ✅ JWT Access Tokens (vida corta: 15 min)
- ✅ JWT Refresh Tokens (vida larga: 7 días, con rotación)
- ✅ Logout y revocación de tokens
- ✅ Auditoría de eventos de seguridad

---

## 🏗️ Arquitectura

El servicio sigue los principios de **Arquitectura Hexagonal (Ports & Adapters)** combinados con **Domain-Driven Design (DDD)**:

```
┌─────────────────────────────────────────────────────────────────┐
│                          API Gateway                            │
│                    (valida JWT con JWKS)                        │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ Auth Service (:8084)                                            │
│ ┌─────────────┐ ┌───────────────┐ ┌─────────────────────────┐   │
│ │ Controllers │ │ Use Cases     │ │ Domain Models           │   │
│ │ (API Layer) │ │ (Application) │ │ (Pure Business Logic)   │   │
│ └─────────────┘ └───────────────┘ └─────────────────────────┘   │
│ ┌─────────────┐ ┌─────────────┐ ┌─────────────────────────┐     │
│ │ Security    │ │ Mappers     │ │ Repositories (Ports)    │     │
│ │ (JWT/OAuth) │ │ (MapStruct) │ │ (Interfaces)            │     │
│ └─────────────┘ └─────────────┘ └─────────────────────────┘     │
└─────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│ PostgreSQL (:5433)                                              │
│ ┌───────────┐ ┌─────────────┐ ┌─────────────────────────┐       │
│ │ users     │ │ credentials │ │ refresh_tokens          │       │
│ │social_... │ │ audit_logs  │ │ flyway_schema_history   │       │
│ └───────────┘ └─────────────┘ └─────────────────────────┘       │
└─────────────────────────────────────────────────────────────────┘
```


### Capas

| Capa | Paquete | Responsabilidad |
|------|---------|----------------|
| **Domain** | `com.bikefinder.auth.domain` | Entidades, Value Objects, Reglas de negocio puras |
| **Application** | `com.bikefinder.auth.application` | Casos de Uso, Ports (interfaces) |
| **Infrastructure** | `com.bikefinder.auth.infrastructure` | Implementaciones JPA, Security, JWT |
| **API** | `com.bikefinder.auth.api` | Controllers REST, DTOs, Exception Handlers |

---

## 🛠️ Tecnologías

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 17 | Lenguaje principal |
| Spring Boot | 3.2.0 | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| Spring Data JPA | 3.x | Persistencia de datos |
| PostgreSQL | 16 | Base de datos |
| Flyway | 10.x | Migraciones de BD |
| MapStruct | 1.5.5 | Mapeo DTO ↔ Entity |
| Lombok | 1.18.30 | Reducción de boilerplate |
| JWT (io.jsonwebtoken) | 0.12.3 | Tokens de acceso |
| Docker | 24.x | Containerización |
| Make | 3.81+ | Automatización de comandos |

---

## 📦 Requisitos Previos

| Herramienta | Versión Mínima | Instalación |
|-------------|---------------|-------------|
| Java JDK | 17 | [Eclipse Temurin](https://adoptium.net/) |
| Maven | 3.9.x | [Maven Download](https://maven.apache.org/) |
| Docker | 24.x | [Docker Desktop](https://www.docker.com/) |
| Make | 3.81+ | Pre-instalado en Linux/Mac, [GnuWin32](http://gnuwin32.sourceforge.net/packages/make.htm) en Windows |
| PostgreSQL | 16 | Dockerizado (incluido) |

---

## 📁 Estructura del Proyecto

```
auth/
├── .env # Variables de entorno (no commitear)
├── .gitignore
├── .dockerignore
├── docker-compose.yml # Orquestación de contenedores
├── Dockerfile # Build de la imagen Docker
├── Makefile # Comandos automatizados
├── pom.xml # Dependencias Maven
├── certs/ # Claves JWT (private.pem, public.pem)
├── scripts/
│ └── generate-jwt-keys.sh # Generar claves RSA
└── src/
├── main/
│ ├── java/com/bikefinder/auth/
│ │ ├── AuthServiceApplication.java
│ │ ├── api/ # Controllers, DTOs, Handlers
│ │ ├── application/ # Use Cases, Ports, Commands
│ │ ├── domain/ # Entidades, Value Objects
│ │ └── infrastructure/ # JPA, Security, JWT
│ └── resources/
│ ├── application.properties
│ ├── certs/ # Claves JWT (desarrollo)
│ └── db/migration/ # Scripts Flyway (V23__*.sql)
└── test/
```

---

## ⚙️ Configuración

### 1. Generar Claves JWT

```bash
# Ejecutar script de generación
chmod +x scripts/generate-jwt-keys.sh
./scripts/generate-jwt-keys.sh

# Verificar archivos generados
ls -la certs/
# private.pem (600 - solo lectura dueño)
# public.pem (644 - lectura pública)
```


## 2. Configurar Variables de Entorno
   Crear archivo .env en la raíz del proyecto:

# Database
DB_USERNAME=auth_user
DB_PASSWORD=auth_secure_password_123

# OAuth2 (obtener de Google Cloud / Meta Developers)
GOOGLE_CLIENT_ID=tu_google_client_id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=tu_google_client_secret

FACEBOOK_CLIENT_ID=tu_facebook_app_id
FACEBOOK_CLIENT_SECRET=tu_facebook_app_secret

# Spring Profiles
SPRING_PROFILES_ACTIVE=dev

# Logging
LOGGING_LEVEL=INFO
LOGGING_LEVEL_COM_BIKE_AUTH=DEBUG

## 3. Configurar application.properties

```bash
### Puerto del servicio
server.port=8084

### Base de datos
spring.datasource.url=jdbc:postgresql://localhost:5433/auth_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

### JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

### JWT
auth.jwt.private-key-path=classpath:certs/private.pem
auth.jwt.public-key-path=classpath:certs/public.pem
auth.jwt.expiration-ms=900000
auth.jwt.refresh-expiration-ms=604800000
auth.jwt.issuer=auth-service
auth.jwt.audience=bike-ecosystem

# Swagger
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### 🚀 Ejecución
### Opción A: Local (sin Docker)

```bash
# 1. Asegurar que PostgreSQL esté corriendo (puerto 5433)
docker-compose up -d auth-db

# 2. Ejecutar migraciones
make migrate

# 3. Compilar y ejecutar
mvn clean compile
mvn spring-boot:run

# 4. Verificar
curl http://localhost:8084/actuator/health
```

### Opción B: Docker Compose    
```bash
# 1. Construir imagen
docker-compose build

# 2. Levantar servicios
docker-compose up -d

# 3. Ver logs
docker-compose logs -f auth-service

# 4. Detener
docker-compose down
```

### Opción C: Makefile (Recomendado)
```bash
# Ver ayuda
make help

# Levantar base de datos
make up

# Ejecutar migraciones
make migrate

# Ver logs
make logs

# Conectarse a la BD
make shell

# Reset completo (cuidado: borra datos)
make reset

# Limpiar todo
make clean
```

### Endpoints de la API
### Autenticación

| Método | Endpoint | Descripción                                                                                          | Auth |
|-------------|--------|------------------------------------------------------------------------------------------------------|-------------|
 |POST | /auth/register | Registrar nuevo usuario con email/password | ❌ |
 |POST | /auth/login | Login con email/password | ❌ |  
 | POST| /auth/refresh |Renovar access token | ❌ |
 |POST | /auth/logout | Cerrar sesión | ✅|
 | GET | /auth/me| Obtener perfil del usuario| ✅| 

### OAuth2 / SSO

| Método | Endpoint | Descripción                                                                                          | Auth |
|-------------|--------|------------------------------------------------------------------------------------------------------|-------------|
| GET | /oauth2/authorization/google| Iniciar login con Google | ❌
|GET | /oauth2/authorization/facebook | Iniciar login con Facebook | ❌
| GET | /auth/oauth2/success | Callback OAuth2 exitoso | ❌


### Infraestructura

| Método | Endpoint | Descripción                                                                                          | Auth |
|-------------|--------|------------------------------------------------------------------------------------------------------|-------------|
|GET | /.well-known/jwks.json | Clave pública para validar JWT | ❌
| GET | /swagger-ui.html | Documentación interactiva | ❌
| GET | /actuator/health | Health check | ❌
| GET | /actuator/info | Información del servicio| ❌


## Ejemplos de Uso

### 1. Registrar Usuario

``` bash 
curl -X POST http://localhost:8084/auth/register \
-H "Content-Type: application/json" \
-d '{
"email": "usuario@bike.com",
"password": "SecurePass123!",
"fullName": "Juan Pérez"
}'
```

Respuesta:  
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "xxx-xxx-xxx",
  "tokenType": "Bearer",
  "expiresIn": 900000,
  "expiresAt": "2026-02-17T06:00:00Z",
  "user": {
    "id": "uuid-del-usuario",
    "email": "usuario@bike.com",
    "fullName": "Juan Pérez",
    "avatarUrl": null
  }
} 
```

### 2. Login
```bash    
curl -X POST http://localhost:8084/auth/login \
-H "Content-Type: application/json" \
-d '{
"email": "usuario@bike.com",
"password": "SecurePass123!"
}'
```

### 3. Refresh Token

```
   curl -X POST http://localhost:8084/auth/refresh \
   -H "Content-Type: application/json" \
   -d '{
   "refreshToken": "xxx-xxx-xxx"
   }'
```

### 4. Endpoint Protegido

```
curl -X GET http://localhost:8084/auth/me \
-H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..."
```

### 5. Logout

``` 
curl -X POST http://localhost:8084/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "xxx-xxx-xxx"
  }'
```

### Variables de Entorno


| Variable | Descripción | Default                                    | Requerida |
|-------------|--------|--------------------------------------------|-------------|
|POST | /auth/register | Registrar nuevo usuario con email/password | ❌ |
|DB_USERNAME|	Usuario de PostgreSQL| 	auth_user                                 |	✅|
|DB_PASSWORD|	Contraseña de PostgreSQL| 	-                                         |	✅|
|GOOGLE_CLIENT_ID|	Client ID de Google OAuth2| 	-                                         |	❌|
|GOOGLE_CLIENT_SECRET|	Client Secret de Google OAuth2| 	-                                         | 	❌                                         |
|FACEBOOK_CLIENT_ID|	App ID de Facebook OAuth2| 	-                                         |	❌|
|FACEBOOK_CLIENT_SECRET|	App Secret de Facebook OAuth2| 	-                                         |	❌|
|SPRING_PROFILES_ACTIVE|	Perfil de Spring| 	dev                                       | 	❌                                         |
|LOGGING_LEVEL|	Nivel de log root| 	INFO                                      | 	❌                                         |
|LOGGING_LEVEL_COM_BIKE_AUTH|	Nivel de log del servicio|	DEBUG|	❌|


### Comandos Útiles

### Makefile


| Comando | Descripción |
|-------------|--------|
|make help | Mostrar ayuda de comandos |
|make up|	Levantar base de datos|
|make down|	Detener servicios|
|make migrate|	Ejecutar migraciones Flyway|
|make status|	Ver estado de migraciones|
|make logs|	Ver logs en tiempo real|
|make shell|	Conectarse a la BD (psql)|
|make clean|	Eliminar contenedores y volúmenes|
|make reset|	Limpiar y migrar desde cero|


### Docker

```
# Construir imagen
docker-compose build

# Levantar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f auth-service

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v

# Ejecutar tests en contenedor
docker-compose run --rm auth-service mvn test
```

### Maven

```
# Limpiar y compilar
mvn clean compile

# Ejecutar tests
mvn test

# Empaquetar JAR
mvn package -DskipTests

# Ejecutar aplicación
mvn spring-boot:run

# Ver dependencias
mvn dependency:tree
```

### 🔒 Seguridad

### Mejores Prácticas Implementadas


| Práctica | Implementación |
|-------------|--------|
|Passwords | BCrypt con cost factor 12 |
|JWT | Firma RSA-256 asimétrica |
|Refresh Tokens | Hash en BD, rotación, revocación |
|Rate Limiting | 5 intentos fallidos → 30 min |
|Bloqueo de Cuenta | 5 intentos fallidos → 30 min |
|Auditoría | Logs de todos los eventos de auth |
|HTTPS | Requerido en producción |
|Secrets | Variables de entorno (no hardcodear) |
		
	

### Claves JWT

```
# Generar nuevas claves (rotación)
./scripts/generate-jwt-keys.sh

# Verificar clave pública
openssl rsa -in certs/public.pem -pubin -text -noout

# NUNCA commitear private.pem
echo "certs/private.pem" >> .gitignore
```

Headers de Seguridad (Producción)
Agregar en el Gateway o Load Balancer:

```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
X-XSS-Protection: 1; mode=block
Content-Security-Policy: default-src 'self' 
```


### 🧪 Tests
### Ejecutar Tests

```
# Todos los tests
mvn test

# Tests unitarios
mvn test -Dtest=*Test

# Tests de integración
mvn test -Dtest=*IT

# Con cobertura
mvn test jacoco:report
```

### 🚢 Producción
### Checklist de Producción


| Item | Estado | Notas  | 
|-------------|--------|-------| 
|SSL/TLS|	⬜	| HTTPS obligatorio|
|Secrets Management|	⬜|	Vault / K8s Secrets|
|Rate Limiting|	⬜|	En API Gateway|
|Monitoring	|⬜	|Prometheus + Grafana|
|Logging Centralizado|	⬜	|ELK / CloudWatch|
|Backup de BD	|⬜	|Automático diario|
|Health Checks	|⬜	|/actuator/health|
|Resource Limits|	⬜	|CPU/Memory en K8s|


### Kubernetes (Ejemplo)

```
apiVersion: apps/v1
kind: Deployment
metadata:
name: auth-service
spec:
replicas: 3
template:
spec:
containers:
- name: auth-service
image: bike/auth-service:1.0.0
ports:
- containerPort: 8084
resources:
limits:
memory: "512Mi"
cpu: "1000m"
requests:
memory: "256Mi"
cpu: "500m"
livenessProbe:
httpGet:
path: /actuator/health
port: 8084
initialDelaySeconds: 60
periodSeconds: 10
```

## Licencia

Proprietary - Bike Ecosystem © 2026


##🎯 Próximos Pasos

- Implementar OAuth2 completo (Google, Facebook, Apple)
- Agregar 2FA (TOTP)
- Verificación de email con tokens
- Rate limiting en Gateway
- Tests de carga con k6
- Documentación OpenAPI completa

