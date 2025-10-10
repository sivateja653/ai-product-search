# ---------- Build Stage ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom and resolve deps first (cache optimization)
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy the source and build
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=build /app/target/search-service-0.0.1-SNAPSHOT.jar app.jar

# Use non-root user (good practice)
RUN useradd -u 1001 spring
USER spring

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]