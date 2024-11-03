package com.assignment.consumer;

import com.assignment.consumer.config.AppConfig;
import com.assignment.consumer.kafka.data.TemperatureMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TemperatureDataConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemperatureDataConsumer.class);
    private static final ConcurrentHashMap<String, TemperatureMessage> latestTemperatures = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Counter successfulMessagesCounter;
    private final Counter failedMessagesCounter;
    private final ExecutorService executorService;

    @Autowired
    public TemperatureDataConsumer(MeterRegistry meterRegistry, AppConfig appConfig) {
        this.executorService = Executors.newFixedThreadPool(appConfig.getNumberOfThreads());
        successfulMessagesCounter = meterRegistry.counter("temperature_messages_success_total", "type", "success");
        failedMessagesCounter = meterRegistry.counter("temperature_messages_failures_total", "type", "failure");
    }

    @KafkaListener(topics = "temperatures", groupId = "temperatures-group", containerFactory = "kafkaListenerContainerFactory")
    public void processTemperature(String message) {
        executorService.submit(() -> handleMessage(message));
    }

    public void handleMessage(String message) {
        try {
            TemperatureMessage newMessage = objectMapper.readValue(message, TemperatureMessage.class);
            String sensorId = newMessage.getLongitude() + "|" + newMessage.getLatitude() + "|" + newMessage.getElevation();

            latestTemperatures.compute(sensorId, (key, currentMessage) -> {
                if (currentMessage == null) {
                    LOGGER.atInfo()
                            .addKeyValue("sensorId", sensorId)
                            .addKeyValue("newMessage", newMessage)
                            .log("New temperature added for sensor " + sensorId + ": " + newMessage.getTemperature());
                    return newMessage;
                }

                if (newMessage.getTimestamp() > currentMessage.getTimestamp()
                        && newMessage.getTemperature() != currentMessage.getTemperature()) {

                    LOGGER.atInfo()
                            .addKeyValue("sensorId", sensorId)
                            .addKeyValue("newMessage", newMessage)
                            .log("Updated temperature for sensor " + sensorId + ": " + newMessage.getTemperature());
                    return newMessage;
                }

                successfulMessagesCounter.increment();
                LOGGER.atDebug()
                        .addKeyValue("sensorId", sensorId)
                        .addKeyValue("currentMessage", currentMessage)
                        .log("No update for sensor " + sensorId + ": temperature or timestamp did not change.");
                return currentMessage;
            });


        } catch (Exception e) {
            failedMessagesCounter.increment();
            LOGGER.atError()
                    .addKeyValue("rawMessage", message)
                    .addKeyValue("exception", e.getClass().getSimpleName())
                    .log("Error processing message: " + e.getMessage(), e);
        }
    }

    public ConcurrentHashMap<String, TemperatureMessage> getLatestTemperatures() {
        return latestTemperatures;
    }
}
