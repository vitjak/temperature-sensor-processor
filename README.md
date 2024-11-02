# Temperature Sensor Processor

## Overview

The Temperature Sensor Processor is a Kafka consumer application designed to process temperature data from various
sensors. It consumes messages from a Kafka topic with multiple partitions and updates the latest temperature readings
for each sensor based on the provided timestamp and temperature values.

To use application you can build image locally:
`docker build -t message-consumer .`

or just use image that is built via GitHub actions:
`docker pull ghcr.io/vitjak/message-consumer:latest`

## Configuration

The application uses two env variables as input:

- `KAFKA_BOOTSTRAP_SERVERS` which sets the IP of the Kafka instance the consumer will connect to
- `SPRING_PROFILES_ACTIVE` which sets the profile that is only used for log display (structured for 'prod' and plain
  console for 'dev')

## Running the Application

To run the application using Docker, use the following command, ensuring to set the necessary environment variables:
`docker run -e KAFKA_BOOTSTRAP_SERVERS=<your_kafka_ip> -e SPRING_PROFILES_ACTIVE=<profile> ghcr.io/vitjak/message-consumer:latest`

## Conclusion

To test locally I used a separate project which generated random messages and sent them to kafka topic with 96
partitions. Then I used `deployment.yaml` file to deploy the consumer in a Minikube instance.