from pydantic import BaseModel, Field


class AnomalyResponse(BaseModel):
    """
    Response returned by the AI service.

    The Python code uses snake_case internally, while the public
    API contract uses camelCase to match the Java services.
    """

    service_name: str = Field(
        serialization_alias="serviceName"
    )

    anomaly_score: float = Field(
        serialization_alias="anomalyScore"
    )

    anomalous: bool

    model_ready: bool = Field(
        serialization_alias="modelReady"
    )