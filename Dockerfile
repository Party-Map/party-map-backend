FROM gradle:8.10-jdk17 AS builder
WORKDIR /home/gradle/src
COPY . .
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:17-jre
VOLUME /tmp
EXPOSE 8080
ARG JAR_FILE=/home/gradle/src/build/libs/*.jar
COPY --from=builder ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]