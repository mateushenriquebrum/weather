
package brum.mateus.weather.sensors.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sensor_readings")
public class Reading {

    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant at;

    @Column(nullable = false)
    private String sensor;

    @Column(nullable = false)
    private float temperature;

    @Column(nullable = false)
    private float humidity;

    @Column(nullable = false)
    private float wind;

    protected Reading() {
        // Required by JPA
    }

    public Reading(
            String sensor,
            Instant at,
            float temperature,
            float humidity,
            float wind) {

        Objects.requireNonNull(sensor);
        Objects.requireNonNull(at);

        this.id = UUID.randomUUID();
        this.sensor = sensor;
        this.at = at;
        this.temperature = temperature;
        this.humidity = humidity;
        this.wind = wind;
    }

    public UUID id() {
        return id;
    }

    public String sensor() {
        return sensor;
    }

    public Instant at() {
        return at;
    }

    public float temperature() {
        return temperature;
    }

    public float humidity() {
        return humidity;
    }

    public float wind() {
        return wind;
    }
}
