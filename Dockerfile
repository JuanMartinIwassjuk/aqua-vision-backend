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
# 2) Runtime Stage
# =====================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el .jar generado desde el stage de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto de la aplicación (Render lo detecta, pero es bueno declararlo)
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
