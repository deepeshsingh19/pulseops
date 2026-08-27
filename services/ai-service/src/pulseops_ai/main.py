from fastapi import FastAPI

from pulseops_ai.anomaly.detector import AnomalyDetector
from pulseops_ai.anomaly.features import extract_features
from pulseops_ai.models.requests import AnomalyRequest
from pulseops_ai.models.responses import AnomalyResponse

app = FastAPI(
    title="PulseOps AI Service",
    version="0.1.0",
)

# One rolling model per monitored service.
detectors: dict[str, AnomalyDetector] = {}


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post(
    "/api/v1/anomaly/score",
    response_model=AnomalyResponse,
)
def score_anomaly(
    request: AnomalyRequest,
) -> AnomalyResponse:

    detector = detectors.setdefault(
        request.service_name,
        AnomalyDetector(),
    )

    features = extract_features(request.metadata)

    anomaly_score = detector.score(features)

    # Score the observation against the existing baseline first,
    # then include it in the rolling baseline.
    detector.add_sample(features)

    return AnomalyResponse(
        service_name=request.service_name,
        anomaly_score=anomaly_score,
        anomalous=anomaly_score >= 0.75,
        model_ready=detector.model_ready,
    )