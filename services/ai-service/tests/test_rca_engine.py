from pulseops_ai.rca.engine import RCAEngine
from pulseops_ai.rca.models import RCARequest


def build_request(
    metrics: dict,
    evidence: list[str] | None = None,
) -> RCARequest:
    return RCARequest(
        incident_key="TEST-001",
        service_name="payment-service",
        title="Payment service degradation detected",
        severity="CRITICAL",
        description="Database-related degradation detected.",
        metrics=metrics,
        telemetry=[],
        traces=[],
        evidence=evidence or [],
    )


def test_database_saturation_produces_high_confidence_rca():
    engine = RCAEngine()

    request = build_request(
        metrics={
            "db_latency_ms": 1431,
            "db_pool_usage": 99.7,
            "http_5xx_rate": 12.8,
        }
    )

    result = engine._deterministic_analysis(request)

    assert result.confidence == 0.91
    assert "database connection saturation" in result.root_cause
    assert result.impact
    assert len(result.evidence) == 3
    assert len(result.recommended_actions) >= 3


def test_partial_database_evidence_produces_lower_confidence():
    engine = RCAEngine()

    request = build_request(
        metrics={
            "db_latency_ms": 1431,
        }
    )

    result = engine._deterministic_analysis(request)

    assert result.confidence == 0.72
    assert "database latency" in result.root_cause.lower()
    assert len(result.recommended_actions) == 2


def test_insufficient_evidence_returns_safe_default():
    engine = RCAEngine()

    request = build_request(
        metrics={}
    )

    result = engine._deterministic_analysis(request)

    assert result.confidence == 0.40
    assert (
        result.root_cause
        == "Insufficient evidence to determine root cause."
    )
    assert result.impact == "Service degradation detected."
    assert result.recommended_actions == []


def test_http_error_signal_produces_targeted_rca():
    engine = RCAEngine()

    request = build_request(
        metrics={
            "http_5xx_rate": 12.8,
        }
    )

    result = engine._deterministic_analysis(request)

    assert result.confidence == 0.65
    assert "HTTP error rate" in result.root_cause
    assert len(result.recommended_actions) == 2