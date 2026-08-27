from typing import Any

from pydantic import BaseModel, Field


class AnomalyRequest(BaseModel):
    service_name: str = Field(min_length=1)
    metadata: dict[str, Any] = Field(default_factory=dict)