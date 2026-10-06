#!/usr/bin/env bash
set -euo pipefail
bam=$1
out=$2
bed=$3
cores=$4
bin=/opt/conda/envs/deeptools-3.5.6/bin
/usr/bin/time -o "$out/bamCoverage_time.tsv" -f '%e\t%M' "$bin/bamCoverage" -b "$bam" -o "$out/coverage.bw" --binSize 50 --normalizeUsing CPM --numberOfProcessors "$cores"
/usr/bin/time -o "$out/computeMatrix_time.tsv" -f '%e\t%M' "$bin/computeMatrix" scale-regions -S "$out/coverage.bw" -R "$bed" --regionBodyLength 5000 --beforeRegionStartLength 2000 --afterRegionStartLength 2000 --skipZeros -o "$out/matrix.gz" -p "$cores"
