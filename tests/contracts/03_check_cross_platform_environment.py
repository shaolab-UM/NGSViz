#!/usr/bin/env python3
"""Contract tests for cross-platform Conda environment files."""

from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
PORTABLE_ENVIRONMENT = ROOT / "environment.yml"
LINUX_HISTORY = ROOT / "environment-linux-64.yml"
LOCK_DIRECTORY = ROOT / "conda-lock"
LOCK_SCRIPT = ROOT / "scripts/01_generate_conda_locks.sh"
POST_INSTALL_SCRIPT = ROOT / "scripts/02_install_r_post_dependencies.R"
TARGET_PLATFORMS = ("linux-64", "osx-64", "osx-arm64")
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
    "bioconductor-rtracklayer",
    "r-biocmanager",
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
        self.assertIn("bioconda", content)
        self.assertIn("nodefaults", content)
        self.assertNotIn("linux-64", content)
        self.assertNotIn("sysroot", content)
        self.assertNotIn("prefix:", content)
        self.assertNotRegex(content, r"=[^\s=]+=[^\s=]+")

    def test_lock_generation_targets_all_supported_platforms(self) -> None:
        content = LOCK_SCRIPT.read_text(encoding="utf-8")

        self.assertIn("conda-lock", content)
        for platform in TARGET_PLATFORMS:
            self.assertIn(f'--platform "{platform}"', content)

    def test_arm64_post_install_is_versioned(self) -> None:
        content = POST_INSTALL_SCRIPT.read_text(encoding="utf-8")

        self.assertIn('bioconductor_version <- "3.20"', content)
        self.assertIn('EnrichedHeatmap = "1.36.0"', content)
        self.assertIn("BiocManager::install", content)
        self.assertIn("packageVersion(package)", content)

    def test_explicit_locks_exist_for_each_target_platform(self) -> None:
        for platform in TARGET_PLATFORMS:
            with self.subTest(platform=platform):
                lock = LOCK_DIRECTORY / f"{platform}.lock"
                content = lock.read_text(encoding="utf-8")
                self.assertIn("@EXPLICIT", content)
                self.assertIn(f"/{platform}/", content)
                self.assertRegex(content, r"#[0-9a-f]{32}(?:\n|$)")

if __name__ == "__main__":
    unittest.main()
