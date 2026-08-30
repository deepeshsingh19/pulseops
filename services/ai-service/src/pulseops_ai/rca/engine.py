from __future__ import annotations

from typing import Any

from pulseops_ai.rca.llm import GeminiRCAClient
from pulseops_ai.rca.models import RCARequest, RCAResponse


class RCAEngine:

    def __init__(self) -> None:

        self.llm_client = GeminiRCAClient()

    def analyze(
        self,
        request: RCARequest,
    ) -> RCAResponse:

        deterministic_result = (
            self._deterministic_analysis(request)
        )

        if self.llm_client.client is None:
            return deterministic_result

        try:

            llm_result = self.llm_client.analyze(
                request
            )

            if llm_result is None:
                return deterministic_result

            return self._merge_results(
                deterministic_result,
                llm_result,
            )

        except Exception as exception:

            print(
                "Gemini RCA unavailable; "
                "using deterministic RCA: "
                f"{exception}"
            )

            return deterministic_result

    def _deterministic_analysis(
        self,
        request: RCARequest,
    ) -> RCAResponse:

        evidence: list[str] = []
        root_cause = (
            "Insufficient evidence to determine root cause."
        )
        impact = "Service degradation detected."
        recommended_actions: list[str] = []
        confidence = 0.40

        metrics = request.metrics

        db_latency = self._numeric_value(
            metrics.get("db_latency_ms")
        )

        db_pool_usage = self._numeric_value(
            metrics.get("db_pool_usage")
        )

        error_rate = self._numeric_value(
            metrics.get("http_5xx_rate")
        )

        if db_latency > 1000:

            evidence.append(
                f"Database latency is elevated at "
                f"{db_latency:.0f} ms."
            )

        if db_pool_usage > 90:

            evidence.append(
                f"Database connection pool usage is "
                f"{db_pool_usage:.1f}%."
            )

        if error_rate > 5:

            evidence.append(
                f"HTTP 5xx error rate is "
                f"{error_rate:.1f}%."
            )

        if (
            db_latency > 1000
            and db_pool_usage > 90
            and error_rate > 5
        ):

            root_cause = (
                "The payment service is likely experiencing "
                "database connection saturation, causing "
                "elevated database latency and downstream "
                "HTTP 5xx errors."
            )

            impact = (
                "Payment requests are failing or experiencing "
                "significant latency."
            )

            recommended_actions = [
                "Inspect slow database queries.",
                "Review database connection pool size and saturation.",
                "Check database CPU, locks and active connections.",
                "Investigate whether recent traffic caused connection exhaustion.",
            ]

            confidence = 0.91

        elif db_latency > 1000:

            root_cause = (
                "Elevated database latency is the strongest "
                "available signal for the incident."
            )

            recommended_actions = [
                "Inspect slow database queries.",
                "Check database CPU and active connections.",
            ]

            confidence = 0.72

        elif error_rate > 5:

            root_cause = (
                "The service is experiencing an elevated "
                "HTTP error rate, but the available evidence "
                "does not identify the underlying dependency."
            )

            recommended_actions = [
                "Inspect recent application logs and traces.",
                "Identify the failing downstream dependency.",
            ]

            confidence = 0.65

        return RCAResponse(
            incident_key=request.incident_key,
            root_cause=root_cause,
            confidence=confidence,
            impact=impact,
            evidence=evidence or request.evidence,
            recommended_actions=recommended_actions,
        )

    @staticmethod
    def _merge_results(
        deterministic: RCAResponse,
        llm: RCAResponse,
    ) -> RCAResponse:

        evidence = (
            llm.evidence
            if llm.evidence
            else deterministic.evidence
        )

        actions = (
            llm.recommended_actions
            if llm.recommended_actions
            else deterministic.recommended_actions
        )

        return RCAResponse(
            incident_key=deterministic.incident_key,
            root_cause=llm.root_cause,
            confidence=min(
                max(llm.confidence, 0.0),
                0.99,
            ),
            impact=llm.impact,
            evidence=evidence,
            recommended_actions=actions,
        )

    @staticmethod
    def _numeric_value(
        value: Any,
    ) -> float:

        if isinstance(value, bool):
            return 0.0

        if isinstance(value, (int, float)):
            return float(value)

        try:
            return float(value)

        except (TypeError, ValueError):
            return 0.0