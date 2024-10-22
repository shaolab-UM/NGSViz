package coverageCalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-10--11:22
 * @function trim has error
 */
public class SEMCalculator {
    // test
    public static void main(String[] args) {
        double[] data = {2,3,1,4,5,6,7,9,2};
        // calculate the SEM without trim
        //double semValue = calculateSEM(data, 0);
        //System.out.println("SEM without trimming: " + semValue);
        // calculate the SEM of trim 5% data
        double semValueTrimmed = calculateSEM(data, 0.95);
        System.out.println("SEM with 5% trimming: " + semValueTrimmed);
    }

    // calculate quantile value for trim_ratio
    public static double quantile(double[] vector_list, double trim_ratio) {
        double percentile_value;
        Arrays.sort(vector_list);
        double index = trim_ratio * (vector_list.length - 1);
        int lower_index = (int) Math.floor(index);
        int upper_undex = (int) Math.ceil(index);
        if (lower_index == upper_undex) {
            percentile_value = vector_list[lower_index];
        } else {
            double lowerValue = vector_list[lower_index];
            double upperValue = vector_list[upper_undex];
            percentile_value = (lowerValue + upperValue) / 2;
        }
        System.out.println("Percentile value: " + percentile_value);
        return percentile_value;
    }
    // trim the low and high value of the data according to the trim_ratio
    public static List<Double> trim(double[] vector_list, double trim_ratio) {
        double low = quantile(vector_list, trim_ratio);
        double high = quantile(vector_list, 1-trim_ratio);
        List<Double> filter_res = filterData(vector_list, low, high);
        return filter_res;
    }

    // filter the value of [] data lower than low value and greater than high value
    public static List<Double> filterData(double[] array, double low, double high) {
        List<Double> result = new ArrayList<>();
        for (int i = 0; i < array.length; i++) {
            double value = array[i];
            if (value > low && value < high) {
                result.add(value);
            }
        }
        return result;
    }

    // list<double> to double[]
    public static double[] list2matrix(List<Double> list_data) {
        int item_length = list_data.size();
        double[] res_matrix = new double[item_length];
        for (int i = 0; i < item_length; i++) {
            res_matrix[i] = list_data.get(i);
        }
        return res_matrix;
    }

    // calculate the sem
    public static double calculateSEM(double[] vector_list, double trim_ratio) {
        if (trim_ratio > 0){
            List<Double> trim_res = trim(vector_list, trim_ratio);
            vector_list = list2matrix(trim_res);
        }
        // count the mean
        double sum = 0.0;
        for (double num : vector_list) {
            sum += num;
        }
        double mean = sum / vector_list.length;
        // count the variance
        double varianceSum = 0.0;
        for (double num : vector_list) {
            varianceSum += Math.pow(num - mean, 2);
        }
        double variance = varianceSum / (vector_list.length-1);
        // count the sd
        double standardDeviation = Math.sqrt(variance);
        // standardDeviation
        double sem = standardDeviation / Math.sqrt(vector_list.length);
        if (Double.isNaN(sem)) {
            sem = 0.0;
        }
        System.out.println("Sem: " + sem);
        return sem;
    }
}
