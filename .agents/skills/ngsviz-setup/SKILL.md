---
name: ngsviz-setup
description: Inspect, create, and repair NGSViz_setting.json and the local ngsViz Java runtime, runnable JAR, tool_path, and reference database path. Use before any ngsViz Java compute validation, when configuration is missing or invalid, or when Java, JAR, tool, or database paths may have moved.
---

# Configure the ngsViz Environment

Complete this environment check before any Java `validate-compute` operation. Visualization-only workflows do not require it.

## Inspection Workflow

1. Identify the directory containing `README.md`, `lib/`, and the ngsViz JAR as `tool_path`. Do not guess it from the current working directory.
2. Run a read-only audit:

   ```bash
   python3 scripts/check_ngsviz_environment.py --tool-path /absolute/path/to/NGSViz
   ```

   If the requested genome is known, add `--genome <assembly>` to filter database candidates for that version.
3. Inspect `errors`, `warnings`, `selected`, and `proposed_config` in the audit JSON.
4. Only when `can_apply=true`, rerun the same command with `--apply`. The script writes configuration atomically and creates a timestamped backup before modifying an existing configuration.
5. Rerun the read-only audit and require `status=ready` before resolving the genome or database and collecting compute parameters.

## Repair Rules

- If `NGSViz_setting.json` is missing, create it only after discovering exactly one qualified Java executable, JAR, and database.
- If `java_path` is invalid or the Java major version is below 17, search for another installed Java 17+ and repair the path when found.
- If no Java 17+ is installed, use `conda env create -f <tool_path>/environment-java.yml` as documented in the README to install the cross-platform Java-only environment. Do not use `environment.yml`, which pins `linux-64` packages, on macOS or non-Linux-x86_64 platforms. This command downloads and installs software, so follow platform approval requirements before running it.
- After installation, run `conda run -n ngsViz-java java -version` to confirm Java 17+. Then obtain the environment root from the `java.home` output of `conda run -n ngsViz-java java -XshowSettings:properties -version`. Select the existing `bin/java` path, or `bin/java.exe` on Windows, verify it with the audit script, and write the absolute `java_path`.
- If the configured `tool_name` does not exist and the tool root contains exactly one `NGSViz-*.jar`, repair the setting with that filename. If no JAR exists, report the error and stop. If several exist and no unique choice is possible, list them and ask the user instead of selecting by filename version.
- If `tool_path` is invalid, repair it using the shared location of the JAR, README, and `lib/`.
- If `db_path` is invalid, first check for incorrect relative-path resolution and then inspect `<tool_path>/database/*.db`. Select only a database that contains `defaultTbl` and passes SQLite integrity checks.
- If no usable database exists, do not create a configuration pointing to a nonexistent file. Hand off to `$ngsviz-install-db`; if its pre-built catalog lacks the target species or version, hand off to `$ngsviz-build-db`.
- Preserve unknown configuration fields. Never overwrite the JAR, database, or other source files.

## Ready Conditions

- Java is executable and its major version is at least 17.
- `tool_path/tool_name` is an existing runnable JAR whose `describe --format json` output declares support for `validate-compute` and `run-compute`.
- `db_path` is a readable SQLite file that contains `defaultTbl` and valid coordinate-table metadata.
- All four configured paths resolve to real paths on the current machine.

Do not create a compute request or run `validate-compute` until the environment is ready.
