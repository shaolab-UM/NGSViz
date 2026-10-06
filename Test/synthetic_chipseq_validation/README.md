# Synthetic ChIP-seq coverage validation dataset

Purpose: numerical validation of DNA-level coverage calculation.

## Design

Reference:
- `chrSynthetic`, length = 500 bp

Alignments:
- 18 synthetic reads
- read length = 20 bp
- CIGAR = `20M` for every read
- both forward (FLAG 0) and reverse (FLAG 16) alignments are included
- no spliced reads and no CIGAR `N` operations are included

This dataset is intentionally restricted to continuous DNA-level alignments suitable for ChIP-seq/CUT&Tag-style coverage validation.

## Files

- `synthetic_chipseq.sam`: human-readable synthetic alignments
- `synthetic_chipseq.bam`: coordinate-sorted BAM generated directly from the same alignments
- `expected_coverage.tsv`: analytically expected per-base depth for all 500 bp
- `expected_coverage_nonzero.tsv`: non-zero expected depth only, with header
- `synthetic_regions.bed`: full region plus four test ROIs
- `validate_with_bedtools.sh`: independent validation using samtools + bedtools

## Expected patterns

Cluster A:
- staggered 20-bp reads starting every 5 bp
- produces a graded overlap pattern

Cluster B:
- four reads start at position 201 and one at 206
- positions 206-220 have expected depth 5

Cluster C:
- reads start at 301, 311, and 321
- creates simple 1/2-depth overlap transitions

Cluster D:
- two partially separated reads
- provides an additional low-complexity case

## Independent validation

Run in this directory:

```bash
bash validate_with_bedtools.sh
```

A successful test ends with:

```text
PASS: bedtools coverage exactly matches analytically expected coverage.
```

## Suggested manuscript wording

A synthetic alignment dataset with analytically defined per-base coverage was constructed to directly validate numerical coverage calculation. The dataset contained continuous, unspliced 20-bp alignments with predefined genomic coordinates and controlled overlap patterns. Coverage values generated from the synthetic BAM were compared with the analytically expected per-base depth and independently cross-validated using BEDTools `genomecov`.
