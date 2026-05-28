import argparse
import logging

from written_test.config import load_settings
from written_test.greeter import build_greeting


def main() -> None:
    settings = load_settings()
    logging.basicConfig(level=getattr(logging, settings.log_level.upper(), logging.INFO))

    parser = argparse.ArgumentParser(prog="wt-demo", description=settings.app_name)
    parser.add_argument("name", nargs="?", default="World", help="问候对象名称")
    args = parser.parse_args()

    message = build_greeting(settings, args.name)
    logging.info("greeting: %s", message)
    print(message)


if __name__ == "__main__":
    main()
