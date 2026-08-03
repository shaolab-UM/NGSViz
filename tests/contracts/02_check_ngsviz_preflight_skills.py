#!/usr/bin/env python3
"""Regression tests for ngsViz preflight skills."""

from __future__ import annotations

import gzip
import importlib.util
import json
import sqlite3
import subprocess
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
DB_TOOL = ROOT / ".agents/skills/ngsviz-install-db/scripts/manage_ngsviz_db.py"
SETUP_TOOL = ROOT / ".agents/skills/ngsviz-setup/scripts/check_ngsviz_environment.py"
GTF_TOOL = ROOT / ".agents/skills/ngsviz-build-db/scripts/check_gtf_for_ngsviz.py"


def load_module(name: str, path: Path):
    spec = importlib.util.spec_from_file_location(name, path)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Unable to load {path}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


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


class SetupEnvironmentTests(unittest.TestCase):
    def test_empty_coordinate_table_is_invalid(self) -> None:
        setup = load_module("ngsviz_setup", SETUP_TOOL)
        with tempfile.TemporaryDirectory() as temporary:
            database = Path(temporary) / "empty.db"
            create_database(database, records=0)

            result = setup.inspect_database(database)

            self.assertFalse(result["valid"])
            self.assertEqual(result["error"], "COORDINATE_TABLE_EMPTY")
            self.assertEqual(result["empty_tables"], ["hg38_RefSeq"])


class BuildDatabaseTests(unittest.TestCase):
    def test_mixed_ncbi_accession_chromosomes_are_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            gtf = Path(temporary) / "hg38.RefSeq.gtf"
            gtf.write_text(
                "NC_000001.11\tRefSeq\ttranscript\t1\t10\t.\t+\t.\t"
                'gene_name "GENE1"; transcript_id "NM_000001.1";\n'
                "NW_001234.1\tRefSeq\texon\t1\t10\t.\t+\t.\t"
                'gene_name "GENE1"; transcript_id "NM_000001.1";\n',
                encoding="utf-8",
            )

            result = subprocess.run(
                ["python3", str(GTF_TOOL), "--gtf", str(gtf), "--genome", "hg38"],
                capture_output=True,
                text=True,
            )

            self.assertEqual(result.returncode, 1, result.stdout)
            payload = json.loads(result.stdout)
            self.assertIn("NCBI_ACCESSION_CHROMOSOMES_UNSUPPORTED", payload["errors"])


if __name__ == "__main__":
    unittest.main()
