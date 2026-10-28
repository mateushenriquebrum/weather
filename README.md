# Weather Sensors API

## Running the Application

From the project root, run:

```bash
mvn spring-boot:run
```

The application will start at `http://localhost:8081`.



## Architecture

The application follows a simple layered architecture:

**Controller → Domain ← Repository**

- **Controller (`web`)** — Exposes REST endpoints, validates HTTP input, and maps requests and domain results to HTTP responses.
- **Domain (`domain`)** — Contains the `Reading` model and `ReadingService`, which coordinates reading creation and average calculations.
- **Repository (`domain`)** — Defines data access operations for persisting readings and querying averages.

The controller does not access the repository directly. The service separates HTTP concerns from persistence, keeping each layer easier to maintain and test.

## API Tests

The following examples assume the application is running on `localhost:8081` with an initially empty database.

### 1. Create a sensor reading

```bash
curl -i -X POST http://localhost:8081/api/v1/sensors/readings \
  -H 'Content-Type: application/json' \
  -d '{
    "sensor": "sensor-1",
    "at": "2026-10-28T10:00:00Z",
    "metrics": {
      "temperature": 18.5,
      "humidity": 65.0,
      "wind": 12.0
    }
  }'
```

Expected: `201 Created`.

### 2. Create another sensor reading

```bash
curl -i -X POST http://localhost:8081/api/v1/sensors/readings \
  -H 'Content-Type: application/json' \
  -d '{
    "sensor": "sensor-2",
    "at": "2026-10-28T11:00:00Z",
    "metrics": {
      "temperature": 22.5,
      "humidity": 55.0,
      "wind": 8.0
    }
  }'
```

Expected: `201 Created`.

### 3. Calculate the average across all sensors

```bash
curl -i \
  'http://localhost:8081/api/v1/sensors/readings/average?metric=temperature&from=2026-10-28T00:00:00Z&to=2026-10-29T00:00:00Z'
```

Expected: average temperature `20.5`, count `2`.

### 4. Calculate the average for a specific sensor

```bash
curl -i \
  'http://localhost:8081/api/v1/sensors/sensor-1/readings/average?metric=temperature&from=2026-10-28T00:00:00Z&to=2026-10-29T00:00:00Z'
```

Expected: average temperature `18.5`, count `1`.

**Note:** Run tests 1 and 2 before tests 3 and 4. Expected averages assume both readings are successfully persisted and the date range is inclusive of the readings.
