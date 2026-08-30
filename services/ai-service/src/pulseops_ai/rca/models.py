from typing import Any

from pydantic import BaseModel, Field


class RCARequest(BaseModel):
    incident_key: str
    service_name: str
    title: str
    severity: str
    description: str
    telemetry: list[dict[str, Any]] = Field(
        default_factory=list
    )
    metrics: dict[str, Any] = Field(
        default_factory=dict
    )
    traces: list[dict[str, Any]] = Field(
        default_factory=list
    )
    evidence: list[str] = Field(
        default_factory=list
    )


class RCAResponse(BaseModel):
    incident_key: str
    root_cause: str
    confidence: float = Field(
        ge=0.0,
        le=1.0,
    )
    impact: str
    evidence: list[str]
    recommended_actions: list[str]