# =====================
# 1) Build Stage
# =====================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Crear directorio de trabajo
WORKDIR /app

# Copiar pom.xml y descargar dependencias (para aprovechar cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# =====================
# 2) Runtime Stage (liviano y optimizado)
# =====================
FROM eclipse-temurin:21-jre-alpine

# Definir directorio de trabajo
WORKDIR /app

# Copiar el .jar desde el build
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto
EXPOSE 8080

ENV TZ=America/Argentina/Buenos_Aires

# Configuración JVM para entornos livianos
# - Usa SerialGC (menos threads, menos consumo)
# - Limita el uso de RAM al 60% del contenedor
# - Optimiza arranque y estabilidad en Render
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=60.0", \
    "-XX:+UseSerialGC", \
    "-XX:+AlwaysPreTouch", \
    "-jar", "app.jar", \
    "--spring.profiles.active=prod"]
