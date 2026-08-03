#!/usr/bin/env python3
"""Audit and optionally repair an ngsViz system configuration."""

from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
import shutil
import sqlite3
import subprocess
import tempfile
from pathlib import Path
from typing import Any


def read_config(path: Path) -> tuple[dict[str, Any], list[str]]:
    if not path.exists():
        return {}, []
    try:
        value = json.loads(path.read_text(encoding="utf-8"))
        if not isinstance(value, dict):
            return {}, ["CONFIG_NOT_OBJECT"]
        return value, []
    except (OSError, json.JSONDecodeError):
        return {}, ["CONFIG_INVALID_JSON"]


def inspect_tool_path(path: Path) -> dict[str, Any]:
    readme = path / "README.md"
    library = path / "lib"
    missing: list[str] = []
    if not readme.is_file() or not os.access(readme, os.R_OK):
        missing.append("README.md")
    if not library.is_dir() or not os.access(library, os.R_OK | os.X_OK):
        missing.append("lib/")
    return {"path": str(path), "valid": not missing, "missing": missing}


def java_version(java_path: Path) -> tuple[int | None, str]:
    try:
        result = subprocess.run(
            [str(java_path), "-version"], capture_output=True, text=True, timeout=10
        )
    except (OSError, subprocess.SubprocessError):
        return None, ""
    output = (result.stderr or result.stdout).strip()
    match = re.search(r'version\s+"(\d+)(?:\.(\d+))?', output)
    if not match:
        return None, output
    major = int(match.group(1))
    if major == 1 and match.group(2):
        major = int(match.group(2))
    return major, output.splitlines()[0] if output else ""


def java_candidates(configured: str | None) -> list[dict[str, Any]]:
    paths: list[Path] = []
    if configured:
        paths.append(Path(configured).expanduser())
    discovered = shutil.which("java")
    if discovered:
        paths.append(Path(discovered))
    java_home = Path("/usr/libexec/java_home")
    if java_home.exists():
        try:
            result = subprocess.run(
                [str(java_home), "-v", "17+"], capture_output=True, text=True, timeout=10
            )
            if result.returncode == 0 and result.stdout.strip():
                paths.append(Path(result.stdout.strip()) / "bin" / "java")
        except subprocess.SubprocessError:
            pass
    rows: list[dict[str, Any]] = []
    seen: set[str] = set()
    for path in paths:
        resolved = str(path.resolve()) if path.exists() else str(path.absolute())
        if resolved in seen:
            continue
        seen.add(resolved)
        major, version = java_version(Path(resolved))
        rows.append(
            {
                "path": resolved,
                "exists": Path(resolved).is_file(),
                "executable": os.access(resolved, os.X_OK),
                "major": major,
                "version": version,
                "compatible": bool(major is not None and major >= 17),
            }
        )
    return rows


def quote_identifier(value: str) -> str:
    return '"' + value.replace('"', '""') + '"'


def inspect_database(path: Path) -> dict[str, Any]:
    row: dict[str, Any] = {"path": str(path), "valid": False, "genomes": [], "species": []}
    if not path.is_file() or not os.access(path, os.R_OK):
        return row
    try:
        uri = f"file:{path.resolve()}?mode=ro"
        with sqlite3.connect(uri, uri=True) as connection:
            quick = connection.execute("PRAGMA quick_check").fetchone()
            if not quick or quick[0] != "ok":
                row["error"] = "SQLITE_INTEGRITY_FAILED"
                return row
            columns = {
                item[1] for item in connection.execute("PRAGMA table_info(defaultTbl)")
            }
            required = {"Species", "CoordinateTblName", "Genome"}
            if not required.issubset(columns):
                row["error"] = "DEFAULT_TABLE_INVALID"
                return row
            metadata = connection.execute(
                "SELECT DISTINCT Species, Genome, CoordinateTblName FROM defaultTbl"
            ).fetchall()
            if not metadata:
                row["error"] = "DEFAULT_TABLE_EMPTY"
                return row
            tables = {
                item[0]
                for item in connection.execute(
                    "SELECT name FROM sqlite_master WHERE type='table'"
                )
            }
            coordinate_tables = {item[2] for item in metadata}
            missing = sorted(coordinate_tables - tables)
            if missing:
                row["error"] = "COORDINATE_TABLE_MISSING"
                row["missing_tables"] = missing
                return row
            empty = sorted(
                table
                for table in coordinate_tables
                if connection.execute(
                    f"SELECT COUNT(*) FROM {quote_identifier(table)}"
                ).fetchone()[0]
                == 0
            )
            if empty:
                row["error"] = "COORDINATE_TABLE_EMPTY"
                row["empty_tables"] = empty
                return row
            row["valid"] = True
            row["genomes"] = sorted({item[1] for item in metadata})
            row["species"] = sorted({item[0] for item in metadata})
            row["coordinate_tables"] = sorted(coordinate_tables)
    except sqlite3.Error as error:
        row["error"] = f"SQLITE_ERROR: {error}"
    return row


