# Campus Facility Complaint API (FacilTrack)

An enterprise-grade, production-ready microservices architecture built with **Java 17+** and **Spring Boot 3.3.x**, designed for managing campus facility maintenance, student/staff complaint reports, and technician repair assignments.

---

## 🏛 Architecture Overview

The system consists of three decoupled microservices organized in a single repository with a Maven Multi-Module structure:

```
                            +--------------------+
                            |  facility-service  |
                            |    (Port: 8082)    |
                            |  [facility_db]     |
                            +---------^----------+
                                      |
                     REST GET Facility| (RestClient validation)
                                      |
+--------------------+      +---------+----------+      +--------------------+
|   Student/Staff    | ---> |   report-service   | <--- | assignment-service |
| Complaint Creation |      |    (Port: 8081)    |      |    (Port: 8083)    |
+--------------------+      |   [report_db]      |      |  [assignment_db]   |
                            +--------------------+      +--------------------+
                                      ^                           |
                                      |   PATCH Internal Status   |
                                      +---------------------------+
                                        (ASSIGNED / IN_PROGRESS / RESOLVED)
```

### Microservices Summary

| Service | Port | Database (H2 File) | Swagger UI | Responsibilities |
|---|---|---|---|---|
| **facility-service** | `8082` | `jdbc:h2:file:./data/facility/facility_db` | `http://localhost:8082/swagger-ui.html` | Facility registration, room catalog, operational status management (ACTIVE / INACTIVE) |
| **report-service** | `8081` | `jdbc:h2:file:./data/report/report_db` | `http://localhost:8081/swagger-ui.html` | Complaint reporting, priority tagging, facility validation, verification lifecycle |
| **assignment-service** | `8083` | `jdbc:h2:file:./data/assignment/assignment_db` | `http://localhost:8083/swagger-ui.html` | Technician dispatch, repair workflow updates, resolution tracking, sync to report-service |

---

## 🔧 Core Technologies & Libraries

- **Java 17+ / Java 21**
- **Spring Boot 3.3.4**
- **Spring Data JPA** (Standard repository methods, clean entity lifecycle hooks, zero native SQL queries for seamless database portability)
- **H2 File Database** (application); **H2 In-Memory** (tests) (Web console enabled at `/h2-console`)
- **Spring 6 `RestClient`** (Synchronous, non-blocking-capable HTTP client with configurable timeouts and custom error mapping)
- **Springdoc OpenAPI 2.6.0** (`springdoc-openapi-starter-webmvc-ui` with Swagger 3 at `/swagger-ui.html`)
- **Testing**: JUnit 5, Mockito, Spring Test, `MockMvc`, `MockRestServiceServer`
- **CI/CD**: GitHub Actions workflow (`.github/workflows/ci.yml`)

---

## 🚀 Repository Structure

```
FacilTrack/
├── .github/
│   └── workflows/
│       └── ci.yml               # Automated CI test pipeline for GitHub Actions
├── pom.xml                      # Multi-module root build configuration & BOM
├── facility-service/            # Facility Management Service (Port 8082)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/campus/facility/
│       │   ├── config/          # OpenAPI configuration
│       │   ├── controller/      # REST endpoints (/api/v1/facilities)
│       │   ├── dto/             # Request & Response DTOs
│       │   ├── entity/          # JPA Facility entity & OperationalStatus enum
│       │   ├── exception/       # Central @RestControllerAdvice & RFC unified errors
│       │   ├── repository/      # Spring Data JPA Repository
│       │   └── service/         # Business logic layer
│       └── test/                # Unit tests, MockMvc slice tests, JPA tests
├── report-service/              # Complaint Report Service (Port 8081)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/campus/report/
│       │   ├── client/          # Spring 6 RestClient for facility-service
│       │   ├── config/          # RestClient & OpenAPI configurations
│       │   ├── controller/      # Public & Internal sync REST controllers
│       │   ├── dto/             # Request & Response DTOs
│       │   ├── entity/          # ComplaintReport entity, Priority, ReportStatus
│       │   ├── exception/       # Global exception handling (400, 404, 422, 503)
│       │   ├── repository/      # Spring Data JPA Repository
│       │   └── service/         # Report management & lifecycle validation
│       └── test/                # MockMvc tests, MockRestServiceServer tests, E2E
└── assignment-service/          # Maintenance Assignment Service (Port 8083)
    ├── pom.xml
    └── src/
        ├── main/java/com/campus/assignment/
        │   ├── client/          # Spring 6 RestClient for report-service sync
        │   ├── config/          # RestClient & OpenAPI configurations
        │   ├── controller/      # Maintenance assignment & history endpoints
        │   ├── dto/             # Request & Response DTOs
        │   ├── entity/          # MaintenanceAssignment entity & WorkStatus enum
        │   ├── exception/       # Global exception handling
        │   ├── repository/      # Spring Data JPA Repository
        │   └── service/         # Assignment workflow & sync logic
        └── test/                # MockMvc tests, MockRestServiceServer tests, E2E
```

