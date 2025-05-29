package com.NGSViz.coverageCalculator;

import com.NGSViz.configSet.InputParameterAttributes;
import java.util.stream.DoubleStream;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2025-02--14:25
 */
public class DataScaler {
    private static  int num_datapoints = InputParameterAttributes.num_datapoints+1;
    private static int flanking_region_datapoints = InputParameterAttributes.flank_points;
    private static int middle_datapoints = InputParameterAttributes.middle_points;

    // Method: switch scale
    // Input:
    //   method: String, method name to scale coverage
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] getCoverageScaled(ArrayList<Integer> physical_coverage, String method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];
        double[] head_cov_scaled;
        double[] tail_cov_scaled;
        double[] mid_cov_scaled;
        System.out.println("--- flank size is :" + flank_size);
        System.out.println("--- physical coverage size is " + physical_coverage.size());

        ArrayList<Integer> head_cov = new ArrayList<>(physical_coverage.subList(0, flank_size));
        ArrayList<Integer> tail_cov = new ArrayList<>(physical_coverage.subList(physical_coverage.size() - flank_size, physical_coverage.size()));
        ArrayList<Integer> mid_cov = new ArrayList<>(physical_coverage.subList(flank_size, physical_coverage.size() - flank_size));

        switch (method){
            case "bin":
                head_cov_scaled = bin(head_cov, flanking_region_datapoints);
                mid_cov_scaled = bin(mid_cov, middle_datapoints);
                tail_cov_scaled = bin(tail_cov, flanking_region_datapoints);
                System.out.println("--- Normalize data with bin");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
            case "spline":
                head_cov_scaled = spline(head_cov, flanking_region_datapoints);
                tail_cov_scaled = spline(tail_cov, flanking_region_datapoints);
                mid_cov_scaled = spline(mid_cov, middle_datapoints);
                System.out.println("--- Normalize data with spline");
                // copy the result into the coverage_scaled
                System.arraycopy(head_cov_scaled, 0, coverage_scaled, 0, head_cov_scaled.length);
                System.arraycopy(mid_cov_scaled, 0, coverage_scaled, head_cov_scaled.length, mid_cov_scaled.length);
                System.arraycopy(tail_cov_scaled, 0, coverage_scaled,
                        head_cov_scaled.length + mid_cov_scaled.length, tail_cov_scaled.length);
                break;
        }
        return coverage_scaled;
    }

    /*public static List<Double> getCoverageScaled2(ArrayList<Integer> physical_coverage, String method, int flank_size) {
        List<Double> coverage_scaled = new ArrayList<>(num_datapoints);
        List<Double> head_cov_scaled;
        List<Double> tail_cov_scaled;
        List<Double> mid_cov_scaled;
        System.out.println("--- flank size is :" + flank_size);
        System.out.println("--- physical coverage size is " + physical_coverage.size());

        ArrayList<Integer> head_cov = new ArrayList<>(physical_coverage.subList(0, flank_size));
        ArrayList<Integer> tail_cov = new ArrayList<>(physical_coverage.subList(physical_coverage.size() - flank_size, physical_coverage.size()));
        ArrayList<Integer> mid_cov = new ArrayList<>(physical_coverage.subList(flank_size, physical_coverage.size() - flank_size));

        switch (method){
            case "bin":
                head_cov_scaled = bin(head_cov, flanking_region_datapoints);
                tail_cov_scaled = bin(tail_cov, flanking_region_datapoints);
                mid_cov_scaled = bin(mid_cov, middle_datapoints);
                System.out.println("--- Normalize data with bin");
                coverage_scaled.addAll(head_cov_scaled);
                coverage_scaled.addAll(mid_cov_scaled);
                coverage_scaled.addAll(tail_cov_scaled);
                break;
            case "spline":
                head_cov_scaled = spline(head_cov, flanking_region_datapoints);
                tail_cov_scaled = spline(tail_cov, flanking_region_datapoints);
                mid_cov_scaled = spline(mid_cov, middle_datapoints);
                System.out.println("--- Normalize data with spline");
                coverage_scaled.addAll(head_cov_scaled);
                coverage_scaled.addAll(mid_cov_scaled);
                coverage_scaled.addAll(tail_cov_scaled);
                break;
        }
        return coverage_scaled;
    }*/

    // Method: to bin the coverage
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] bin(ArrayList<Integer> coverage_list, int num_points){
        int total_size = coverage_list.size();
        int chunk_size = total_size / num_points;
        double[] coverage_scaled = new double[num_points];
        for (int i = 0; i < num_points; i++) {
            int start = i * chunk_size;
            // could include more item in the last chunk
            int end = (i == num_points - 1) ? total_size : start + chunk_size;
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += coverage_list.get(j);
                count++;
            }
            coverage_scaled[i] = sum / count;
        }
        return coverage_scaled;
    }

    public static List<Double> bin2(ArrayList<Integer> coverage_list, int num_points){
        int total_size = coverage_list.size();
        int chunk_size = total_size / num_points;
        List<Double> coverage_scaled = new ArrayList<>();
        for (int i = 0; i < num_points; i++) {
            int start = i * chunk_size;
            // could include more item in the last chunk
            int end = (i == num_points - 1) ? total_size : start + chunk_size;
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += coverage_list.get(j);
                count++;
            }
            coverage_scaled.add(sum / count);
        }
        return coverage_scaled;
    }
    // Method: spline
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] spline(ArrayList<Integer> coverage_list , int num_points) {
        double[] coverage_scaled = new double[num_points];
        int totalSize = coverage_list.size();
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if (num_points == 1) {
            // For num_points == 1, take the first value or average if needed
            coverage_scaled[0] = coverage_list.isEmpty() ? 0.0 : coverage_list.get(0).doubleValue();
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list.get(lower) +
                    weight * coverage_list.get(upper);
            // Floor negative values which are caused by spline
            if (interpolatedValue < 0) {
                interpolatedValue = 0;
            }
            coverage_scaled[i] = interpolatedValue;
        }
        return coverage_scaled;
    }

    public static List<Double> spline2(ArrayList<Integer> coverage_list , int num_points) {
        List<Double> coverage_scaled = new ArrayList<>();
        int totalSize = coverage_list.size();
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if(num_points==1){
            for (Integer value : coverage_list) {
                coverage_scaled.add(value.doubleValue());
            }
            System.out.println();
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list.get(lower) +
                    weight * coverage_list.get(upper);
            // Floor negative values which are caused by spline.
            if(interpolatedValue < 0){
                interpolatedValue = 0;
            }
            coverage_scaled.add(interpolatedValue);
        }
        return coverage_scaled;
    }
}
