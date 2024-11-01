package com.assignment.consumer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "consumer-app")
public class AppConfig {

    private String kafkaBootstrapServers;

    private int numberOfThreads;

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
        this.kafkaBootstrapServers = kafkaBootstrapServers;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}
