# PulseOps

### Distributed Incident Detection & Automated Root Cause Analysis

PulseOps is an event-driven incident detection and automated RCA platform designed to detect abnormal behavior across distributed services, correlate telemetry, create incidents, and generate evidence-backed root cause analysis.

It combines rule-based detection, anomaly scoring, distributed event processing, observability data, and LLM-assisted RCA into a single workflow.

---

## Overview

In a distributed system, failures often appear first as a combination of symptoms rather than a single obvious error:

- increasing database latency
- connection-pool saturation
- elevated HTTP 5xx errors
- abnormal service telemetry
- degraded request performance

PulseOps continuously processes telemetry from services, correlates related signals within a time window, detects abnormal behavior, creates incidents, collects observability evidence, and generates an RCA.

The goal is to reduce the time between:

```text
Telemetry → Detection → Incident → Evidence → RCA
````

---

## Architecture

```text
                         ┌───────────────────────┐
                         │     Demo Services     │
                         │                       │
                         │ Order / Payment /     │
                         │ Inventory             │
                         └───────────┬───────────┘
                                     │
                              Telemetry Events
                                     │
                                     ▼
                         ┌───────────────────────┐
                         │     PulseOps API      │
                         │     Spring Boot       │
                         └───────────┬───────────┘
                                     │
                          PostgreSQL + Outbox
                                     │
                                     ▼
                              ┌─────────────┐
                              │    Kafka    │
                              └──────┬──────┘
                                     │
                                     ▼
                       ┌─────────────────────────┐
                       │    Event Processor      │
                       │                         │
                       │ Correlation             │
                       │ Detection               │
                       │ Anomaly Analysis        │
                       │ Evidence Collection     │
                       └───────────┬─────────────┘
                                   │
                     ┌─────────────┼─────────────┐
                     │             │             │
                     ▼             ▼             ▼
                 ┌───────┐    ┌───────────┐  ┌─────────┐
                 │ Redis │    │Prometheus │  │  Tempo  │
                 └───────┘    └───────────┘  └─────────┘
                                   │             │
                                   └──────┬──────┘
                                          │
                                          ▼
                                ┌──────────────────┐
                                │    AI Service    │
                                │    FastAPI       │
                                │                  │
                                │ Anomaly Analysis │
                                │ RCA Engine       │
                                │ LLM + Fallback   │
                                └────────┬─────────┘
                                         │
                                      RCA Event
                                         │
                                         ▼
                                ┌──────────────────┐
                                │    PulseOps API  │
                                │                  │
                                │ Persist Incident │
                                │ Persist RCA      │
                                └──────────────────┘

Observability:

Applications → OpenTelemetry → OTel Collector → Tempo
Applications → Prometheus → Grafana
```

---

## Core Features

### Distributed telemetry ingestion

Services publish telemetry events containing:

* service name
* event type
* severity
* timestamp
* metric metadata
* trace/span identifiers when available

Telemetry is persisted through the PulseOps API before being published to Kafka.

### Event-driven processing

Kafka decouples telemetry ingestion from downstream processing.

The platform uses separate event flows for:

```text
telemetry.events
incidents.created
incidents.rca.requested
incidents.rca.completed
```

This allows telemetry processing, incident creation, and RCA generation to operate independently.

### Telemetry correlation

The Event Processor correlates related telemetry from the same service within a configurable time window.

For the payment-service demonstration, correlated signals include:

```text
db_latency_ms
db_pool_usage
http_5xx_rate
```

### Hybrid anomaly detection

PulseOps combines deterministic detection with anomaly analysis.

The detection pipeline considers conditions such as:

```text
db_latency_ms > 1000 ms
db_pool_usage > 90%
http_5xx_rate > 5%
```

The AI service can additionally perform anomaly scoring.

Rule-based detection remains available when the AI service is unavailable, preventing an AI failure from stopping incident detection.

### Automated RCA

After an incident is detected, the system collects evidence from:

* correlated telemetry
* Prometheus metrics
* Tempo traces

The RCA engine combines this evidence with AI-assisted reasoning to produce:

* root cause
* confidence score
* impact
* supporting evidence
* recommended actions

### Transactional Outbox

Telemetry and incident events use a transactional outbox pattern to prevent the database state and Kafka publication from becoming inconsistent.

The workflow is:

```text
Database Transaction
       │
       ├── Persist business/event data
       │
       └── Persist Outbox Event
                    │
                    ▼
              Outbox Publisher
                    │
                    ▼
                  Kafka
