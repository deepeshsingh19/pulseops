from __future__ import annotations

import os

from google import genai

from pulseops_ai.rca.models import RCARequest, RCAResponse


class GeminiRCAClient:

    def __init__(self) -> None:

        api_key = os.getenv("GEMINI_API_KEY")

        if not api_key:
            self.client = None
            return

        self.client = genai.Client(
            api_key=api_key
        )

        self.model = os.getenv(
            "GEMINI_MODEL",
            "gemini-3.7-flash",
        )

    def analyze(
        self,
        request: RCARequest,
    ) -> RCAResponse | None:

        if self.client is None:
            return None

        prompt = self._build_prompt(request)

        response = self.client.models.generate_content(
            model=self.model,
            contents=prompt,
            config={
                "response_mime_type": "application/json",
                "response_schema": RCAResponse,
                "temperature": 0.1,
            },
        )

        if not response.text:
            raise RuntimeError(
                "Gemini returned an empty response"
            )

        return RCAResponse.model_validate_json(
            response.text
        )

    @staticmethod
    def _build_prompt(
        request: RCARequest,
    ) -> str:

        return f"""
You are the root-cause analysis component of PulseOps.

Analyze the incident using ONLY the evidence supplied below.

Do not invent metrics, traces, services, errors, causes, or
operational facts that are not supported by the evidence.

The response must:
- identify the most likely root cause
- explain the impact
- cite concrete evidence from the supplied data
- recommend practical remediation steps
- keep confidence conservative and evidence-based

Incident:
Incident key: {request.incident_key}
Service: {request.service_name}
Title: {request.title}
Severity: {request.severity}
Description: {request.description}

Metrics:
{request.metrics}

Telemetry:
{request.telemetry}

Traces:
{request.traces}

Existing evidence:
{request.evidence}

Return only the requested structured RCA response.
"""