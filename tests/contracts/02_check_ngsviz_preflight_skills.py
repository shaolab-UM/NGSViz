#!/usr/bin/env python3
"""Regression tests for ngsViz preflight skills."""

from __future__ import annotations

import gzip
import json
import sqlite3
import subprocess
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DB_TOOL = ROOT / ".agents/skills/ngsviz-install-db/scripts/manage_ngsviz_db.py"


def create_database(path: Path, genome: str = "hg38", records: int = 1) -> None:
    with sqlite3.connect(path) as connection:
        connection.execute(
            "CREATE TABLE defaultTbl (Species TEXT, Genome TEXT, DB TEXT, CoordinateTblName TEXT)"
        )
        connection.execute(
            "INSERT INTO defaultTbl VALUES (?, ?, ?, ?)",
            ("Homo_sapiens", genome, "RefSeq", f"{genome}_RefSeq"),
        )
        connection.execute(f'CREATE TABLE "{genome}_RefSeq" (Chromosome TEXT)')
        for _ in range(records):
            connection.execute(f'INSERT INTO "{genome}_RefSeq" VALUES ("chr1")')


class InstallDatabaseTests(unittest.TestCase):
    def test_prepare_decompresses_and_verifies_prebuilt_database(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            workspace = Path(temporary)
            source_db = workspace / "source.db"
            archive = workspace / "NGSViz_hg38_RefSeq.db.gz"
            output = workspace / "prebuilt" / "NGSViz_hg38_RefSeq.db"
            create_database(source_db)
            with source_db.open("rb") as source, gzip.open(archive, "wb") as target:
                target.write(source.read())

            result = subprocess.run(
                [
                    "python3",
                    str(DB_TOOL),
                    "prepare",
                    "--source",
                    str(archive),
                    "--output",
                    str(output),
                    "--genome",
                    "hg38",
                ],
                capture_output=True,
                text=True,
            )

            self.assertEqual(result.returncode, 0, result.stderr or result.stdout)
            payload = json.loads(result.stdout)
            self.assertTrue(payload["valid"])
            self.assertTrue(payload["decompressed"])
            self.assertTrue(archive.exists())
            self.assertTrue(output.exists())
            self.assertEqual(output.read_bytes()[:16], b"SQLite format 3\x00")


if __name__ == "__main__":
    unittest.main()
