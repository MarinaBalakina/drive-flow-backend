FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .

ARG SERVICE
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew :${SERVICE}:bootJar --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app
ARG SERVICE
COPY --from=build /app/${SERVICE}/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]