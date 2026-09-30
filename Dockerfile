# syntax=docker/dockerfile:1.7

# Build with the Gradle wrapper so Docker uses exactly the Gradle version the project pins.
# Tests do not run here: CI runs `./gradlew check` before it builds this image.
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /workspace

COPY gradlew build.gradle.kts settings.gradle.kts gradle.properties ./
COPY gradle ./gradle
COPY config ./config
# Warm the Gradle distribution and dependency cache in a layer that only changes with the build files.
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies --quiet > /dev/null

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:25-jre-alpine AS runtime
RUN addgroup -S app && adduser -S -G app app
WORKDIR /app
COPY --from=build --chown=app:app /workspace/build/libs/*.jar /app/app.jar
USER app
# Size the heap from the container's memory limit instead of the host's.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8080/api/openapi || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
