# =============================================================================
# ETAPA 1: BUILD
# =============================================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar solo los archivos de configuración de Maven primero
# Esto permite aprovechar el caché de Docker si las dependencias no cambian
COPY pom.xml .
COPY src ./src

# Compilar la aplicación (skip tests para build de imagen, ejecutar tests en CI)
RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true -B -V

# =============================================================================
# ETAPA 2: RUNTIME
# =============================================================================
FROM eclipse-temurin:17-jre-alpine

# Metadata del contenedor
LABEL maintainer="bike-team@bike.com"
LABEL version="1.0.0"
LABEL description="Microservicio de Autenticación - Bike Ecosystem"

WORKDIR /app

# Crear usuario no-root por seguridad (best practice)
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copiar el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar propietario al usuario no-root
RUN chown -R appuser:appgroup /app

# Cambiar a usuario no-root
USER appuser

# Exponer el puerto del servicio
EXPOSE 8084

# Configurar JVM para contenedores (optimización de memoria)
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Health check para Kubernetes / Docker Swarm
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Entrada del contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]
