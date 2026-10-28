
package brum.mateus.weather.sensors.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface ReadingRepository
        extends JpaRepository<Reading, UUID> {

    @Query("""
        SELECT AVG(
            CASE :metric
                WHEN 'temperature' THEN r.temperature
                WHEN 'humidity' THEN r.humidity
                WHEN 'wind' THEN r.wind
            END
        ) AS average,
        COUNT(r) AS count
        FROM Reading r
        WHERE r.at >= :from
          AND r.at < :to
        """)
    Projection averageAcrossSensors(
            @Param("metric") String metric,
            @Param("from") Instant from,
            @Param("to") Instant to);

    @Query("""
        SELECT AVG(
            CASE :metric
                WHEN 'temperature' THEN r.temperature
                WHEN 'humidity' THEN r.humidity
                WHEN 'wind' THEN r.wind
            END
        ) AS average,
        COUNT(r) AS count
        FROM Reading r
        WHERE r.sensor = :sensor
          AND r.at >= :from
          AND r.at < :to
        """)
    Projection averageForSensor(
            @Param("sensor") String sensor,
            @Param("metric") String metric,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
