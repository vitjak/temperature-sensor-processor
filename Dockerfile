FROM openjdk:17-jdk-slim AS build
WORKDIR /
COPY pom.xml .
COPY mvnw ./
RUN chmod +x mvnw
COPY .mvn/wrapper .mvn/wrapper
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean install -DskipTests


FROM openjdk:17-jdk-slim
WORKDIR /
COPY --from=build target/message-consumer-0.0.1-SNAPSHOT.jar message-consumer.jar
ENTRYPOINT ["java", "-jar", "message-consumer.jar"]
