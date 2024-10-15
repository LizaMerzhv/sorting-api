FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/sort-api-3.3.2.jar /app/my-app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "my-app.jar"]
