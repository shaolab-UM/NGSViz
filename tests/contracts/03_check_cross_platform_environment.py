#!/usr/bin/env python3
"""Contract tests for cross-platform Conda environment files."""

from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
PORTABLE_ENVIRONMENT = ROOT / "environment.yml"
LINUX_HISTORY = ROOT / "environment-linux-64.yml"
REQUIRED_DIRECT_DEPENDENCIES = {
    "openjdk",
    "r-base",
    "r-aplot",
    "r-bs4dash",
    "r-circlize",
    "r-colorspace",
    "r-corrplot",
    "r-cowplot",
    "r-data.table",
    "r-dplyr",
    "r-dt",
    "r-factominer",
    "r-fresh",
    "r-future",
    "r-ggh4x",
    "r-ggplot2",
    "r-ggrepel",
    "r-ggsci",
    "r-here",
    "r-jsonlite",
    "r-leaflet",
    "r-markdown",
    "r-plotly",
    "r-processx",
    "r-promises",
    "r-purrr",
    "r-rcolorbrewer",
    "r-rcpproll",
    "r-readr",
    "r-reshape2",
    "r-rsqlite",
    "r-scales",
    "r-shiny",
    "r-shinyfiles",
    "r-tidyr",
    "r-tidyverse",
    "r-zoo",
    "bioconductor-biomart",
    "bioconductor-enrichedheatmap",
    "bioconductor-rtracklayer",
}


def dependency_names(content: str) -> set[str]:
    names = set()
    for line in content.splitlines():
        match = re.match(r"^\s{2}-\s+([a-z0-9._-]+)", line)
        if match:
            names.add(match.group(1))
    return names


class EnvironmentSpecificationTests(unittest.TestCase):
    def test_linux_export_is_preserved_as_history(self) -> None:
        content = LINUX_HISTORY.read_text(encoding="utf-8")

        self.assertIn("binutils_impl_linux-64", content)
        self.assertIn("sysroot_linux-64", content)
        self.assertIn("prefix: /opt/conda/envs/ngsViz", content)

    def test_portable_environment_contains_direct_dependencies_only(self) -> None:
        content = PORTABLE_ENVIRONMENT.read_text(encoding="utf-8")
        dependencies = dependency_names(content)

        self.assertTrue(REQUIRED_DIRECT_DEPENDENCIES <= dependencies)
        self.assertIn("nodefaults", content)
        self.assertNotIn("linux-64", content)
        self.assertNotIn("sysroot", content)
        self.assertNotIn("prefix:", content)
        self.assertNotRegex(content, r"=[^\s=]+=[^\s=]+")

if __name__ == "__main__":
    unittest.main()
