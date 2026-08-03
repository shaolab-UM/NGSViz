#!/usr/bin/env python3
"""Inspect ngsViz databases and parse the README pre-built DB catalog."""

from __future__ import annotations

import argparse
import gzip
import json
import os
import re
import shutil
import sqlite3
import tempfile
from pathlib import Path
from typing import Any


CATALOG_ROW = re.compile(
    r"^\|\s*(?P<organism>[^|]+?)\s*\|\s*(?P<species>[^|]+?)\s*\|\s*"
    r"\[(?P<genome>[^]]+)\]\((?P<url>https?://[^)]+)\)\s*\|\s*(?P<source>[^|]+?)\s*\|$"
)


def parse_catalog(readme: Path) -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for line in readme.read_text(encoding="utf-8").splitlines():
        match = CATALOG_ROW.match(line)
        if match:
            rows.append({key: value.strip() for key, value in match.groupdict().items()})
    return rows


def quote_identifier(value: str) -> str:
    return '"' + value.replace('"', '""') + '"'


def inspect_database(path: Path) -> dict[str, Any]:
    result: dict[str, Any] = {
        "path": str(path.resolve()),
        "valid": False,
        "quick_check": None,
        "entries": [],
        "errors": [],
    }
    if not path.is_file():
        result["errors"].append("DATABASE_NOT_FOUND")
        return result
    try:
        uri = f"file:{path.resolve()}?mode=ro"
        with sqlite3.connect(uri, uri=True) as connection:
            quick = connection.execute("PRAGMA quick_check").fetchone()
            result["quick_check"] = quick[0] if quick else None
            if result["quick_check"] != "ok":
                result["errors"].append("SQLITE_INTEGRITY_FAILED")
                return result
            tables = {
                row[0]
                for row in connection.execute(
                    "SELECT name FROM sqlite_master WHERE type='table'"
                )
            }
            if "defaultTbl" not in tables:
                result["errors"].append("DEFAULT_TABLE_MISSING")
                return result
            metadata = connection.execute(
                "SELECT DISTINCT Species, Genome, DB, CoordinateTblName FROM defaultTbl "
                "ORDER BY Species, Genome, DB, CoordinateTblName"
            ).fetchall()
            for species, genome, database, coordinate_table in metadata:
                entry = {
                    "species": species,
                    "genome": genome,
                    "database": database,
                    "coordinate_table": coordinate_table,
                    "table_exists": coordinate_table in tables,
                    "record_count": None,
                }
                if coordinate_table in tables:
                    count = connection.execute(
                        f"SELECT COUNT(*) FROM {quote_identifier(coordinate_table)}"
                    ).fetchone()
                    entry["record_count"] = count[0] if count else 0
                if not entry["table_exists"]:
                    result["errors"].append(f"COORDINATE_TABLE_MISSING:{coordinate_table}")
                elif entry["record_count"] == 0:
                    result["errors"].append(f"COORDINATE_TABLE_EMPTY:{coordinate_table}")
                result["entries"].append(entry)
            if not metadata:
                result["errors"].append("DEFAULT_TABLE_EMPTY")
            result["valid"] = not result["errors"]
    except sqlite3.Error as error:
        result["errors"].append(f"SQLITE_ERROR:{error}")
    return result


def prepare_database(source: Path, output: Path, genome: str) -> dict[str, Any]:
    result: dict[str, Any] = {
        "source": str(source.resolve()),
        "output": str(output.resolve()),
        "decompressed": False,
        "valid": False,
        "errors": [],
    }
    if not source.is_file():
        result["errors"].append("SOURCE_NOT_FOUND")
        return result
    if output.exists():
        result["errors"].append("OUTPUT_ALREADY_EXISTS")
        return result

    output.parent.mkdir(parents=True, exist_ok=True)
    descriptor, temporary_name = tempfile.mkstemp(
        prefix=f".{output.name}.", dir=output.parent
    )
    os.close(descriptor)
    temporary = Path(temporary_name)
    try:
        with source.open("rb") as handle:
            compressed = handle.read(2) == b"\x1f\x8b"
        opener = gzip.open if compressed else Path.open
        with opener(source, "rb") as input_handle, temporary.open("wb") as output_handle:
            shutil.copyfileobj(input_handle, output_handle)
        inspection = inspect_database(temporary)
        matching = [
            entry for entry in inspection["entries"] if entry["genome"] == genome
        ]
        if not matching:
            inspection["errors"].append("REQUESTED_GENOME_MISSING")
            inspection["valid"] = False
        result.update(inspection)
        result.update(
            {
                "source": str(source.resolve()),
                "output": str(output.resolve()),
                "decompressed": compressed,
                "requested_genome": genome,
                "matching_entries": matching,
            }
        )
        if result["valid"]:
            os.replace(temporary, output)
    except (OSError, gzip.BadGzipFile) as error:
        result["errors"].append(f"PREPARE_ERROR:{error}")
        result["valid"] = False
    finally:
        if temporary.exists():
            temporary.unlink()
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    subparsers = parser.add_subparsers(dest="command", required=True)
    catalog = subparsers.add_parser("catalog")
    catalog.add_argument("--readme", required=True, type=Path)
    catalog.add_argument("--species")
    catalog.add_argument("--genome")
    inspect = subparsers.add_parser("inspect")
    inspect.add_argument("--db", required=True, type=Path)
    verify = subparsers.add_parser("verify")
    verify.add_argument("--db", required=True, type=Path)
    verify.add_argument("--genome", required=True)
    prepare = subparsers.add_parser("prepare")
    prepare.add_argument("--source", required=True, type=Path)
    prepare.add_argument("--output", required=True, type=Path)
    prepare.add_argument("--genome", required=True)
    args = parser.parse_args()

    if args.command == "catalog":
        rows = parse_catalog(args.readme)
        if args.species:
            query = args.species.casefold().replace(" ", "_")
            rows = [
                row
                for row in rows
                if query in {row["species"].casefold(), row["organism"].casefold().replace(" ", "_")}
            ]
        if args.genome:
            rows = [row for row in rows if row["genome"].casefold() == args.genome.casefold()]
        output = {"count": len(rows), "entries": rows}
        print(json.dumps(output, ensure_ascii=False, indent=2))
        return 0 if rows else 1

    if args.command == "prepare":
        result = prepare_database(args.source, args.output, args.genome)
        print(json.dumps(result, ensure_ascii=False, indent=2))
        return 0 if result["valid"] else 1

    result = inspect_database(args.db)
    if args.command == "verify":
        matching = [entry for entry in result["entries"] if entry["genome"] == args.genome]
        result["requested_genome"] = args.genome
        result["matching_entries"] = matching
        if not matching:
            result["errors"].append("REQUESTED_GENOME_MISSING")
            result["valid"] = False
    print(json.dumps(result, ensure_ascii=False, indent=2))
    return 0 if result["valid"] else 1


if __name__ == "__main__":
    raise SystemExit(main())
