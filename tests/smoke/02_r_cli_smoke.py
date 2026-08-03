#!/usr/bin/env python3
"""Run the ngsViz R CLI contract and regression smoke tests."""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path


def main() -> None:
    if len(sys.argv) != 3:
        raise SystemExit("Usage: 02_r_cli_smoke.py <conda-prefix> <project-root>")
    prefix = Path(sys.argv[1]).resolve()
    root = Path(sys.argv[2]).resolve(strict=True)
    rscript = prefix / "bin/Rscript"
    result = subprocess.run(
        [
            str(rscript),
            str(root / "lib/ngsVizPlotMain.R"),
            "describe",
            "--format",
            "json",
        ],
        capture_output=True,
        text=True,
        check=True,
    )
    payload = json.loads(result.stdout)
    required = {"describe", "validate-plot", "run-plot"}
    if payload["status"] != "success" or not required <= set(payload["data"]["commands"]):
        raise RuntimeError("R CLI contract is incomplete.")
    subprocess.run(
        [str(rscript), str(root / "tests/r/run_r_tests.R")], check=True
    )
    print(f"R CLI smoke passed with {rscript}.")


if __name__ == "__main__":
    main()