```

### Redis-backed correlation

Redis is used for short-lived telemetry correlation and incident cooldown/idempotency behavior.

This helps prevent duplicate incidents when multiple related telemetry events arrive within the same detection window.

### Observability

PulseOps includes:

* Prometheus
* Grafana
* OpenTelemetry
* OpenTelemetry Collector
* Grafana Tempo
* OpenTelemetry Java Agent

This provides both metrics and distributed tracing for investigating incidents.

---

## Technology Stack

| Area                | Technology                            |
| ------------------- | ------------------------------------- |
| Backend             | Java 21, Spring Boot                  |
| AI Service          | Python, FastAPI                       |
| Messaging           | Apache Kafka                          |
| Relational Database | PostgreSQL                            |
| Correlation / State | Redis                                 |
| Metrics             | Prometheus                            |
| Dashboards          | Grafana                               |
| Distributed Tracing | OpenTelemetry, Tempo                  |
| Telemetry Pipeline  | OpenTelemetry Collector               |
| RCA                 | Python + deterministic engine + LLM   |
| Containers          | Docker, Docker Compose                |
| Database Migrations | Flyway                                |
| Testing             | JUnit, Spring Boot Test, Python tests |

---

## Service Structure

```text
services/
├── ai-service/
├── event-processor/
├── inventory-service/
├── order-service/
├── payment-service/
├── pulseops-api/
└── pulseops-common/
```

### PulseOps API

Responsible for:

* telemetry ingestion
* incident creation
* incident persistence
* RCA persistence
* Kafka publication
* transactional outbox processing

### Event Processor

Responsible for:

* consuming telemetry
* correlation
* anomaly detection
* incident detection
* observability evidence collection
* RCA orchestration

### AI Service

Provides:

* anomaly detection
* RCA engine
* LLM-assisted RCA generation
* deterministic fallback behavior

### Demo Services

The order, payment, and inventory services simulate applications producing telemetry.

The payment service provides controllable failure modes for demonstrations:

```text
NORMAL
LATENCY
ERROR
```

---

## Event Flow

### 1. Telemetry ingestion

```text
Demo Service
     │
     ▼
POST /api/v1/telemetry/events
     │
     ▼
PulseOps API
     │
     ├── PostgreSQL
     │
     └── Outbox
             │
             ▼
           Kafka
```

### 2. Detection

```text
Kafka
  │
  ▼
Event Processor
  │
  ▼
Redis correlation window
  │
  ▼
Detection rules + anomaly analysis
  │
  ▼
Incident detected
```

### 3. RCA

```text
Incident
   │
   ▼
Prometheus + Tempo + Telemetry
   │
   ▼
Evidence Collection
   │
   ▼
AI / RCA Service
   │
   ▼
RCA result
   │
   ▼
Kafka
   │
   ▼
PulseOps API
   │
   ▼
Persisted RCA
```

---

## Detection Example

The payment-service demonstration detects degradation when multiple signals indicate abnormal behavior.

Example:

```text
HTTP 5xx rate      = 17.8%
DB pool usage      = 99.4%
DB latency         = 1694 ms
```

These correlated signals produce a critical incident.

---

## Running Locally

### Prerequisites

Install:

* Docker
* Docker Compose
* Java 21
* Maven
* Python 3.12
* `uv`

### Clone

```bash
git clone https://github.com/deepeshsingh19/pulseops.git
cd pulseops
```

### Build the Java services

```bash
cd services
mvn clean package
cd ..
```

### Start the platform

```bash
docker compose \
  -f infrastructure/docker/docker-compose.yml \
  up -d
