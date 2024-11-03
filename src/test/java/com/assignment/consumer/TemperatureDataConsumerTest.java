package com.assignment.consumer;

import com.assignment.consumer.config.AppConfig;
import com.assignment.consumer.kafka.data.TemperatureMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class TemperatureDataConsumerTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Counter successfulMessagesCounter;
    @Mock
    private Counter failedMessagesCounter;
    @Mock
    private MeterRegistry meterRegistry;
    @Mock
    private AppConfig appConfig;
    @Mock
    private TemperatureDataConsumer dataConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(appConfig.getNumberOfThreads()).thenReturn(1);
        when(meterRegistry.counter("temperature_messages_success_total", "type", "success"))
                .thenReturn(successfulMessagesCounter);
        when(meterRegistry.counter("temperature_messages_failures_total", "type", "failure"))
                .thenReturn(failedMessagesCounter);
        dataConsumer = new TemperatureDataConsumer(meterRegistry, appConfig);
        dataConsumer.getLatestTemperatures().clear();
    }

    //New message with sensorId that is not yet in map
    @Test
    void testHandleMessage_NewSensorId_ShouldAddNewMessage() throws Exception {
        String message = "{\"longitude\": \"46\\u00b004'53.6\\\"N\", \"latitude\": \"14\\u00b029'43.5\\\"E\", \"elevation\": 296, \"timestamp\": 1704067200000, \"temperature\": 2.22}";
        TemperatureMessage newMessage = new TemperatureMessage();
        newMessage.setLongitude("46°04'53.6\"N");
        newMessage.setLatitude("14°29'43.5\"E");
        newMessage.setElevation(296);
        newMessage.setTimestamp(1704067200000L);
        newMessage.setTemperature(2.22f);

        String sensorId = "46°04'53.6\"N|14°29'43.5\"E|296";

        when(objectMapper.readValue(message, TemperatureMessage.class)).thenReturn(newMessage);
        dataConsumer.handleMessage(message);

        assertTrue(dataConsumer.getLatestTemperatures().containsKey(sensorId));
        assertEquals(newMessage, dataConsumer.getLatestTemperatures().get(sensorId));
    }

    //Newer message with different temperature than current
    @Test
    void testHandleMessage_ExistingSensorId_ShouldUpdateTemperature() throws JsonProcessingException {
        TemperatureMessage currentMessage = new TemperatureMessage();
        currentMessage.setLongitude("46°04'53.6\"N");
        currentMessage.setLatitude("14°29'43.5\"E");
        currentMessage.setElevation(296);
        currentMessage.setTimestamp(1704067200000L);
        currentMessage.setTemperature(2.22f);

        String newMessageJson = "{\"longitude\": \"46\\u00b004'53.6\\\"N\", \"latitude\": \"14\\u00b029'43.5\\\"E\", \"elevation\": 296, \"timestamp\": 1704067299999, \"temperature\": 11.22}";
        TemperatureMessage newMessage = new TemperatureMessage();
        newMessage.setLongitude("46°04'53.6\"N");
        newMessage.setLatitude("14°29'43.5\"E");
        newMessage.setElevation(296);
        newMessage.setTimestamp(1704067299999L);
        newMessage.setTemperature(11.22f);

        String sensorId = "46°04'53.6\"N|14°29'43.5\"E|296";

        dataConsumer.getLatestTemperatures().put(sensorId, currentMessage);

        when(objectMapper.readValue(newMessageJson, TemperatureMessage.class)).thenReturn(newMessage);
        dataConsumer.handleMessage(newMessageJson);

        assertEquals(newMessage, dataConsumer.getLatestTemperatures().get(sensorId));
    }

    //Older message with different temperature than current
    @Test
    void testHandleMessage_ExistingSensorId_NoUpdateNeeded1() throws JsonProcessingException {
        TemperatureMessage currentMessage = new TemperatureMessage();
        currentMessage.setLongitude("46°04'53.6\"N");
        currentMessage.setLatitude("14°29'43.5\"E");
        currentMessage.setElevation(296);
        currentMessage.setTimestamp(1704067200000L);
        currentMessage.setTemperature(2.22f);

        String newMessageJson = "{\"longitude\": \"46\\u00b004'53.6\\\"N\", \"latitude\": \"14\\u00b029'43.5\\\"E\", \"elevation\": 296, \"timestamp\": 1604067200000, \"temperature\": 24.87}";
        TemperatureMessage newMessage = new TemperatureMessage();
        newMessage.setLongitude("46°04'53.6\"N");
        newMessage.setLatitude("14°29'43.5\"E");
        newMessage.setElevation(296);
        newMessage.setTimestamp(1604067200000L);
        newMessage.setTemperature(24.87f);

        String sensorId = "46°04'53.6\"N|14°29'43.5\"E|296";

        dataConsumer.getLatestTemperatures().put(sensorId, currentMessage);

        when(objectMapper.readValue(newMessageJson, TemperatureMessage.class)).thenReturn(newMessage);
        dataConsumer.handleMessage(newMessageJson);

        assertEquals(currentMessage, dataConsumer.getLatestTemperatures().get(sensorId));
    }

    //Newer message but same temperature than current
    @Test
    void testHandleMessage_ExistingSensorId_NoUpdateNeeded2() throws JsonProcessingException {
        TemperatureMessage currentMessage = new TemperatureMessage();
        currentMessage.setLongitude("46°04'53.6\"N");
        currentMessage.setLatitude("14°29'43.5\"E");
        currentMessage.setElevation(296);
        currentMessage.setTimestamp(1704067200000L);
        currentMessage.setTemperature(2.22f);

        String newMessageJson = "{\"longitude\": \"46\\u00b004'53.6\\\"N\", \"latitude\": \"14\\u00b029'43.5\\\"E\", \"elevation\": 296, \"timestamp\": 1704067999999, \"temperature\": 2.22}";
        TemperatureMessage newMessage = new TemperatureMessage();
        newMessage.setLongitude("46°04'53.6\"N");
        newMessage.setLatitude("14°29'43.5\"E");
        newMessage.setElevation(296);
        newMessage.setTimestamp(1704067299999L);
        newMessage.setTemperature(24.87f);

        String sensorId = "46°04'53.6\"N|14°29'43.5\"E|296";

        dataConsumer.getLatestTemperatures().put(sensorId, currentMessage);

        when(objectMapper.readValue(newMessageJson, TemperatureMessage.class)).thenReturn(newMessage);
        dataConsumer.handleMessage(newMessageJson);

        assertEquals(currentMessage, dataConsumer.getLatestTemperatures().get(sensorId));
    }
}
