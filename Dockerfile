# =============================================================================
# ETAPA 1: BUILD
# =============================================================================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -Dmaven.javadoc.skip=true -B -V

# =============================================================================
# ETAPA 2: RUNTIME
# =============================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="bike-team@bike.com"
LABEL version="1.0.0"
LABEL description="Microservicio de Autenticación - Bike Ecosystem"

WORKDIR /app

RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

COPY --from=build /app/target/*.jar app.jar

# NO copiar certs aquí - se montan como volumen

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8084

ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
