import type { Incident, IncidentDetail } from "../types/incident";

const API_BASE_URL = "/api/v1";

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === "true";

const API_BASE_URL_RESOLVED =
  import.meta.env.VITE_API_BASE_URL || API_BASE_URL;

let demoIncidentsPromise: Promise<IncidentDetail[]> | null = null;

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text();

    throw new Error(
      message || `Request failed with status ${response.status}`,
    );
  }

  return response.json();
}

async function loadDemoIncidents(): Promise<IncidentDetail[]> {
  if (!demoIncidentsPromise) {
    demoIncidentsPromise = fetch("/demo-incidents.json").then((response) =>
      handleResponse<IncidentDetail[]>(response),
    );
  }

  return demoIncidentsPromise;
}

export async function getIncidents(): Promise<Incident[]> {
  if (DEMO_MODE) {
    return loadDemoIncidents();
  }

  const response = await fetch(`${API_BASE_URL_RESOLVED}/incidents`);

  return handleResponse<Incident[]>(response);
}

export async function getIncident(
  incidentId: number,
): Promise<IncidentDetail> {
  if (DEMO_MODE) {
    const incidents = await loadDemoIncidents();

    const incident = incidents.find((item) => item.id === incidentId);

    if (!incident) {
      throw new Error(`Demo incident ${incidentId} was not found.`);
    }

    return incident;
  }

  const response = await fetch(
    `${API_BASE_URL_RESOLVED}/incidents/${incidentId}`,
  );

  return handleResponse<IncidentDetail>(response);
}