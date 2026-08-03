#!/usr/bin/env python3
"""Run the ngsViz Java CLI smoke test in a Conda prefix."""

from __future__ import annotations

import json
import re
import subprocess
import sys
from pathlib import Path


def find_java(prefix: Path) -> Path:
    candidates = (prefix / "bin/java", prefix / "lib/jvm/bin/java")
    for candidate in candidates:
        if candidate.is_file():
            return candidate
    raise RuntimeError(f"Java executable not found in {prefix}")


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: 01_java_smoke.py <conda-prefix> <ngsviz-jar>")
    java = find_java(Path(sys.argv[1]).resolve())
    jar = Path(sys.argv[2]).resolve(strict=True)
    version = subprocess.run(
        [str(java), "-version"], capture_output=True, text=True, check=True
    )
    match = re.search(r'version "(\d+)', version.stderr)
    if match is None or int(match.group(1)) < 17:
        raise RuntimeError(f"Unsupported Java version: {version.stderr.strip()}")
    result = subprocess.run(
        [str(java), "-jar", str(jar), "describe", "--format", "json"],
        capture_output=True,
        text=True,
        check=True,
    )
    payload = json.loads(result.stdout)
    commands = payload["data"]["commands"]
    required = {"describe", "validate-compute", "run-compute"}
    if payload["status"] != "success" or not required <= set(commands):
        raise RuntimeError("Java CLI contract is incomplete.")
    print(f"Java smoke passed with {java}.")


if __name__ == "__main__":
    main()
