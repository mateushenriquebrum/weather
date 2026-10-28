package brum.mateus.weather.sensors.web;

import brum.mateus.weather.sensors.domain.Projection;
import brum.mateus.weather.sensors.domain.Reading;
import brum.mateus.weather.sensors.domain.ReadingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

@RestController
@Validated
@RequestMapping("/api/v1/sensors")
public class ReadingController {

    private final ReadingService sensors;

    public ReadingController(ReadingService sensors) {
        this.sensors = sensors;
    }

    @PostMapping("/readings")
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingResponse createReading(
            @Valid @RequestBody CreateReadingRequest request) {

        Reading domain = sensors.create(
                request.sensor(),
                request.at(),
                request.metrics().temperature(),
                request.metrics().humidity(),
                request.metrics().wind());

        return new ReadingController.ReadingResponse(
                domain.id().toString(),
                domain.sensor(),
                domain.at(),
                new Metrics(domain.temperature(), domain.humidity(), domain.wind())
        );
    }

    @GetMapping("/readings/average")
    public AverageResponse averageAcrossSensors(
            @RequestParam @NotBlank String metric,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) Instant to) {

        Preconditions.metrics(metric);
        Preconditions.range(from, to);

        Projection projection = sensors.averageAcrossReading(metric, from, to);

        return new AverageResponse(
                metric,
                "*",
                projection.average(),
                projection.count(),
                from,
                to
        );
    }

    @GetMapping("/{sensor}/readings/average")
    public AverageResponse averageForSensor(
            @PathVariable @NotBlank String sensor,
            @RequestParam @NotBlank String metric,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DATE_TIME) Instant to) {

        Preconditions.metrics(metric);
        Preconditions.range(from, to);

        Projection projection = sensors.averageForReading(sensor, metric, from, to);

        return new AverageResponse(
                metric,
                sensor,
                projection.average(),
                projection.count(),
                from,
                to
        );
    }

    public record CreateReadingRequest(
            @NotBlank String sensor,
            @NotNull Instant at,
            @NotNull @Valid Metrics metrics) {
    }

    public record Metrics(
            @NotNull Float temperature,
            @NotNull Float humidity,
            @NotNull Float wind) {
    }

    public record ReadingResponse(
            String id,
            String sensor,
            Instant at,
            Metrics metrics) {
    }

    public record AverageResponse(
            String metric,
            String sensor,
            Double average,
            Long count,
            Instant from,
            Instant to) {
    }
}