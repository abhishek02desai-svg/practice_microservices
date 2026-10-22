# ---------- Stage 1: build the JAR with Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy only the pom first so dependencies are cached between builds
COPY pom.xml .
RUN echo "Maven dependencies will be resolved during packaging"

# Now copy the source code and build
COPY src ./src
RUN mvn -B clean package -Dmaven.test.skip=true

# ---------- Stage 2: small runtime image ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Run as a non-root user
RUN useradd --system --uid 1001 appuser
USER appuser

COPY --from=build /build/target/notification-service-1.0.0.jar app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
