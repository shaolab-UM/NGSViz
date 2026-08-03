---
name: ngsviz-install-db
description: Discover installed ngsViz species and genome assemblies, parse the README pre-built database catalog, download a requested pre-built SQLite database, validate it, and import it with the ngsViz Java -DB method. Use when a requested genome is absent from the configured database or when ngsViz has no usable reference database.
---

# Install an ngsViz Pre-Built Reference Database

Install only a species and genome assembly that the user explicitly specified or confirmed. Do not infer an assembly from BAM filenames, chromosome prefixes, or common experimental practice.

## Inspect Available Databases

1. Inspect the current database:

   ```bash
   python3 /absolute/path/to/installed/ngsviz-install-db/scripts/manage_ngsviz_db.py \
     inspect --db /absolute/path/to/current.db
   ```

2. Parse the pre-built catalog from the project's current `README.md`. Do not copy or hard-code old links:

   ```bash
   python3 /absolute/path/to/installed/ngsviz-install-db/scripts/manage_ngsviz_db.py \
     catalog --readme /absolute/path/to/NGSViz/README.md
   ```

   Add `--species Homo_sapiens` or `--genome hg38` for exact filtering when needed.

## Select a Version

- If the user specified a genome, return immediately to the main workflow when it is already installed; otherwise, find an exact match in the README catalog.
- If the user specified only a species, inspect the current database first. If exactly one genome is available, ask whether to use it; if several are available, list them and ask the user to choose.
- If the user rejects the only installed version, list other versions for the same species from the README.
- If the current database lacks the species, query the README. If the species is present, list its versions, ask the user to choose, and then install the confirmed version.
- If the README lacks the species or genome, stop the download workflow, ask the user for a GTF from UCSC that matches the assembly, and hand off to `$ngsviz-build-db`.

When matching species, show both the README common name and scientific name. Internally, use `defaultTbl.Species` and the README scientific name.

## Download and Validate

1. Download the exact URL parsed from the catalog to an isolated temporary file with `curl -L --fail`. Downloads are network operations, so follow platform approval requirements.
2. A README pre-built file may be a gzip archive. Use this command to inspect the file's magic bytes, decompress it when necessary, validate it, and atomically write the resulting SQLite file to the target path:

   ```bash
   python3 /absolute/path/to/installed/ngsviz-install-db/scripts/manage_ngsviz_db.py prepare \
     --source /absolute/path/to/downloaded-file \
     --output /absolute/path/to/NGSViz/database/prebuilt/NGSViz_<genome>_RefSeq.db \
     --genome <assembly>
   ```

   Preserve the downloaded archive. Do not pass `.db.gz` directly to SQLite or overwrite an existing target file. On conflict, compare contents first or choose a new timestamped path.
3. `prepare` already validates SQLite and the target genome. Optionally verify them independently again before import:

   ```bash
   python3 /absolute/path/to/installed/ngsviz-install-db/scripts/manage_ngsviz_db.py verify \
     --db /absolute/path/to/downloaded.db --genome <assembly>
   ```

   Require SQLite `quick_check=ok`, a `defaultTbl`, the target genome, and a present, nonempty table named by its `CoordinateTblName`.

## Install

- If no primary database exists, do not merge. Use the validated pre-built database as the primary database and invoke `$ngsviz-setup --apply` to write `db_path`.
- If a writable primary database exists, create a timestamped backup first, then use the absolute Java, JAR, and configuration paths to run the method documented in the README:

  ```bash
  /absolute/path/to/java \
    -jar /absolute/path/to/NGSViz/NGSViz-1.3.jar \
    -CP /absolute/path/to/NGSViz/NGSViz_setting.json \
    -DB /absolute/path/to/NGSViz_<genome>_RefSeq.db
  ```

- If the primary database is not writable, do not merge in place. Copy it to a separate user-writable path, update the configuration, and then merge.
- The legacy Java merge may print SQL exceptions but still return a successful exit code. Never rely only on the exit code; after merging, rerun `verify --db <main_db> --genome <assembly>`.
- If verification fails, preserve the backup and downloaded file, report the error, and do not proceed to `validate-compute`.

After successful installation, rerun `$ngsviz-setup` and return the installed species and genome list to `$ngsviz-agent`.
