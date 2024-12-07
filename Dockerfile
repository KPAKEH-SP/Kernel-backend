FROM maven:3.9-eclipse-temurin-21 AS build
COPY . /app
WORKDIR /app
RUN mvn clean
RUN mvn package -X

FROM openjdk:21-jdk
COPY --from=build /app/target/*.jar /app.jar
CMD ["java", "-jar", "/app.jar"]