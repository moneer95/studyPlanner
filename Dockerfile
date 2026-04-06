# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package \
    && cp /app/target/smart-study-planner-*.jar /app/app.jar

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /data && chown spring:spring /data

COPY --from=build /app/app.jar /app/app.jar

USER spring:spring

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080
VOLUME ["/data"]

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
