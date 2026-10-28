# ---------- Etapa 1: construir el .jar ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# ---------- Etapa 2: ejecutar la app ----------
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/boleta-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]