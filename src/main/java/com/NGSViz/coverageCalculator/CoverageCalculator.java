package com.NGSViz.coverageCalculator;

import htsjdk.samtools.SAMRecord;
import java.util.List;
import java.util.Arrays;
import java.util.stream.IntStream;

public class CoverageCalculator {

    /*private static final int TARGET_BINS = 101;

    *//**
     * 计算单个基因区域的原始 coverage。
     *
     * @param gene 基因信息
     * @param reads 该基因区域内的 reads
     * @return 原始 coverage 数组
     *//*
    private int[] calculateRawCoverage(Gene gene, List<SAMRecord> reads) {
        int geneLength = gene.getEnd() - gene.getStart() + 1;
        int[] coverage = new int[geneLength];

        for (SAMRecord read : reads) {
            // 确保 read 的坐标与基因区域有重叠
            int readStart = read.getStart();
            int readEnd = read.getEnd();

            int overlapStart = Math.max(gene.getStart(), readStart);
            int overlapEnd = Math.min(gene.getEnd(), readEnd);

            // 如果有重叠，则增加 coverage
            if (overlapStart <= overlapEnd) {
                for (int i = overlapStart; i <= overlapEnd; i++) {
                    // 将基因的绝对坐标映射到相对于基因起始位置的0-based索引
                    int geneRelativeIndex = i - gene.getStart();
                    if (geneRelativeIndex >= 0 && geneRelativeIndex < geneLength) {
                        coverage[geneRelativeIndex]++;
                    }
                }
            }
        }
        return coverage;
    }*/

    /**
     * 将原始 coverage 缩放到指定数量的 bin。
     *
     * @param rawCoverage 原始 coverage 数组
     * @return 缩放后的 coverage 数组 (101个数据点)
     */
    /*public double[] scaleCoverage(Gene gene, List<SAMRecord> reads) {
        int[] rawCoverage = calculateRawCoverage(gene, reads);
        int geneLength = gene.getEnd() - gene.getStart() + 1;
        double[] scaledCoverage = new double[TARGET_BINS];

        if (geneLength == 0) {
            return scaledCoverage; // 避免除以零
        }

        // 计算每个 bin 的长度
        double binSize = (double) geneLength / TARGET_BINS;

        for (int i = 0; i < TARGET_BINS; i++) {
            int binStart = (int) (i * binSize);
            int binEnd = (int) ((i + 1) * binSize) - 1;

            // 确保 bin 范围不超出原始 coverage 数组
            binStart = Math.max(0, binStart);
            binEnd = Math.min(rawCoverage.length - 1, binEnd);

            double binSum = 0;
            if (binStart <= binEnd) {
                for (int j = binStart; j <= binEnd; j++) {
                    binSum += rawCoverage[j];
                }
            }
            scaledCoverage[i] = binSum;
        }
        return scaledCoverage;
    }*/
}