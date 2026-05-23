# Stage 1: build frontend (Vite outputs to ../src/main/resources/static)
FROM node:20-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build
# built files land at /app/src/main/resources/static/

# Stage 2: build backend
FROM eclipse-temurin:17-jdk AS backend
WORKDIR /app
COPY . .
COPY --from=frontend /app/src/main/resources/static/ src/main/resources/static/
RUN ./mvnw package -DskipTests

# Stage 3: runtime
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=backend /app/target/shop-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
