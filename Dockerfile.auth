FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

COPY pom.xml .
COPY auth-service ./auth-service

RUN mvn clean package -pl auth-service -am -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]