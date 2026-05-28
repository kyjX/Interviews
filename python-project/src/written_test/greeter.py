from written_test.config import Settings


def build_greeting(settings: Settings, name: str) -> str:
    if not name.strip():
        raise ValueError("name must not be blank")
    return f"{settings.greeting}, {name.strip()}!"
