from fastapi import FastAPI, HTTPException, Request

from pulseops_ai.anomaly.detector import AnomalyDetector
from pulseops_ai.anomaly.features import extract_features
from pulseops_ai.models.requests import AnomalyRequest
from pulseops_ai.models.responses import AnomalyResponse

app = FastAPI(
    title="PulseOps AI Service",
    version="0.1.0",
)

detectors: dict[str, AnomalyDetector] = {}


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post(
    "/api/v1/anomaly/score",
    response_model=AnomalyResponse,
    response_model_by_alias=True,
)
async def score_anomaly(
    request: Request,
) -> AnomalyResponse:

    raw_body = await request.body()

    print(
        f"Received anomaly request: "
        f"bytes={len(raw_body)}, "
        f"content_type={request.headers.get('content-type')}"
    )

    if not raw_body:
        raise HTTPException(
            status_code=422,
            detail="Request body was empty",
        )

    try:
        payload = AnomalyRequest.model_validate_json(
            raw_body
        )
    except Exception as exception:
        raise HTTPException(
            status_code=422,
            detail=f"Invalid request body: {exception}",
        )

    detector = detectors.setdefault(
        payload.service_name,
        AnomalyDetector(),
    )

    features = extract_features(
        payload.metadata
    )

    # Score against the existing baseline first.
    anomaly_score = detector.score(features)

    # Then include the observation in the rolling baseline.
    detector.add_sample(features)

    return AnomalyResponse(
        service_name=payload.service_name,
        anomaly_score=anomaly_score,
        anomalous=anomaly_score >= 0.75,
        model_ready=detector.model_ready,
    )