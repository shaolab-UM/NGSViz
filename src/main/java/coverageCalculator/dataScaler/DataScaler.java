/*
package coverageCalculator.dataScaler;

import configSet.InputParameterAttributes;
import configSet.InputParameterAttributes;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * @author Benchen Ye
 * @create 2024-09--19:56
 *//*


// Class: scale the physical coverage into the number of data point to plot
// Constructor Input:
public class DataScaler {
    private ArrayList<Integer> physical_coverage;
    private int num_datapoints = InputParameterAttributes.num_datapoints;
    public static void testmain(){

    }

    public DataScaler(ArrayList<Integer> physical_coverage)
    {
        this.physical_coverage = physical_coverage;
    }


    // Method: switch scale
    // Input:
    //   method: String, method name to scale coverage
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public List<Double> getCoverageScaled(String method) {
        List<Double> coverage_scaled = new ArrayList<>(this.num_datapoints);
        switch (method){
            case "bin":
                coverage_scaled = bin();
                System.out.println("bin");
                break;
            case "spline":
                coverage_scaled = spline();
                System.out.println("spline");
                break;
        }
        return coverage_scaled;
    }

    // Method: to bin the coverage
    // Input:
    //   physical_coverage: ArrayList<Integer> of physical coverage
    //   num_datapoints: int, number of data points to plot
    // Output:
    //   coverage_scaled: List<Double> of binned values
    public List<Double> bin(){
        int total_size = physical_coverage.size();
        int chunk_size = total_size / num_datapoints;
        List<Double> coverage_scaled = new ArrayList<>();
        for (int i = 0; i < num_datapoints; i++) {
            int start = i * chunk_size;
            // could include more item in the last chunk
            int end = (i == num_datapoints - 1) ? total_size : start + chunk_size;
            double sum = 0;
            int count = 0;
            for (int j = start; j < end; j++) {
                sum += physical_coverage.get(j);
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
    public List<Double> spline() {
        List<Double> coverage_scaled = new ArrayList<>();
        int totalSize = physical_coverage.size();
        double step = (double) (totalSize - 1) / (num_datapoints - 1);

        for (int i = 0; i < num_datapoints; i++) {
            double index = i * step;
            int lower = (int) Math.floor(index);
            int upper = (int) Math.ceil(index);
            if (upper >= totalSize) upper = totalSize - 1;
            double weight = index - lower;
            // Linear interpolation method is used to calculate the value after interpolation
            double interpolatedValue = (1 - weight) * physical_coverage.get(lower) +
                    weight * physical_coverage.get(upper);
            // Floor negative values which are caused by spline.
            if(interpolatedValue < 0){
                interpolatedValue = 0;
            }
            coverage_scaled.add(interpolatedValue);
        }
        return coverage_scaled;
    }

}

*/