---

## 📡 API Endpoints Reference

### 1. Facility Service (`http://localhost:8082`)

| HTTP Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/facilities` | List all facilities (optional `?status=ACTIVE\|INACTIVE`) |
| `GET` | `/api/v1/facilities/{id}` | Get facility details by ID |
| `POST` | `/api/v1/facilities` | Register new facility (defaults to `ACTIVE`) |
| `PUT` | `/api/v1/facilities/{id}` | Full update of facility details |
| `PATCH` | `/api/v1/facilities/{id}` | Partial update (e.g. toggle `operationalStatus`) |
| `DELETE` | `/api/v1/facilities/{id}` | Deactivate / soft-delete facility (sets `INACTIVE`) |

### 2. Report Service (`http://localhost:8081`)

| HTTP Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/reports` | Create complaint (validates facility with `facility-service`, status: `SUBMITTED`) |
| `GET` | `/api/v1/reports` | List complaints (filter by `?status=...&category=...`) |
| `GET` | `/api/v1/reports/{id}` | Get complaint details |
| `PUT` | `/api/v1/reports/{id}` | Full update (permitted **only** when status is `SUBMITTED`) |
| `PATCH` | `/api/v1/reports/{id}` | Partial update (permitted **only** when status is `SUBMITTED`) |
| `DELETE` | `/api/v1/reports/{id}` | Cancel complaint (permitted **only** when status is `SUBMITTED`) |
| `PATCH` | `/api/v1/reports/{id}/verify` | Admin verification: transitions to `VERIFIED` or `REJECTED` |
| `PATCH` | `/api/v1/internal/reports/{id}/status` | Internal sync endpoint called by `assignment-service` |

### 3. Assignment Service (`http://localhost:8083`)

| HTTP Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/assignments` | Assign technician to report (`workStatus` = `ASSIGNED`, syncs to `report-service`) |
| `GET` | `/api/v1/assignments/{id}` | Retrieve assignment details |
| `PATCH` | `/api/v1/assignments/{id}/status` | Update `workStatus` (`IN_PROGRESS` or `RESOLVED`). If `RESOLVED`, `resolutionNote` is strictly required |
| `GET` | `/api/v1/reports/{reportId}/maintenance-history` | Get full history and resolution records for a given report |

---

## 🛡 Global Error Handling (Unified RFC Schema)

Every microservice implements a centralized `@RestControllerAdvice` producing standard RFC 7807 compliant error responses:

```json
{
  "timestamp": "2026-10-21T10:45:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Facility with ID 'fac-101' is currently INACTIVE and cannot accept complaint reports",
  "path": "/api/v1/reports",
  "details": null
}
```

### Standard Status Codes Handled:
- **`400 Bad Request`**: Malformed JSON, constraint violations (`@NotBlank`, `@NotNull`), invalid enum arguments.
- **`404 Not Found`**: Resource does not exist.
- **`422 Unprocessable Entity`**: Business logic rule rejections (e.g., submitting report for inactive facility, modifying/canceling report when status is beyond `SUBMITTED`, resolving assignment without `resolutionNote`).
- **`503 Service Unavailable`**: Inter-service communication failures (remote service down, connection timeout).
- **`500 Internal Server Error`**: Unexpected server error fallback.

---

## 🏃 Getting Started & Running Locally

### Prerequisites
- JDK 17 or higher
- Apache Maven 3.8+ (or use installed Maven 3.9+)

### Build All Microservices
```bash
./mvnw clean package
```

### Running the Services

Run all commands from the repository root (`FacilTrack/`). Build first as above,
then use a separate terminal for each application, all with the same working directory:

```bash
# Terminal 1 (repository root)
java -jar facility-service/target/facility-service-1.0.0.jar

# Terminal 2 (repository root)
java -jar report-service/target/report-service-1.0.0.jar