def database_candidates(tool_path: Path, configured: str | None) -> list[dict[str, Any]]:
    paths: list[Path] = []
    if configured:
        candidate = Path(configured).expanduser()
        paths.append(candidate if candidate.is_absolute() else tool_path / candidate)
    database_dir = tool_path / "database"
    if database_dir.is_dir():
        paths.extend(sorted(database_dir.rglob("*.db")))
    rows: list[dict[str, Any]] = []
    seen: set[str] = set()
    for index, path in enumerate(paths):
        resolved = path.resolve()
        if str(resolved) in seen:
            continue
        seen.add(str(resolved))
        row = inspect_database(resolved)
        row["configured"] = bool(configured and index == 0)
        rows.append(row)
    return rows


def choose_database(rows: list[dict[str, Any]], genome: str | None) -> tuple[dict[str, Any] | None, bool]:
    valid = [row for row in rows if row["valid"]]
    if genome:
        valid = [row for row in valid if genome in row["genomes"]]
    configured = [row for row in valid if row.get("configured")]
    if len(configured) == 1:
        return configured[0], False
    return (valid[0], False) if len(valid) == 1 else (None, len(valid) > 1)


def inspect_jar(java_path: Path, jar_path: Path) -> dict[str, Any]:
    result: dict[str, Any] = {
        "path": str(jar_path),
        "runnable": False,
        "cli_compatible": False,
        "version_output": "",
    }
    try:
        version_process = subprocess.run(
            [str(java_path), "-jar", str(jar_path), "-V"],
            capture_output=True,
            text=True,
            timeout=20,
        )
        output = "\n".join(
            part.strip() for part in (version_process.stdout, version_process.stderr) if part.strip()
        )
        result["version_exit_code"] = version_process.returncode
        result["version_output"] = output
        result["runnable"] = version_process.returncode == 0 and bool(output)
        describe_process = subprocess.run(
            [str(java_path), "-jar", str(jar_path), "describe", "--format", "json"],
            capture_output=True,
            text=True,
            timeout=20,
        )
        result["describe_exit_code"] = describe_process.returncode
        contract = json.loads(describe_process.stdout)
        commands = set(contract.get("data", {}).get("commands", []))
        required = {"describe", "validate-compute", "run-compute"}
        result["commands"] = sorted(commands)
        result["cli_compatible"] = (
            describe_process.returncode == 0
            and contract.get("status") == "success"
            and required.issubset(commands)
        )
    except (OSError, subprocess.SubprocessError, json.JSONDecodeError) as error:
        result["error"] = str(error)
    return result


def choose_jar(tool_path: Path, configured: str | None) -> tuple[Path | None, bool, list[str]]:
    jars = sorted(path for path in tool_path.glob("NGSViz-*.jar") if path.is_file())
    if configured:
        configured_path = tool_path / configured
        if configured_path in jars:
            return configured_path, False, [path.name for path in jars]
    return (jars[0], False, [jars[0].name]) if len(jars) == 1 else (None, len(jars) > 1, [path.name for path in jars])


def proposed_config(
    original: dict[str, Any], tool_path: Path, java_path: Path, jar_path: Path, db_path: Path
) -> dict[str, Any]:
    config = json.loads(json.dumps(original)) if original else {}
    version = config.setdefault("versionNum", {})
    tools = config.setdefault("toolParas", {})
    jar_match = re.match(r"NGSViz-(.+)\.jar$", jar_path.name)
    version["version"] = jar_match.group(1) if jar_match else version.get("version", "unknown")
    tools.update(
        {
            "tool_path": str(tool_path),
            "db_path": str(db_path),
            "tool_name": jar_path.name,
            "java_path": str(java_path),
        }
    )
    return config


