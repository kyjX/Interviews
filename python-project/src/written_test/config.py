from dataclasses import dataclass
import os


@dataclass(frozen=True)
class Settings:
    app_name: str = "written-test-python"
    log_level: str = "INFO"
    greeting: str = "Hello"


def load_settings() -> Settings:
    return Settings(
        log_level=os.getenv("LOG_LEVEL", "INFO"),
        greeting=os.getenv("GREETING", "Hello"),
    )
