from __future__ import annotations

from collections import deque

import numpy as np
from sklearn.ensemble import IsolationForest


class AnomalyDetector:
    """
    Maintains a rolling baseline for one service and scores
    new telemetry observations against that baseline.
    """

    def __init__(self, window_size: int = 100) -> None:
        self.window_size = window_size
        self._samples: deque[list[float]] = deque(maxlen=window_size)

        self._model = IsolationForest(
            n_estimators=100,
            contamination="auto",
            random_state=42,
        )

        self._fitted = False

    @property
    def model_ready(self) -> bool:
        return self._fitted

    def add_sample(self, features: list[float]) -> None:
        self._samples.append(features)

        # We need enough baseline observations before fitting.
        if len(self._samples) >= 20:
            self._fit()

    def score(self, features: list[float]) -> float:
        if not self._fitted:
            return 0.0

        raw_score = self._model.decision_function(
            np.array([features])
        )[0]

        # Convert the Isolation Forest score into a simple
        # 0..1 value where larger means more unusual.
        anomaly_score = 0.5 - raw_score

        return float(np.clip(anomaly_score, 0.0, 1.0))

    def is_anomalous(
        self,
        features: list[float],
        threshold: float = 0.75,
    ) -> bool:
        return self.score(features) >= threshold

    def _fit(self) -> None:
        data = np.array(list(self._samples))

        self._model.fit(data)
        self._fitted = True