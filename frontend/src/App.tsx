import { useCallback, useEffect, useMemo, useState } from "react";

import { getIncident, getIncidents } from "./api/incidents";
import IncidentDetail from "./components/IncidentDetail";
import IncidentTable from "./components/IncidentTable";
import StatCard from "./components/StatCard";
import type {
  Incident,
  IncidentDetail as IncidentDetailType,
} from "./types/incident";

const PAGE_SIZE = 50;
const REFRESH_INTERVAL = 15000;

function formatLastUpdated(date: Date | null): string {
  if (!date) {
    return "Never";
  }

  return date.toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  });
}

function App() {
  const [incidents, setIncidents] = useState<Incident[]>([]);
  const [selectedIncident, setSelectedIncident] =
    useState<IncidentDetailType | null>(null);

  const [loadingIncidents, setLoadingIncidents] = useState(true);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const [error, setError] = useState<string | null>(null);
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null);

  const [search, setSearch] = useState("");
  const [severityFilter, setSeverityFilter] = useState("ALL");
  const [statusFilter, setStatusFilter] = useState("ALL");

  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE);

  const loadIncidentDetail = useCallback(async (incidentId: number) => {
    try {
      setLoadingDetail(true);

      const detail = await getIncident(incidentId);

      setSelectedIncident(detail);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to load incident details.",
      );
    } finally {
      setLoadingDetail(false);
    }
  }, []);

  const refreshIncidents = useCallback(
    async (initial = false) => {
      try {
        if (initial) {
          setLoadingIncidents(true);
        } else {
          setRefreshing(true);
        }

        setError(null);

        const data = await getIncidents();

        setIncidents(data);
        setLastUpdated(new Date());

        if (data.length === 0) {
          setSelectedIncident(null);
          return;
        }

        const currentSelectedId = selectedIncident?.id;

        if (
          currentSelectedId &&
          data.some((incident) => incident.id === currentSelectedId)
        ) {
          await loadIncidentDetail(currentSelectedId);
        } else if (!currentSelectedId || initial) {
          await loadIncidentDetail(data[0].id);
        } else {
          setSelectedIncident(null);
        }
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load incidents.",
        );
      } finally {
        setLoadingIncidents(false);
        setRefreshing(false);
      }
    },
    [loadIncidentDetail, selectedIncident?.id],
  );

  useEffect(() => {
    void refreshIncidents(true);
  }, [refreshIncidents]);

  useEffect(() => {
    const interval = window.setInterval(() => {
      void refreshIncidents(false);
    }, REFRESH_INTERVAL);

    return () => {
      window.clearInterval(interval);
    };
  }, [refreshIncidents]);

  useEffect(() => {
    setVisibleCount(PAGE_SIZE);
  }, [search, severityFilter, statusFilter]);

  const filteredIncidents = useMemo(() => {
    const query = search.trim().toLowerCase();

    return incidents.filter((incident) => {
      const matchesSearch =
        !query ||
        incident.incidentKey.toLowerCase().includes(query) ||
        incident.title.toLowerCase().includes(query) ||
        incident.serviceName.toLowerCase().includes(query) ||
        String(incident.id).includes(query);

      const matchesSeverity =
        severityFilter === "ALL" ||
        incident.severity === severityFilter;

      const matchesStatus =
        statusFilter === "ALL" ||
        incident.status === statusFilter;

      return (
        matchesSearch &&
        matchesSeverity &&
        matchesStatus
      );
    });
  }, [incidents, search, severityFilter, statusFilter]);

  const visibleIncidents = filteredIncidents.slice(
    0,
    visibleCount,
  );

  const hasMore = visibleCount < filteredIncidents.length;

  const totalIncidents = incidents.length;

  const criticalIncidents = incidents.filter(
    (incident) => incident.severity === "CRITICAL",
  ).length;

  const highIncidents = incidents.filter(
    (incident) => incident.severity === "HIGH",
  ).length;

  const openIncidents = incidents.filter(
    (incident) => incident.status === "OPEN",
  ).length;

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="brand">
          <div className="brand-mark">P</div>

          <div>
            <div className="brand-name">PulseOps</div>

            <div className="brand-subtitle">
              Incident Intelligence Platform
            </div>
          </div>
        </div>

        <div className="topbar-actions">
          <div className="system-status">
            <span className="status-dot" />
            <span>Platform Operational</span>
          </div>

          <button
            type="button"
            className={
              refreshing
                ? "refresh-button refreshing"
                : "refresh-button"
            }
            onClick={() => void refreshIncidents(false)}
            disabled={refreshing}
          >
            <span className="refresh-icon">↻</span>

            {refreshing ? "Refreshing..." : "Refresh"}
          </button>
        </div>
      </header>

      <main className="main-content">
        <section className="page-heading">
          <div>
            <div className="page-eyebrow">
              OPERATIONS CENTER
            </div>

            <h1>Incident Overview</h1>

            <p>
              Detect, investigate and understand production
              incidents from a single workspace.
            </p>
          </div>

          <div className="update-indicator">
            <span className="update-label">
              LAST UPDATED
            </span>

            <span className="update-value">
              {formatLastUpdated(lastUpdated)}
            </span>

            <span className="auto-refresh-label">
              Auto-refreshes every 15s
            </span>
          </div>
        </section>

        {error && (
          <div className="error-banner">
            <div className="error-content">
              <strong>Unable to refresh data</strong>
              <span>{error}</span>
            </div>

            <button
              type="button"
              onClick={() => void refreshIncidents(false)}
              className="error-retry-button"
            >
              Retry
            </button>
          </div>
        )}

        <section className="stats-grid">
          <StatCard
            label="Total Incidents"
            value={totalIncidents}
            description="Recorded incidents"
          />

          <StatCard
            label="Critical"
            value={criticalIncidents}
            description="Requires attention"
            tone="danger"
          />

          <StatCard
            label="High Severity"
            value={highIncidents}
            description="Elevated risk"
            tone="warning"
          />

          <StatCard
            label="Open"
            value={openIncidents}
            description="Currently unresolved"
            tone="danger"
          />
        </section>

        <section className="workspace">
          <div className="incident-list-panel">
            <div className="panel-header">
              <div>
                <div className="section-label">
                  INCIDENT STREAM
                </div>

                <h2>
                  Incidents
                  <span className="result-count">
                    {filteredIncidents.length}
                  </span>
                </h2>
              </div>

              <div className="stream-meta">
                <span className="live-indicator">
                  <span className="live-dot" />
                  LIVE
                </span>

                <span>
                  Showing {visibleIncidents.length} of{" "}
                  {filteredIncidents.length}
                </span>
              </div>
            </div>

            <div className="filters">
              <input
                type="text"
                placeholder="Search incidents..."
                value={search}
                onChange={(event) =>
                  setSearch(event.target.value)
                }
                className="search-input"
              />

              <select
                value={severityFilter}
                onChange={(event) =>
                  setSeverityFilter(event.target.value)
                }
                className="filter-select"
              >
                <option value="ALL">All severities</option>
                <option value="CRITICAL">Critical</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>

              <select
                value={statusFilter}
                onChange={(event) =>
                  setStatusFilter(event.target.value)
                }
                className="filter-select"
              >
                <option value="ALL">All statuses</option>
                <option value="OPEN">Open</option>
                <option value="ACKNOWLEDGED">
                  Acknowledged
                </option>
                <option value="RESOLVED">Resolved</option>
              </select>
            </div>

            <IncidentTable
              incidents={visibleIncidents}
              selectedIncidentId={
                selectedIncident?.id ?? null
              }
              onSelectIncident={(incident) =>
                void loadIncidentDetail(incident.id)
              }
              loading={loadingIncidents}
            />

            {!loadingIncidents && hasMore && (
              <div className="load-more-container">
                <button
                  type="button"
                  className="load-more-button"
                  onClick={() =>
                    setVisibleCount(
                      (current) => current + PAGE_SIZE,
                    )
                  }
                >
                  Load more incidents
                  <span>
                    {Math.min(
                      PAGE_SIZE,
                      filteredIncidents.length -
                        visibleCount,
                    )}
                  </span>
                </button>
              </div>
            )}

            {!loadingIncidents &&
              filteredIncidents.length > 0 &&
              !hasMore && (
                <div className="list-footer">
                  Showing all {filteredIncidents.length} matching
                  incidents
                </div>
              )}
          </div>

          <IncidentDetail
            incident={selectedIncident}
            loading={loadingDetail}
          />
        </section>
      </main>
    </div>
  );
}

export default App;