def atomic_write(path: Path, config: dict[str, Any]) -> str | None:
    backup: str | None = None
    if path.exists():
        stamp = dt.datetime.now().strftime("%Y%m%dT%H%M%S")
        backup_path = path.with_name(f"{path.name}.bak.{stamp}")
        shutil.copy2(path, backup_path)
        backup = str(backup_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    fd, temporary = tempfile.mkstemp(prefix=f".{path.name}.", dir=path.parent)
    try:
        with os.fdopen(fd, "w", encoding="utf-8") as handle:
            json.dump(config, handle, ensure_ascii=False, indent=2)
            handle.write("\n")
        os.replace(temporary, path)
    finally:
        if os.path.exists(temporary):
            os.unlink(temporary)
    return backup


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--tool-path", required=True, type=Path)
    parser.add_argument("--config", type=Path)
    parser.add_argument("--genome")
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()

    tool_path = args.tool_path.expanduser().resolve()
    config_path = (args.config or tool_path / "NGSViz_setting.json").expanduser().resolve()
    config, config_issues = read_config(config_path)
    errors: list[str] = []
    tools = config.get("toolParas", {}) if isinstance(config.get("toolParas", {}), dict) else {}
    configured_tool = tools.get("tool_path")
    warnings: list[str] = list(config_issues)
    if configured_tool and Path(configured_tool).expanduser().resolve() != tool_path:
        warnings.append("TOOL_PATH_MISMATCH")
    tool_inspection = inspect_tool_path(tool_path)
    if not tool_inspection["valid"]:
        errors.append("TOOL_PATH_INCOMPLETE")

    java_rows = java_candidates(tools.get("java_path"))
    compatible_java = [row for row in java_rows if row["compatible"]]
    java_selected = compatible_java[0] if compatible_java else None
    if not java_selected:
        errors.append("JAVA_17_OR_NEWER_NOT_FOUND")

    jar_selected, jar_ambiguous, jars = choose_jar(tool_path, tools.get("tool_name"))
    if not jars:
        errors.append("NGSVIZ_JAR_NOT_FOUND")
    elif jar_ambiguous:
        errors.append("NGSVIZ_JAR_AMBIGUOUS")
    jar_inspection = None
    if jar_selected and java_selected:
        jar_inspection = inspect_jar(Path(java_selected["path"]), jar_selected)
        if not jar_inspection["runnable"]:
            errors.append("NGSVIZ_JAR_NOT_RUNNABLE")
        elif not jar_inspection["cli_compatible"]:
            errors.append("NGSVIZ_AI_CLI_UNSUPPORTED")

    db_rows = database_candidates(tool_path, tools.get("db_path"))
    db_selected, db_ambiguous = choose_database(db_rows, args.genome)
    if not db_selected:
        errors.append("DATABASE_AMBIGUOUS" if db_ambiguous else "DATABASE_NOT_FOUND")

    can_apply = not errors and java_selected and jar_selected and db_selected
    proposal = None
    if can_apply:
        proposal = proposed_config(
            config,
            tool_path,
            Path(java_selected["path"]),
            jar_selected,
            Path(db_selected["path"]),
        )
    backup = None
    if args.apply and proposal:
        backup = atomic_write(config_path, proposal)

    output = {
        "status": "ready" if can_apply else "blocked",
        "config_path": str(config_path),
        "config_exists": config_path.exists(),
        "applied": bool(args.apply and proposal),
        "backup": backup,
        "can_apply": bool(can_apply),
        "errors": sorted(set(errors)),
        "warnings": sorted(set(warnings)),
        "selected": {
            "tool_path": tool_inspection,
            "java": java_selected,
            "jar": jar_inspection,
            "database": db_selected,
        },
        "candidates": {"java": java_rows, "jars": jars, "databases": db_rows},
        "proposed_config": proposal,
    }
    print(json.dumps(output, ensure_ascii=False, indent=2))
    return 0 if can_apply else 1


if __name__ == "__main__":
    raise SystemExit(main())
