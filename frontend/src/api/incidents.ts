import type { Incident, IncidentDetail } from "../types/incident";

const API_BASE_URL = "/api/v1";

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === "true";

const API_BASE_URL_RESOLVED =
  import.meta.env.VITE_API_BASE_URL || API_BASE_URL;

const DEMO_INCIDENT: IncidentDetail = {
  id: 291,
  incidentKey: "AUTO-PAYMENT-SERVICE-392B9651",
  title: "Payment service database degradation",
  description:
    "Payment service degradation detected from correlated database latency, connection-pool saturation, and elevated HTTP 5xx errors.",
  severity: "CRITICAL",
  status: "OPEN",
  serviceName: "payment-service",
  detectedAt: "2026-09-12T20:00:00Z",
  createdAt: "2026-09-12T20:00:00Z",
  updatedAt: "2026-09-12T20:00:00Z",
  rca: {
    rootCause:
      "Likely database connection saturation causing elevated database latency and downstream HTTP 5xx errors.",
    confidence: 0.91,
    impact:
      "Payment requests experienced elevated failure rates and increased latency during the degradation window.",
    evidence: [
      "Database latency is elevated at 1694 ms.",
      "Database connection pool usage is 99.4%.",
      "HTTP 5xx error rate is 17.8%.",
    ],
    recommendedActions: [
      "Inspect slow database queries.",
      "Review connection-pool sizing and saturation.",
      "Check CPU, locks, and active database connections.",
      "Investigate traffic-driven connection exhaustion.",
    ],
  },
  telemetry: [
    {
      metric: "db_latency_ms",
      value: 1693.8,
      timestamp: "2026-09-12T20:00:00Z",
      traceId: null,
      spanId: null,
    },
    {
      metric: "db_pool_usage",
      value: 99.4,
      timestamp: "2026-09-12T20:00:00Z",
      traceId: null,
      spanId: null,
    },
    {
      metric: "http_5xx_rate",
      value: 17.8,
      timestamp: "2026-09-12T20:00:00Z",
      traceId: null,
      spanId: null,
    },
  ],
};

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text();

    throw new Error(
      message || `Request failed with status ${response.status}`,
    );
  }

  return response.json();
}

async function tryFetch<T>(url: string, fallback: T): Promise<T> {
  try {
    const response = await fetch(url);
    return await handleResponse<T>(response);
  } catch {
    return fallback;
  }
}

export async function getIncidents(): Promise<Incident[]> {
  if (DEMO_MODE) {
    return [DEMO_INCIDENT];
  }

  return tryFetch<Incident[]>(
    `${API_BASE_URL_RESOLVED}/incidents`,
    [DEMO_INCIDENT],
  );
}

export async function getIncident(
  incidentId: number,
): Promise<IncidentDetail> {
  if (DEMO_MODE || incidentId === DEMO_INCIDENT.id) {
    return DEMO_INCIDENT;
  }

  return tryFetch<IncidentDetail>(
    `${API_BASE_URL_RESOLVED}/incidents/${incidentId}`,
    DEMO_INCIDENT,
  );
}