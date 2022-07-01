FROM openjdk:8-jre-slim-buster
WORKDIR /app
COPY target/publisher-1.0-SNAPSHOT.jar /app/
CMD ["java", "-jar", "./publisher-1.0-SNAPSHOT.jar"]