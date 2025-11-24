# =====================
# 1) Build Stage (Compilación a Nativo)
# =====================
# Usar una imagen de GraalVM para la compilación nativa
FROM ghcr.io/graalvm/jdk:21 AS builder

# Crear directorio de trabajo
WORKDIR /app

# Copiar pom.xml y código fuente
COPY pom.xml .
COPY src ./src

# ⚠️ La línea clave: Compila la aplicación a un ejecutable nativo
# Usa -Pnative para activar el perfil de compilación de Spring Boot
RUN ./mvnw -Pnative clean package -DskipTests

# =====================
# 2) Runtime Stage (Lo más liviano posible)
# =====================
# Usar una imagen base estática, sin JRE, es lo más ligero que existe
FROM ghcr.io/distroless/static-debian12
# Alternativa minimalista si 'static' da problemas: FROM alpine/glibc:latest

# Definir directorio de trabajo
WORKDIR /app

# Copiar el ejecutable nativo desde el build (el nombre depende de tu artifactId)
COPY --from=builder /app/target/app-name /app/app-name

# Exponer el puerto
EXPOSE 8080

ENV TZ=America/Argentina/Buenos_Aires

# Ejecutar el binario nativo directamente
ENTRYPOINT ["/app/app-name", "--spring.profiles.active=prod"]