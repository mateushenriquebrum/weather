
package brum.mateus.weather.sensors.domain;

import brum.mateus.weather.sensors.web.Preconditions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ReadingService {

    private final ReadingRepository readings;

    public ReadingService(ReadingRepository readings) {
        this.readings = readings;
    }

    @Transactional
    public Reading create(@NotBlank String sensor,
                          @NotNull Instant timestamp,
                          @NotNull float temperature,
                          @NotNull float humidity,
                          @NotNull float wind) {

        Reading reading = new Reading(
                sensor,
                timestamp,
                temperature,
                humidity,
                wind
        );

        return readings.save(reading);
    }

    @Transactional(readOnly = true)
    public Projection averageAcrossReading(
            String metric,
            Instant from,
            Instant to) {

        Preconditions.metrics(metric);
        Preconditions.range(from, to);

        return readings.averageAcrossSensors(metric, from, to);
    }

    @Transactional(readOnly = true)
    public Projection averageForReading(
            String sensor,
            String metric,
            Instant from,
            Instant to) {

        Preconditions.metrics(metric);
        Preconditions.range(from, to);

        return readings.averageForSensor(sensor, metric, from, to);
    }
}
