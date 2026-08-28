from typing import Any

from pydantic import AliasChoices, BaseModel, Field


class AnomalyRequest(BaseModel):
    service_name: str = Field(
        min_length=1,
        validation_alias=AliasChoices(
            "service_name",
            "serviceName",
        ),
    )

    metadata: dict[str, Any] = Field(
        default_factory=dict
    )