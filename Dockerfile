FROM gradle:9-jdk21-alpine AS builder
WORKDIR /home/gradle/src
COPY . .
RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=/home/gradle/src/build/libs/*.jar
COPY --from=builder ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]