```

Check the running containers:

```bash
docker compose \
  -f infrastructure/docker/docker-compose.yml \
  ps
```

---

## Demo: Simulate a Payment Incident

### 1. Enable payment failures

```bash
curl -s -X POST \
  http://localhost:8082/api/v1/payments/failure-mode \
  -H 'Content-Type: application/json' \
  -d '{"mode":"ERROR"}'
```

Expected:

```json
{
  "failureMode": "ERROR"
}
```

### 2. Generate failed payments

```bash
for i in {1..30}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -X POST \
    http://localhost:8082/api/v1/payments
done
```

The requests should return HTTP `500`.

### 3. Wait for processing

Allow approximately 30–60 seconds for:

```text
Telemetry
→ Kafka
→ Correlation
→ Incident
→ Evidence
→ RCA
```

### 4. Retrieve the incident

Once the incident ID is known:

```bash
curl -s \
  http://localhost:8080/api/v1/incidents/<INCIDENT_ID> \
  | jq
```

### 5. Restore normal behavior

```bash
curl -s -X POST \
  http://localhost:8082/api/v1/payments/failure-mode \
  -H 'Content-Type: application/json' \
  -d '{"mode":"NORMAL"}'
```

---

## Verified Demo Result

A complete end-to-end run produced:

```text
Incident ID:       291
Incident Key:      AUTO-PAYMENT-SERVICE-392B9651
Service:           payment-service
Severity:          CRITICAL
Status:            OPEN
```

Detected signals:

```text
HTTP 5xx count:    30
HTTP 5xx rate:     17.8%
DB pool usage:     99.4%
DB latency:        1694 ms
```

The generated RCA identified database connection saturation as the likely root cause with:

```text
Confidence: 0.91
```

The RCA also included evidence and recommended remediation actions such as investigating slow queries, database connection-pool saturation, CPU/locks/connections, and possible traffic-related connection exhaustion.

---

## API Endpoints

### Telemetry

```text
POST /api/v1/telemetry/events
```

### Incidents

```text
POST /api/v1/incidents
GET  /api/v1/incidents/{incidentId}
```

### Payment Demo

```text
POST /api/v1/payments
POST /api/v1/payments/failure-mode
GET  /api/v1/payments/failure-mode
```

### AI Service

```text
GET /health
```

### Metrics / Health

Spring Boot Actuator exposes:

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```

---

## Testing

The project includes tests for important processing and API paths, including:

* telemetry correlation
* incident API behavior
* RCA decision paths
* AI/RCA engine behavior

The Maven test suite has been run successfully across the multi-module project.

---

## Design Decisions

### Why Kafka?

Kafka provides asynchronous event processing and allows ingestion, detection, incident handling, and RCA workflows to remain decoupled.

### Why Redis?

Telemetry correlation is short-lived state rather than primary business data, making Redis suitable for maintaining correlation windows and cooldown/idempotency state.

### Why PostgreSQL?

Incident and telemetry records are persistent application data and benefit from relational consistency and transactional guarantees.

### Why an Outbox?

The transactional outbox prevents a successful database transaction from being followed by a lost Kafka event.

### Why deterministic fallback for AI?

AI services can fail, time out, or become unavailable. Incident detection should continue even when AI-assisted analysis is unavailable.

### Why OpenTelemetry + Tempo?

Distributed traces provide additional context for diagnosing failures that metrics alone may not explain.

---

## Project Goals

PulseOps demonstrates practical experience with:

* distributed systems
* event-driven architecture
* asynchronous processing
* Kafka
* transactional messaging patterns
* telemetry correlation
* observability
* metrics and distributed tracing
* Redis-backed state
* automated incident detection
* RCA pipelines
* AI-assisted engineering workflows
* Dockerized multi-service systems

---

## Future Improvements

Potential future extensions include:

* Kubernetes deployment
* cloud deployment
* persistent alerting/notification integrations
* richer anomaly models trained on historical incidents
* incident dashboards
* automated remediation workflows
* authentication and authorization

These are intentionally outside the current local Docker-based implementation.

---

## License

This project is licensed under the terms of the repository's `LICENSE` file.