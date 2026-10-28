package brum.mateus.weather.sensors.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Set;

public class Preconditions {
    public static void metrics(String metric) {
        Set<String> VALID_METRICS = Set.of("temperature", "humidity", "wind");
        if (!VALID_METRICS.contains(metric)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported metric: " + metric);
        }
    }
    public static void range(Instant from, Instant to) {
        if (!from.isBefore(to)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "'from' must be earlier than 'to'");
        }
    }
}
