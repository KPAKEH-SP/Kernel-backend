FROM openjdk:21-jdk
COPY target/KernelUsers-0.0.1.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]