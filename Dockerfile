# syntax=docker/dockerfile:1.7

# Build with the Gradle wrapper so Docker uses exactly the Gradle version the project pins.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY gradle ./gradle
# Warm the Gradle distribution and dependency cache in a layer that only changes with the build files.
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies --quiet > /dev/null

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
