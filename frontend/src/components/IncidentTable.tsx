import type { Incident, Severity } from "../types/incident";

interface IncidentTableProps {
  incidents: Incident[];
  selectedIncidentId: number | null;
  onSelectIncident: (incident: Incident) => void;
  loading?: boolean;
}

function formatDate(value: string): string {
  return new Date(value).toLocaleString();
}

function getSeverityClass(severity: Severity): string {
  return `severity severity-${severity.toLowerCase()}`;
}

function getStatusClass(status: string): string {
  return `status status-${status.toLowerCase()}`;
}

function LoadingRows() {
  return (
    <>
      {Array.from({ length: 8 }).map((_, index) => (
        <tr key={index} className="skeleton-row">
          <td>
            <div className="skeleton skeleton-id" />
          </td>

          <td>
            <div className="skeleton skeleton-title" />
            <div className="skeleton skeleton-key" />
          </td>

          <td>
            <div className="skeleton skeleton-service" />
          </td>

          <td>
            <div className="skeleton skeleton-badge" />
          </td>

          <td>
            <div className="skeleton skeleton-badge" />
          </td>

          <td>
            <div className="skeleton skeleton-time" />
          </td>
        </tr>
      ))}
    </>
  );
}

export default function IncidentTable({
  incidents,
  selectedIncidentId,
  onSelectIncident,
  loading = false,
}: IncidentTableProps) {
  return (
    <div className="incident-table-wrapper">
      <table className="incident-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Incident</th>
            <th>Service</th>
            <th>Severity</th>
            <th>Status</th>
            <th>Detected</th>
          </tr>
        </thead>

        <tbody>
          {loading ? (
            <LoadingRows />
          ) : incidents.length === 0 ? (
            <tr>
              <td colSpan={6} className="empty-state">
                <div className="empty-state-icon">⌁</div>
                <strong>No incidents found</strong>
                <span>
                  Try changing your search or filter criteria.
                </span>
              </td>
            </tr>
          ) : (
            incidents.map((incident) => (
              <tr
                key={incident.id}
                className={
                  incident.id === selectedIncidentId
                    ? "incident-row selected"
                    : "incident-row"
                }
                onClick={() => onSelectIncident(incident)}
              >
                <td className="incident-id">#{incident.id}</td>

                <td>
                  <div className="incident-title">
                    {incident.title}
                  </div>

                  <div className="incident-key">
                    {incident.incidentKey}
                  </div>
                </td>

                <td>
                  <span className="service-name">
                    {incident.serviceName}
                  </span>
                </td>

                <td>
                  <span className={getSeverityClass(incident.severity)}>
                    {incident.severity}
                  </span>
                </td>

                <td>
                  <span className={getStatusClass(incident.status)}>
                    {incident.status}
                  </span>
                </td>

                <td className="detected-time">
                  {formatDate(incident.detectedAt)}
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}