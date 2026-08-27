from __future__ import annotations

from typing import Any


def extract_features(metadata: dict[str, Any] | None) -> list[float]:
    """
    Convert telemetry metadata into a fixed-length feature vector.

    The model expects the same feature order for every request.
    """

    metadata = metadata or {}

    metric = metadata.get("metric")
    value = float(metadata.get("value", 0.0))

    features = {
        "latency_ms": 0.0,
        "error_rate": 0.0,
        "cpu_usage": 0.0,
        "memory_usage": 0.0,
        "db_pool_usage": 0.0,
        "kafka_lag": 0.0,
    }

    # Single-metric events such as:
    # {"metric": "db_latency_ms", "value": 1450}
    if metric in features:
        features[metric] = value

    # Multi-feature events can provide the values directly.
    for name in features:
        if name in metadata:
            features[name] = float(metadata[name])

    return [
        features["latency_ms"],
        features["error_rate"],
        features["cpu_usage"],
        features["memory_usage"],
        features["db_pool_usage"],
        features["kafka_lag"],
    ]