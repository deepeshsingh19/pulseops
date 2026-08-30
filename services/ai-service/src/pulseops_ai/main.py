from fastapi import FastAPI, HTTPException, Request

from pulseops_ai.anomaly.detector import AnomalyDetector
from pulseops_ai.anomaly.features import extract_features
from pulseops_ai.models.requests import AnomalyRequest
from pulseops_ai.models.responses import AnomalyResponse
from pulseops_ai.rca.engine import RCAEngine
from pulseops_ai.rca.models import RCARequest, RCAResponse

app = FastAPI(
    title="PulseOps AI Service",
    version="0.2.0",
)

detectors: dict[str, AnomalyDetector] = {}
rca_engine = RCAEngine()


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

    anomaly_score = detector.score(features)

    detector.add_sample(features)

    return AnomalyResponse(
        service_name=payload.service_name,
        anomaly_score=anomaly_score,
        anomalous=anomaly_score >= 0.75,
        model_ready=detector.model_ready,
    )


@app.post(
    "/api/v1/rca/analyze",
    response_model=RCAResponse,
)
def analyze_root_cause(
    request: RCARequest,
) -> RCAResponse:

    try:
        return rca_engine.analyze(request)

    except Exception as exception:

        raise HTTPException(
            status_code=500,
            detail=f"RCA analysis failed: {exception}",
        )