package com.assignment.consumer.kafka.data;

import lombok.Data;

@Data
public class TemperatureMessage {
    private String longitude;
    private String latitude;
    private int elevation;
    private long timestamp;
    private float temperature;
}
