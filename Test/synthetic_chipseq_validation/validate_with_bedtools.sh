#!/usr/bin/env bash
set -euo pipefail

BAM=synthetic_chipseq.bam
EXPECTED=expected_coverage.tsv

# 1. 检查BAM
samtools quickcheck -v "$BAM"
samtools view -H "$BAM"
samtools view "$BAM"

# 2. bedtools逐碱基coverage
bedtools genomecov -ibam "$BAM" -d > bedtools_coverage.tsv

# 3. 与人工理论coverage逐行比较
diff -u "$EXPECTED" bedtools_coverage.tsv

echo "PASS: bedtools coverage exactly matches analytically expected coverage."
