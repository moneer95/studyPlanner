# Build stage — Debian-based images support amd64 + arm64 (Apple Silicon)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package \
    && cp /app/target/smart-study-planner-*.jar /app/app.jar

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring \
    && mkdir -p /data && chown spring:spring /data

COPY --from=build /app/app.jar /app/app.jar

USER spring:spring

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
VOLUME ["/data"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
