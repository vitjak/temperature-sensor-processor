FROM openjdk:17-jdk-slim

WORKDIR .

COPY target/message-consumer-0.0.1-SNAPSHOT.jar message-consumer.jar

ENTRYPOINT ["java", "-jar", "message-consumer.jar"]
