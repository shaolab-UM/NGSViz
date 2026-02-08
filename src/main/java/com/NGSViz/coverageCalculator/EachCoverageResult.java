package com.NGSViz.coverageCalculator;

import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.sqldbOperate.Transcript;

import java.util.Arrays;

public class EachCoverageResult {
    private Transcript gene_coord;
    private double[] scaled_coverage;
    private String record_name;
    //private static double scale_ratio = InputParameterAttributes.scale_ratio;
    private int index;

    public EachCoverageResult(Transcript gene_coord, double[] scaled_coverage) {
        this.gene_coord = gene_coord;
        this.scaled_coverage = scaled_coverage;
        this.record_name = gene_coord.getRecordName();
        this.index = gene_coord.getIndex();
    }

    public void bamSizeNormalization(long library_size, Double scale_ratio)
    {
        //double[] result_list = new double[scaled_coverage.length];
        // normalize to CPM
        for (int i = 0; i < scaled_coverage.length; i++) {
            double num = scaled_coverage[i];
            double result;
            if (scale_ratio != null) {
                // spike-in normalization:
                // scale_ratio = min(raw spike-in reads) / current raw spike-in reads
                // library size normalization is implicit here
                result = num * scale_ratio;
            }else{
                // no spike-in normalization → CPM
                result = (num / library_size) * 1_000_000.0;
            }
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