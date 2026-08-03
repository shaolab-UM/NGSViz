#!/usr/bin/env python3
"""Check whether a GTF is compatible with the current ngsViz DB builder."""

from __future__ import annotations

import argparse
import gzip
import json
import re
from pathlib import Path
from typing import TextIO


def open_gtf(path: Path) -> TextIO:
    if path.suffix == ".gz":
        return gzip.open(path, "rt", encoding="utf-8")
    return path.open("r", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--gtf", required=True, type=Path)
    parser.add_argument("--genome", required=True)
    parser.add_argument("--max-records", type=int, default=200000)
    args = parser.parse_args()

    errors: list[str] = []
    warnings: list[str] = []
    path = args.gtf.expanduser().resolve()
    name = path.name[:-3] if path.name.endswith(".gz") else path.name
    prefix = name.split(".", 1)[0]
    if prefix != args.genome:
        errors.append("GTF_FILENAME_GENOME_MISMATCH")
    if not path.is_file():
        errors.append("GTF_NOT_FOUND")
        print(json.dumps({"valid": False, "path": str(path), "errors": errors}, indent=2))
        return 1

    counts = {"records": 0, "transcript": 0, "exon": 0, "refseq_transcript": 0}
    seqnames: set[str] = set()
    has_gene_name = False
    has_transcript_id = False
    malformed = 0
    try:
        with open_gtf(path) as handle:
            for line in handle:
                if line.startswith("#") or not line.strip():
                    continue
                fields = line.rstrip("\n").split("\t")
                if len(fields) != 9:
                    malformed += 1
                    continue
                counts["records"] += 1
                feature = fields[2]
                attributes = fields[8]
                seqnames.add(fields[0])
                if feature in counts:
                    counts[feature] += 1
                has_gene_name = has_gene_name or bool(re.search(r'\bgene_name\s+"', attributes))
                transcript = re.search(r'\btranscript_id\s+"([^"]+)"', attributes)
                if transcript:
                    has_transcript_id = True
                    if re.match(r"^(NM|NR)_", transcript.group(1)):
                        counts["refseq_transcript"] += 1
                if counts["records"] >= args.max_records:
                    break
    except (OSError, UnicodeError, gzip.BadGzipFile) as error:
        errors.append(f"GTF_READ_ERROR:{error}")

    if counts["records"] == 0:
        errors.append("GTF_HAS_NO_RECORDS")
    if counts["transcript"] == 0:
        errors.append("TRANSCRIPT_RECORDS_MISSING")
    if counts["exon"] == 0:
        errors.append("EXON_RECORDS_MISSING")
    if not has_gene_name:
        errors.append("GENE_NAME_ATTRIBUTE_MISSING")
    if not has_transcript_id:
        errors.append("TRANSCRIPT_ID_ATTRIBUTE_MISSING")
    if counts["refseq_transcript"] == 0:
        errors.append("REFSEQ_TRANSCRIPT_IDS_MISSING")
    if malformed:
        warnings.append(f"MALFORMED_RECORDS:{malformed}")
    ncbi_accession = re.compile(r"^[A-Z]{2}_\d+(?:\.\d+)?$")
    if any(ncbi_accession.match(name) for name in seqnames):
        errors.append("NCBI_ACCESSION_CHROMOSOMES_UNSUPPORTED")
    elif seqnames and any(not name.startswith("chr") for name in seqnames):
        warnings.append("CHROMOSOME_PREFIX_WILL_BE_ADDED")

    output = {
        "valid": not errors,
        "path": str(path),
        "genome": args.genome,
        "sampled_counts": counts,
        "sampled_seqnames": sorted(seqnames),
        "errors": errors,
        "warnings": warnings,
    }
    print(json.dumps(output, ensure_ascii=False, indent=2))
    return 0 if not errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
