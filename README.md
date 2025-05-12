# Virtual Power Plant (VPP) REST API

A Spring Boot-based REST API that simulates a Virtual Power Plant (VPP) by managing and querying distributed battery resources. This system aggregates power from small-scale batteries and enables flexible, filtered retrieval of capacity data.

---

## Features

- Register batteries with name, postcode, and capacity
- Query batteries by postcode range with total and average capacity stats
- Optional filtering by min and max capacity
- Sorted battery names for cleaner insights
- Java Streams for in-memory processing
- Concurrent save handling for high-throughput simulation
- Logging with Log4j2
- Integration tests using Testcontainers with PostgreSQL
- Dockerized setup for consistent local/dev/test environments
- Interactive API documentation with Swagger UI

---

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Web, JPA, Validation
- PostgreSQL
- Log4j2
- Swagger UI (Springdoc OpenAPI)
- Docker & Docker Compose
- Gradle (with wrapper)
- Testcontainers
- JUnit 5

---

## Setup

### Prerequisites

- **Docker + Docker Compose** – required for running the project
- **Java 17** *(optional)* – required for running or debugging the app outside Docker
- **Gradle** *(optional)* – used for local builds and testing, included via wrapper (`./gradlew`)

### Running the App

```bash
  docker-compose up --build
```

Access the API at: `http://localhost:8080`

### Running Tests

```bash
  ./gradlew test
```

### Code Coverage

```bash
  ./gradlew jacocoTestReport
```

Then open: `build/reports/jacoco/test/html/index.html`

---

## API Endpoints

### Register Batteries

`POST /api/batteries`

Request Body:
```json
{
  "batteries": [
    { "name": "Alpha", "postcode": "6000", "capacity": 1000 },
    { "name": "Beta", "postcode": "6001", "capacity": 2000 }
  ]
}
```

### Query Batteries

`GET /api/batteries?startPostcode=6000&endPostcode=6100&minCapacity=1000&maxCapacity=30000`

Sample Response:
```json
{
  "batteryNames": ["Alpha", "Beta"],
  "totalCapacity": 3000,
  "averageCapacity": 1500.0
}
```

---

## API Documentation

Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

---

## Project Structure

```
src/
├── controller/
├── dto/
├── model/
├── repository/
├── service/
└── config/
```

---

## Key Implementation Notes

- Postcode stored numerically (`postcodeNumeric`) for range filtering
- Validation and exception handling via annotations and `@ControllerAdvice`
- Private setters for derived fields ensure data integrity
- Profile-specific configs for dev and test environments
- Log rotation enabled using Log4j2 with file appender

---

## Architectural Decisions

- **Postcode Normalization:** Postcode strings are converted to integers (postcodeNumeric) to enable efficient numeric range queries in the database.


- **Entity Design:** Setter methods for derived fields (like postcodeNumeric) are made private to preserve internal consistency. The class exposes only necessary public methods to follow encapsulation principles.


- **Validation and Error Handling:** Input validation is handled via Spring’s validation annotations. All validation errors and runtime exceptions are centralized using @RestControllerAdvice for consistency.


- **DTO Usage:** To decouple persistence models from the API contract, request and response bodies are mapped to DTOs. This makes the API more resilient to future model changes.


## Testing

- Unit tests for service and validation logic
- Integration tests using Testcontainers with PostgreSQL
- Concurrent scenarios tested with multi-threading

---

## Test Data

Use the provided `test-data May 2025.json` file to bulk test battery uploads.

---

## Git & Collaboration

- Issues were created to track features, improvements, and fixes.

- Pull Requests (PRs) were used to submit changes, each with clear titles and descriptions.

- All changes were peer-review ready with meaningful commit messages.

