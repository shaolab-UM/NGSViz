package com.NGSViz.coverageCalculator;

import com.NGSViz.sqldbOperate.Transcript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Store the scaled coverage array for each gene
public class GeneCoverageResult {
    private double[][] scaled_coverage_mat;
    private List<Transcript> transcripts_list = new ArrayList<>();
    private List<String> record_name_list = new ArrayList<>();
    private ArrayList<Integer> index_list = new ArrayList<>();

    public GeneCoverageResult(double[][] scaled_coverage_mat, List<String> record_name_list) {
        this.scaled_coverage_mat = scaled_coverage_mat;
        this.record_name_list = record_name_list;
    }

    public double[][] getScaledCoverage() {
        return scaled_coverage_mat;
    }
    public List<String> getRecordName(){ return record_name_list; }
    public List<Transcript> getTranscripts(){ return transcripts_list; }
    public ArrayList<Integer> getIndexList(){ return index_list; }
    public void print() {
        for (int i = 0; i < Math.min(5, scaled_coverage_mat.length); i++) {
            String geneName = record_name_list.get(i);
            System.out.printf("Row %d (Gene: %s): %s%n", i, geneName, Arrays.toString(scaled_coverage_mat[i]));
        }
    }
}


