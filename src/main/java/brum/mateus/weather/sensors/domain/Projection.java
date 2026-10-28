package brum.mateus.weather.sensors.domain;

public record Projection(
        Double average,
        Long count
) {}