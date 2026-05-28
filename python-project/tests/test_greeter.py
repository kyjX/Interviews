import pytest

from written_test.config import Settings
from written_test.greeter import build_greeting


def test_build_greeting() -> None:
    settings = Settings(greeting="Hi")
    assert build_greeting(settings, "Alice") == "Hi, Alice!"


def test_build_greeting_blank_name() -> None:
    with pytest.raises(ValueError):
        build_greeting(Settings(), "   ")
