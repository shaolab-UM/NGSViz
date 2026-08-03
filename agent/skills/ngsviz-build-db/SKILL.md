---
name: ngsviz-build-db
description: Validate a user-provided UCSC GTF, build an ngsViz SQLite reference database with lib/buildRefDB.R, verify its schema and records, and import it into the configured ngsViz database. Use when the requested species or genome assembly is not available in the README pre-built database catalog.
---

# Build an ngsViz Reference Database from GTF

Use this workflow only when the target species or genome is absent from the README pre-built catalog. Require a UCSC GTF that exactly matches the assembly used to align the BAM.

## Collect Parameters

Explicitly confirm:

- The absolute GTF path.
- The scientific species name, such as `Homo_sapiens`.
- The genome assembly, such as `hg38`.
- The `DB_name`, such as `RefSeq`.
- `annotatedNCRNA`: `TRUE` or `FALSE`.

Do not guess the species or assembly. Require the filename format `<genome>.<annotation>.gtf`; the segment before the first period must equal the confirmed genome.

## Validate the Input

Run:

```bash
python3 /absolute/path/to/installed/ngsviz-build-db/scripts/check_gtf_for_ngsviz.py \
  --gtf /absolute/path/to/<genome>.<annotation>.gtf \
  --genome <assembly>
```

Confirm that the GTF has at least nine columns, contains transcript or exon records, and includes `gene_name` and `transcript_id`. The current `buildRefDB.R` retains `NM` and `NR` transcripts, so require a compatible UCSC RefSeq GTF. If the file contains only Ensembl transcript IDs, stop and request a compatible GTF instead of building an empty database. Stop if any chromosome name uses an NCBI accession style such as `NC_`, `NW_`, `NT_`, or `NZ_`; require consistent conversion to UCSC style first, even if the file also contains other naming styles.

## Build

1. Create an isolated workspace at `analysis/reference_db_build/<date>_<species>_<genome>/`. Do not move or modify the original GTF.
2. Preflight R and the `RSQLite`, `dplyr`, `rtracklayer`, and `biomaRt` packages. Missing packages trigger downloads, so follow platform approval requirements before installation.
3. Run the project script from the build workspace so that it writes output to `DB/<species>/` inside that workspace:

   ```bash
   Rscript /absolute/path/to/NGSViz/lib/buildRefDB.R \
     /absolute/path/to/<genome>.<annotation>.gtf \
     <DB_name> <species> <TRUE-or-FALSE>
   ```

4. Expect `DB/<species>/NGSViz_<genome>_<DB_name>.db`. Do not overwrite an existing build artifact.
5. Use `manage_ngsviz_db.py verify` from `$ngsviz-install-db` to verify the target genome, coordinate table existence, and nonempty records.
6. Import the database through the `$ngsviz-install-db` workflow. If no primary database exists, set the built database directly as `db_path`.
7. Verify the primary database again after import and rerun `$ngsviz-setup`.

## Failure Boundaries

- Stop when the GTF does not match the BAM assembly, required attributes are missing, or the build result is empty.
- Do not automatically download or rewrite a GTF from a non-UCSC source.
- Do not infer the genome assembly from the presence or absence of a `chr` prefix.
- Preserve the original GTF, built database, pre-import backup, and logs separately.
