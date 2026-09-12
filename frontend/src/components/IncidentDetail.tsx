import type {
  IncidentDetail as IncidentDetailType,
  TelemetryPoint,
} from "../types/incident";

interface IncidentDetailProps {
  incident: IncidentDetailType | null;
  loading: boolean;
}

function formatDate(value: string): string {
  return new Date(value).toLocaleString();
}

function getLatestMetric(
  telemetry: TelemetryPoint[],
  metric: string,
): TelemetryPoint | null {
  const matches = telemetry.filter(
    (item) => item.metric === metric,
  );

  if (matches.length === 0) {
    return null;
  }

  return matches.reduce((latest, current) =>
    new Date(current.timestamp).getTime() >
    new Date(latest.timestamp).getTime()
      ? current
      : latest,
  );
}

function formatMetricValue(
  metric: string,
  value: number,
): string {
  if (metric === "db_latency_ms") {
    return `${Math.round(value).toLocaleString()} ms`;
  }

  return `${value.toFixed(1)}%`;
}

function getMetricLabel(metric: string): string {
  switch (metric) {
    case "db_latency_ms":
      return "DB Latency";

    case "db_pool_usage":
      return "DB Pool Usage";

    case "http_5xx_rate":
      return "HTTP 5xx Rate";

    default:
      return metric;
  }
}

function getMetricClass(
  metric: string,
  value: number,
): string {
  const isCritical =
    (metric === "db_latency_ms" && value > 1000) ||
    (metric === "db_pool_usage" && value > 90) ||
    (metric === "http_5xx_rate" && value > 5);

  return isCritical
    ? "telemetry-card telemetry-card-critical"
    : "telemetry-card";
}

function ConfidenceMeter({
  confidence,
}: {
  confidence: number;
}) {
  const percentage = Math.max(
    0,
    Math.min(100, confidence * 100),
  );

  return (
    <div className="confidence-meter">
      <div className="confidence-meter-header">
        <span>Confidence</span>

        <strong>{percentage.toFixed(0)}%</strong>
      </div>

      <div className="confidence-track">
        <div
          className="confidence-fill"
          style={{
            width: `${percentage}%`,
          }}
        />
      </div>
    </div>
  );
}

