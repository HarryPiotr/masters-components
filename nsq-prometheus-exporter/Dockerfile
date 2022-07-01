FROM openjdk:8-jre-slim-buster
WORKDIR /app
COPY target/nsq-prometheus-exporter-1.0-SNAPSHOT.jar /app/
CMD ["java", "-jar", "./nsq-prometheus-exporter-1.0-SNAPSHOT.jar"]