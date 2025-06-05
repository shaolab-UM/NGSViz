package com.NGSViz.coverageCalculator;

import com.NGSViz.sqldbOperate.Transcript;

import java.util.Arrays;

public class EachCoverageResult {
    private Transcript gene_coord;
    private double[] scaled_coverage;
    private String record_name;
    private int index;

    public EachCoverageResult(Transcript gene_coord, double[] scaled_coverage) {
        this.gene_coord = gene_coord;
        this.scaled_coverage = scaled_coverage;
        this.record_name = gene_coord.getRecordName();
        this.index = gene_coord.getIndex();
    }

    public void bamSizeNormalization(long library_size)
    {
        //double[] result_list = new double[scaled_coverage.length];
        // normalize to RPM
        for (int i = 0; i < scaled_coverage.length; i++) {
            double num = scaled_coverage[i];
            // num / library_size * 1e6
            double result = num / library_size * 1_000_000.0;
            scaled_coverage[i] = result;
        }
    }

    public Transcript getTranscript() {
        return gene_coord;
    }

    public double[] getScaledCoverage() {
        return scaled_coverage;
    }
    public int getCoverageLength(){
        return scaled_coverage.length;
    }
    public int getIndex(){
        return index;
    }
    @Override
    public String toString() {
        return record_name + " : " + Arrays.toString(scaled_coverage);
    }
}