from pydantic import BaseModel


class AnomalyResponse(BaseModel):
    service_name: str
    anomaly_score: float
    anomalous: bool
    model_ready: bool