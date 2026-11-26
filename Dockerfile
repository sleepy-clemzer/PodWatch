# syntax=docker/dockerfile:1
# Image unique PodWatch : frontend React embarqué dans le jar Spring Boot (ADR-008).
# Build : docker build -t podwatch:local .

# --- 1. Build du frontend -------------------------------------------------------------
FROM node:26-alpine AS frontend
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# --- 2. Build du backend (le frontend est copié dans classpath:/static) -----------------
FROM maven:3.9-eclipse-temurin-25 AS backend
WORKDIR /build/backend
COPY backend/pom.xml ./
# Couche de cache : les dépendances ne sont re-téléchargées que si pom.xml change.
RUN mvn -B -ntp dependency:go-offline
COPY backend/src ./src
COPY --from=frontend /build/frontend/dist ./src/main/resources/static
# Les tests sont exécutés par la CI (job backend), pas pendant la construction de l'image.
RUN mvn -B -ntp -DskipTests package

# --- 3. Image d'exécution ---------------------------------------------------------------
FROM eclipse-temurin:25-jre
# Utilisateur non-root avec UID numérique (compatible runAsNonRoot côté Kubernetes).
RUN groupadd --system --gid 10001 podwatch \
    && useradd --system --uid 10001 --gid podwatch --no-create-home --shell /usr/sbin/nologin podwatch
WORKDIR /app
COPY --from=backend /build/backend/target/podwatch-backend-*.jar /app/app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
