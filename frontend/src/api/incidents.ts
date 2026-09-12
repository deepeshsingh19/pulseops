import type { Incident, IncidentDetail } from "../types/incident";

const API_BASE_URL = "/api/v1";

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text();

    throw new Error(
      message || `Request failed with status ${response.status}`,
    );
  }

  return response.json();
}

export async function getIncidents(): Promise<Incident[]> {
  const response = await fetch(`${API_BASE_URL}/incidents`);

  return handleResponse<Incident[]>(response);
}

export async function getIncident(
  incidentId: number,
): Promise<IncidentDetail> {
  const response = await fetch(
    `${API_BASE_URL}/incidents/${incidentId}`,
  );

  return handleResponse<IncidentDetail>(response);
}