# Terminal 3 (repository root)
java -jar assignment-service/target/assignment-service-1.0.0.jar
```

### Persistent local storage

Each service opens only its own embedded H2 database. Hibernate uses
`ddl-auto: update` for these local application databases. H2 creates missing
storage directories on first connection; existing database files are reused.

| Service | Directory environment variable | Default directory | Default database file (from repository root) |
|---|---|---|---|
| Facility | `FACILITY_DB_DIR` | `./data/facility` | `data/facility/facility_db.mv.db` |
| Report | `REPORT_DB_DIR` | `./data/report` | `data/report/report_db.mv.db` |
| Assignment | `ASSIGNMENT_DB_DIR` | `./data/assignment` | `data/assignment/assignment_db.mv.db` |

Variables specify a directory, without a database filename or JDBC prefix. For example:

```bash
FACILITY_DB_DIR=/absolute/path/faciltrack/facility java -jar facility-service/target/facility-service-1.0.0.jar
REPORT_DB_DIR=/absolute/path/faciltrack/report java -jar report-service/target/report-service-1.0.0.jar
ASSIGNMENT_DB_DIR=/absolute/path/faciltrack/assignment java -jar assignment-service/target/assignment-service-1.0.0.jar
```

Relative paths are resolved from the process working directory, not the JAR
location. Keep the working directory and environment variables identical across
restarts. Use an absolute path or an explicit `./` prefix for custom directories.
H2 appends `.mv.db` to the database name. Local database files are ignored by Git;
each laptop has its own data, which is not shared through GitHub.

Stop each application with **Ctrl+C** (or SIGTERM), then wait for shutdown to
finish before restarting. Graceful HTTP shutdown lets active requests finish;
Spring closes JPA and the connection pool, allowing H2 to close normally.
The old in-memory-only `DB_CLOSE_DELAY=-1` setting is removed, and H2's default
shutdown hook remains enabled (no `DB_CLOSE_ON_EXIT=FALSE` override).

Do not run two application processes against the same embedded database file
at the same time. There is no `AUTO_SERVER` mode. Use the service's own web H2
console while it is running rather than opening its file from another JVM.
If Docker is added later, mount the database directory as a persistent volume.
This change cannot recover or automatically migrate previously lost in-memory data.

### Checking persistence through API V1

1. Start Facility, Report, then Assignment using the root-directory commands above.
2. POST `/api/v1/facilities` on port 8082 with a new name, `facilityType`,
   `building`, `room`, and `operationalStatus: "ACTIVE"`; record the returned ID.
3. POST `/api/v1/reports` on port 8081 with that `facilityId`, a `reporterId`,
   free-text `category`, `description`, and `priority: "HIGH"`; record the ID.
4. PATCH `/api/v1/reports/{id}/verify` with `{"status":"VERIFIED"}`.
5. POST `/api/v1/assignments` on port 8083 with that `reportId`, a
   `technicianId`, and optional `notes`; record the returned ID. The report
   status becomes `ASSIGNED`.
6. GET each resource by ID on its service and save the responses. Stop all three
   normally, restart from the same root directory with the same storage settings,
   then repeat the GETs. The IDs, contents, and statuses should still match.

Use new sample records; do not delete existing database files to perform this check.

### Accessing Swagger UI & H2 Consoles
- **Facility Service**:
  - Swagger UI: `http://localhost:8082/swagger-ui.html`
  - H2 Console: `http://localhost:8082/h2-console` (JDBC URL: `jdbc:h2:file:./data/facility/facility_db`, user: `sa`, blank password)
- **Report Service**:
  - Swagger UI: `http://localhost:8081/swagger-ui.html`
  - H2 Console: `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:file:./data/report/report_db`, user: `sa`, blank password)
- **Assignment Service**:
  - Swagger UI: `http://localhost:8083/swagger-ui.html`
  - H2 Console: `http://localhost:8083/h2-console` (JDBC URL: `jdbc:h2:file:./data/assignment/assignment_db`, user: `sa`, blank password)

---

## 🧪 Running Automated Tests

Tests keep separate H2 **in-memory** databases (`facility_test_db`,
`report_test_db`, `assignment_test_db`) through the `test` profile and
`ddl-auto: create-drop`. Repository and lifecycle integration tests activate
this profile; they do not open application database files. Unit and controller
tests use mocks.

Run the full test suite across all 3 microservices:
```bash
./mvnw clean test
```

Or verify with packaging:
```bash
./mvnw clean verify
```

The automated test suite includes:
- **Domain Validation & Logic Tests**: JUnit 5 & Mockito testing entity behavior, status validation, and edge-cases.
- **Controller Slice Tests**: Spring `MockMvc` verifying HTTP statuses (200, 201, 204, 400, 404, 422, 503).
- **HTTP Mock Server Tests**: Spring `MockRestServiceServer` validating `RestClient` connection handling, timeouts, and error code translations.
- **End-to-End Lifecycle Integration Tests**: Full integration tests validating the complete lifecycle: Facility Creation -> Report Submission -> Verification -> Assignment -> In Progress -> Resolution with resolution note -> Maintenance history check.
