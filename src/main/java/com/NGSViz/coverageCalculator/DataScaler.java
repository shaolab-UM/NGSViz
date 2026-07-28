package com.NGSViz.coverageCalculator;

import com.NGSViz.configSet.InputParameterAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.NGSViz.configSet.InputParameterAttributes.CenterMode;
import static org.apache.commons.math3.stat.descriptive.AggregateSummaryStatistics.aggregate;

/**
 * @author Benchen Ye
 * @create 2025-02--14:25
 */
public class DataScaler {
    private static  int num_datapoints = InputParameterAttributes.num_datapoints+1;
    private static String interval_type = InputParameterAttributes.interval_type;
    private static int flank_points = InputParameterAttributes.flank_points+1;
    private static int middle_points = InputParameterAttributes.middle_points;

    public static double[] getCoverageScaled(int[] physical_coverage, String bin_method, int flank_size) {
        double[] coverage_scaled = new double[num_datapoints];

        // Here, the points of bin need to match the division of datapoint.
        if (interval_type.equals("point_interval")) {
            coverage_scaled = bin(physical_coverage, num_datapoints, bin_method);
        } else if (interval_type.equals("broad_interval")) {

            double[] head_cov_scaled;
            double[] tail_cov_scaled;
            double[] mid_cov_scaled;
            // Convert to primitive arrays for better performance
            int[] head_cov = new int[flank_size];
            int[] tail_cov = new int[flank_size];
            int[] mid_cov = new int[physical_coverage.length - 2 * flank_size];

            // Copy head region
            System.arraycopy(physical_coverage, 0, head_cov, 0, flank_size);
            // Copy tail region
            System.arraycopy(physical_coverage, physical_coverage.length - flank_size, tail_cov, 0, flank_size);
            // Copy middle region
            System.arraycopy(physical_coverage, flank_size, mid_cov, 0, mid_cov.length);

            head_cov_scaled = bin(head_cov, flank_points, bin_method);
            mid_cov_scaled = bin(mid_cov, middle_points, bin_method);
            tail_cov_scaled = bin(tail_cov, flank_points, bin_method);

            // Scale each region
            // Combine scaled regions
            for (int i = 0; i < (flank_points-1); i++) { //20
                coverage_scaled[i] = head_cov_scaled[i];
                coverage_scaled[num_datapoints - flank_points + 1 + i] = tail_cov_scaled[i];
            }
            coverage_scaled[flank_points-1] = (head_cov_scaled[flank_points-1] + mid_cov_scaled[0])/2;
            coverage_scaled[num_datapoints - flank_points] = (tail_cov_scaled[0] + mid_cov_scaled[middle_points-1])/2;
            for (int j = 0; j < (middle_points-2); j++) { //61
                coverage_scaled[flank_points + j] = mid_cov_scaled[j+1];
            }

        }
        return coverage_scaled;
    }


    private static double aggregate(List<Integer> values, String method) {
        if (values.isEmpty()) return 0.0;

        switch (method) {
            case "mean":
                double sum = 0;
                for (int v : values) sum += v;
                return sum / values.size();
            case "median":
                int n = values.size();
                Collections.sort(values);

                if (n % 2 == 1) {
                    return values.get(n / 2);
                } else {
                    return (values.get(n / 2 - 1) + values.get(n / 2)) / 2.0;
                }
            case "max":
                int m = Integer.MIN_VALUE;
                for (int v : values) m = Math.max(m, v);
                return m;
            default:
                throw new IllegalArgumentException("Unknown bin method: " + method);
        }
    }


    // Method: to bin the coverage
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public static double[] bin(int[] coverage_list, int num_points, String bin_method){
        int total_size = coverage_list.length;
        int chunk_size = total_size / num_points;
        int[] boundaries = new int[num_points + 1];
        for (int i = 0; i <= num_points; i++) {
            boundaries[i] = i * chunk_size;
        }
        boundaries[num_points] = total_size; // Ensure last boundary is correct
        
        double[] coverage_scaled = new double[num_points];

        for (int i = 0; i < num_points; i++) {
            int start = boundaries[i];
            int end = boundaries[i + 1];
            int count = end - start;

            if (count <= 0) {
                coverage_scaled[i] = 0;
                continue;
            }

            if (bin_method.equalsIgnoreCase("mean")) {

                double sum = 0;
                for (int j = start; j < end; j++) {
                    sum += coverage_list[j];
                }
                coverage_scaled[i] = sum / count;

            } else if (bin_method.equalsIgnoreCase("max")) {

                int max = coverage_list[start];
                for (int j = start + 1; j < end; j++) {
                    if (coverage_list[j] > max) {
                        max = coverage_list[j];
                    }
                }
                coverage_scaled[i] = max;

            } else if (bin_method.equalsIgnoreCase("median")) {

                int[] temp = new int[count];
                for (int j = 0; j < count; j++) {
                    temp[j] = coverage_list[start + j];
                }

                Arrays.sort(temp);

                if (count % 2 == 1) {
                    coverage_scaled[i] = temp[count / 2];
                } else {
                    coverage_scaled[i] =
                            (temp[count / 2 - 1] + temp[count / 2]) / 2.0;
                }

            } else {
                throw new IllegalArgumentException("Unknown bin_method: " + bin_method);
            }
        }

        return coverage_scaled;
    }


    public static double[] spline(int[] coverage_list , int num_points) {
        double[] coverage_scaled = new double[num_points];
        int totalSize = coverage_list.length;
        double step = (double) (totalSize - 1) / (num_points - 1);
        System.out.println("Num points is " + num_points);
        if (num_points == 1) {
            // For num_points == 1, take the first value or average if needed
            coverage_scaled[0] = coverage_list.length == 0 ? 0.0 : (double) coverage_list[0];
            return coverage_scaled;
        }
        for (int i = 0; i < num_points; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * coverage_list[lower] +
                    weight * coverage_list[upper];
            // Floor negative values which are caused by spline
            if (interpolatedValue < 0) {
                interpolatedValue = 0;
            }
            coverage_scaled[i] = interpolatedValue;
        }
        return coverage_scaled;
    }

}
