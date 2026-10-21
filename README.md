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

| Service | Port | Database (H2 In-Memory) | Swagger UI | Responsibilities |
|---|---|---|---|---|
| **facility-service** | `8082` | `jdbc:h2:mem:facility_db` | `http://localhost:8082/swagger-ui.html` | Facility registration, room catalog, operational status management (ACTIVE / INACTIVE) |
| **report-service** | `8081` | `jdbc:h2:mem:report_db` | `http://localhost:8081/swagger-ui.html` | Complaint reporting, priority tagging, facility validation, verification lifecycle |
| **assignment-service** | `8083` | `jdbc:h2:mem:assignment_db` | `http://localhost:8083/swagger-ui.html` | Technician dispatch, repair workflow updates, resolution tracking, sync to report-service |

---

## 🔧 Core Technologies & Libraries

- **Java 17+ / Java 21**
- **Spring Boot 3.3.4**
- **Spring Data JPA** (Standard repository methods, clean entity lifecycle hooks, zero native SQL queries for seamless database portability)
- **H2 In-Memory Database** (Web console enabled at `/h2-console`)
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
mvn clean package
```

### Running the Services
Run each service in a separate terminal:

```bash
# Terminal 1 - Facility Service
cd facility-service
mvn spring-boot:run

# Terminal 2 - Report Service
cd report-service
mvn spring-boot:run

# Terminal 3 - Assignment Service
cd assignment-service
mvn spring-boot:run
```

### Accessing Swagger UI & H2 Consoles
- **Facility Service**:
  - Swagger UI: `http://localhost:8082/swagger-ui.html`
  - H2 Console: `http://localhost:8082/h2-console` (JDBC URL: `jdbc:h2:mem:facility_db`, user: `sa`, blank password)
- **Report Service**:
  - Swagger UI: `http://localhost:8081/swagger-ui.html`
  - H2 Console: `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:report_db`, user: `sa`, blank password)
- **Assignment Service**:
  - Swagger UI: `http://localhost:8083/swagger-ui.html`
  - H2 Console: `http://localhost:8083/h2-console` (JDBC URL: `jdbc:h2:mem:assignment_db`, user: `sa`, blank password)

---

## 🧪 Running Automated Tests

Run the full test suite across all 3 microservices:
```bash
mvn clean test
```

Or verify with packaging:
```bash
mvn clean verify
```

The automated test suite includes:
- **Domain Validation & Logic Tests**: JUnit 5 & Mockito testing entity behavior, status validation, and edge-cases.
- **Controller Slice Tests**: Spring `MockMvc` verifying HTTP statuses (200, 201, 204, 400, 404, 422, 503).
- **HTTP Mock Server Tests**: Spring `MockRestServiceServer` validating `RestClient` connection handling, timeouts, and error code translations.
- **End-to-End Lifecycle Integration Tests**: Full integration tests validating the complete lifecycle: Facility Creation -> Report Submission -> Verification -> Assignment -> In Progress -> Resolution with resolution note -> Maintenance history check.