function IncidentTimeline({
  incident,
}: {
  incident: IncidentDetailType;
}) {
  const events = [
    {
      label: "Incident detected",
      timestamp: incident.detectedAt,
      completed: true,
    },
    {
      label: "Incident created",
      timestamp: incident.createdAt,
      completed: true,
    },
    {
      label: "Record updated",
      timestamp: incident.updatedAt,
      completed: true,
    },
  ];

  return (
    <section className="detail-section">
      <div className="section-label">
        INCIDENT TIMELINE
      </div>

      <div className="timeline">
        {events.map((event, index) => (
          <div className="timeline-item" key={event.label}>
            <div className="timeline-indicator">
              <span
                className={
                  event.completed
                    ? "timeline-dot completed"
                    : "timeline-dot"
                }
              />
              {index < events.length - 1 && (
                <span className="timeline-line" />
              )}
            </div>

            <div className="timeline-content">
              <strong>{event.label}</strong>

              <span>{formatDate(event.timestamp)}</span>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default function IncidentDetail({
  incident,
  loading,
}: IncidentDetailProps) {
  if (loading) {
    return (
      <aside className="incident-detail-panel">
        <div className="detail-loading">
          Loading incident details...
        </div>
      </aside>
    );
  }

  if (!incident) {
    return (
      <aside className="incident-detail-panel">
        <div className="detail-empty">
          <div className="detail-empty-icon">◈</div>

          <h3>Select an incident</h3>

          <p>
            Choose an incident from the table to inspect its
            details and RCA.
          </p>
        </div>
      </aside>
    );
  }

  const rca = incident.rca;

  const dbLatency = getLatestMetric(
    incident.telemetry,
    "db_latency_ms",
  );

  const dbPoolUsage = getLatestMetric(
    incident.telemetry,
    "db_pool_usage",
  );

  const http5xxRate = getLatestMetric(
    incident.telemetry,
    "http_5xx_rate",
  );

  return (
    <aside className="incident-detail-panel">
      <div className="detail-header">
        <div>
          <div className="detail-eyebrow">
            INCIDENT #{incident.id}
          </div>

          <h2>{incident.title}</h2>

          <div className="detail-key">
            {incident.incidentKey}
          </div>
        </div>

        <span
          className={`severity severity-${incident.severity.toLowerCase()}`}
        >
          {incident.severity}
        </span>
      </div>

      <div className="detail-meta-grid">
        <div className="detail-meta-item">
          <span>Service</span>
          <strong>{incident.serviceName}</strong>
        </div>

        <div className="detail-meta-item">
          <span>Status</span>
          <strong>{incident.status}</strong>
        </div>

        <div className="detail-meta-item">
          <span>Detected</span>
          <strong>{formatDate(incident.detectedAt)}</strong>
        </div>

        <div className="detail-meta-item">
          <span>Created</span>
          <strong>{formatDate(incident.createdAt)}</strong>
        </div>
      </div>

      <IncidentTimeline incident={incident} />

      <section className="detail-section">
        <div className="section-label">DESCRIPTION</div>

        <p className="detail-description">
          {incident.description}
        </p>
      </section>

      <section className="detail-section telemetry-section">
        <div className="section-heading">
          <div>
            <div className="section-label">TELEMETRY</div>

            <h3>Detection signals</h3>
          </div>

          <span className="telemetry-window">
            60s window
          </span>
        </div>

        <div className="telemetry-grid">
          {dbLatency && (
            <div
              className={getMetricClass(
                dbLatency.metric,
                dbLatency.value,
              )}
            >
              <div className="telemetry-label">
                {getMetricLabel(dbLatency.metric)}
              </div>

              <div className="telemetry-value">
                {formatMetricValue(
                  dbLatency.metric,
                  dbLatency.value,
                )}
              </div>

              <div className="telemetry-time">
                {formatDate(dbLatency.timestamp)}
              </div>
            </div>
          )}

          {dbPoolUsage && (
            <div
              className={getMetricClass(
                dbPoolUsage.metric,
                dbPoolUsage.value,
              )}
            >
              <div className="telemetry-label">
                {getMetricLabel(dbPoolUsage.metric)}
              </div>

              <div className="telemetry-value">
                {formatMetricValue(
                  dbPoolUsage.metric,
                  dbPoolUsage.value,
                )}
              </div>

              <div className="telemetry-time">
                {formatDate(dbPoolUsage.timestamp)}
              </div>
            </div>
          )}

          {http5xxRate && (
            <div
              className={getMetricClass(
                http5xxRate.metric,
                http5xxRate.value,
              )}
            >
              <div className="telemetry-label">
                {getMetricLabel(http5xxRate.metric)}
              </div>

              <div className="telemetry-value">
                {formatMetricValue(
                  http5xxRate.metric,
                  http5xxRate.value,
                )}
              </div>

              <div className="telemetry-time">
                {formatDate(http5xxRate.timestamp)}
              </div>
            </div>
          )}
        </div>

        {!dbLatency &&
          !dbPoolUsage &&
          !http5xxRate && (
            <div className="telemetry-empty">
              No metric telemetry is available for this
              incident.
            </div>
          )}
      </section>

      {rca ? (
        <>
          <section className="detail-section rca-section">
            <div className="section-heading">
              <div>
                <div className="section-label">
                  ROOT CAUSE ANALYSIS
                </div>

                <h3>AI-assisted RCA</h3>
              </div>

              <div className="confidence">
                <strong>
                  {(rca.confidence * 100).toFixed(0)}%
                </strong>
              </div>
            </div>

            <ConfidenceMeter
              confidence={rca.confidence}
            />

            <div className="rca-root-cause">
              {rca.rootCause}
            </div>
          </section>

          <section className="detail-section">
            <div className="section-label">IMPACT</div>

            <p className="detail-description">
              {rca.impact}
            </p>
          </section>

          <section className="detail-section">
            <div className="section-label">EVIDENCE</div>

            <div className="evidence-list">
              {rca.evidence.map((item, index) => (
                <div className="evidence-item" key={index}>
                  <span className="evidence-marker">
                    {String(index + 1).padStart(2, "0")}
                  </span>

                  <span>{item}</span>
                </div>
              ))}
            </div>
          </section>

          <section className="detail-section">
            <div className="section-label">
              RECOMMENDED ACTIONS
            </div>

            <div className="actions-list">
              {rca.recommendedActions.map(
                (action, index) => (
                  <div className="action-item" key={index}>
                    <span className="action-number">
                      {index + 1}
                    </span>

                    <span>{action}</span>
                  </div>
                ),
              )}
            </div>
          </section>
        </>
      ) : (
        <section className="detail-section">
          <div className="no-rca">
            <div className="section-label">
              ROOT CAUSE ANALYSIS
            </div>

            <p>
              RCA has not been generated for this incident
              yet.
            </p>
          </div>
        </section>
      )}
    </aside>
  );
}