export type Severity = "CRITICAL" | "HIGH" | "MEDIUM" | "LOW";

export type IncidentStatus =
  | "OPEN"
  | "RESOLVED"
  | "ACKNOWLEDGED";

export interface Incident {
  id: number;
  incidentKey: string;
  title: string;
  description: string;
  severity: Severity;
  status: IncidentStatus;
  serviceName: string;
  detectedAt: string;
  createdAt: string;
  updatedAt: string;
}

export interface IncidentRca {
  rootCause: string;
  confidence: number;
  impact: string;
  evidence: string[];
  recommendedActions: string[];
}

export interface TelemetryPoint {
  metric: string;
  value: number;
  timestamp: string;
  traceId: string | null;
  spanId: string | null;
}

export interface IncidentDetail extends Incident {
  rca: IncidentRca | null;
  telemetry: TelemetryPoint[];
}