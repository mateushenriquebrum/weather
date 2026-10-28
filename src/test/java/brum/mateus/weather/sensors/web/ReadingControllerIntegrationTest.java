
package brum.mateus.weather.sensors.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@SpringBootTest
@AutoConfigureMockMvc
class ReadingControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/sensors";

    private static final String FROM = "2026-10-24T00:00:00Z";
    private static final String TO = "2026-10-25T00:00:00Z";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanDatabase() {
        jdbcTemplate.execute("DELETE FROM sensor_readings");
    }

    @Test
    void cannotCreateReadingWithInvalidPayload() throws Exception {
        mvc.perform(post(BASE_URL + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "sensor": " ",
                              "at": "2026-10-24T10:00:00Z",
                              "metrics": {
                                "temperature": 18.5,
                                "humidity": 65.0,
                                "wind": 4.2
                              }
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void successfullyCreatesReading() throws Exception {
        mvc.perform(post(BASE_URL + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readingJson(
                                "sensor-a",
                                "2026-10-24T10:00:00Z",
                                18.5, 65.0, 4.2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.sensor", is("sensor-a")))
                .andExpect(jsonPath("$.metrics.temperature",
                        is(18.5)))
                .andExpect(jsonPath("$.metrics.humidity",
                        is(65.0)))
                .andExpect(jsonPath("$.metrics.wind",
                        is(4.2)));
    }

    @Test
    void fetchesAverageForASensor() throws Exception {
        createReading("sensor-a", "2026-10-24T10:00:00Z",
                10, 50, 2);
        createReading("sensor-a", "2026-10-24T11:00:00Z",
                20, 70, 4);
        createReading("sensor-b", "2026-10-24T12:00:00Z",
                100, 90, 8);

        mvc.perform(get(BASE_URL + "/sensor-a/readings/average")
                        .param("metric", "temperature")
                        .param("from", FROM)
                        .param("to", TO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensor", is("sensor-a")))
                .andExpect(jsonPath("$.metric", is("temperature")))
                .andExpect(jsonPath("$.average",
                        closeTo(15.0, 0.0001)))
                .andExpect(jsonPath("$.count", is(2)));
    }

    @Test
    void fetchesAverageAcrossAllSensors() throws Exception {
        createReading("sensor-a", "2026-10-24T10:00:00Z",
                10, 50, 2);
        createReading("sensor-a", "2026-10-24T11:00:00Z",
                20, 70, 4);
        createReading("sensor-b", "2026-10-24T12:00:00Z",
                30, 90, 6);

        mvc.perform(get(BASE_URL + "/readings/average")
                        .param("metric", "temperature")
                        .param("from", FROM)
                        .param("to", TO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensor", is("*")))
                .andExpect(jsonPath("$.average",
                        closeTo(20.0, 0.0001)))
                .andExpect(jsonPath("$.count", is(3)));
    }

    @Test
    void returnsNoReadingsForNonExistingSensor() throws Exception {
        mvc.perform(get(BASE_URL + "/missing-sensor/readings/average")
                        .param("metric", "temperature")
                        .param("from", FROM)
                        .param("to", TO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sensor", is("missing-sensor")))
                .andExpect(jsonPath("$.count", is(0)))
                .andExpect(jsonPath("$.average", nullValue()));
    }

    @Test
    void rejectsInvalidTimeRange() throws Exception {
        mvc.perform(get(BASE_URL + "/readings/average")
                        .param("metric", "temperature")
                        .param("from", "2026-10-25T12:00:00Z")
                        .param("to", "2026-10-25T10:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnsupportedMetric() throws Exception {
        mvc.perform(get(BASE_URL + "/readings/average")
                        .param("metric", "pressure")
                        .param("from", FROM)
                        .param("to", TO))
                .andExpect(status().isBadRequest());
    }

    private void createReading(
            String sensor,
            String at,
            double temperature,
            double humidity,
            double wind) throws Exception {

        mvc.perform(post(BASE_URL + "/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(readingJson(
                                sensor, at, temperature, humidity, wind)))
                .andExpect(status().isCreated());
    }

    private String readingJson(
            String sensor,
            String at,
            double temperature,
            double humidity,
            double wind) {

        return """
            {
              "sensor": "%s",
              "at": "%s",
              "metrics": {
                "temperature": %s,
                "humidity": %s,
                "wind": %s
              }
            }
            """.formatted(sensor, at, temperature, humidity, wind);
    }
}
