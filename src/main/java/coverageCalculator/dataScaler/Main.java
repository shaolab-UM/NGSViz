package coverageCalculator.dataScaler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-09--19:57
 */
public class Main {
    public static void main(String[] args) {
        // DataScaler scaler = new DataScaler();

        /*
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            physical_coverage.add(random.nextInt(100)); // 生成0到99之间的随机整数
        }
        int pts = 4;
        */
        // 使用插值法
        ArrayList<Integer> physical_coverage = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            double value = Math.sin(i * 0.01) + Math.random() * 0.1; // 示例数据
            physical_coverage.add((int) (value * 100)); // 将 double 转换为 Integer 并放大以保留精度
            //System.out.println(physical_coverage.get(i));
        }

        System.out.println(physical_coverage);
        DataScaler data_scaler = new DataScaler(physical_coverage);
        List<Double> bin_averages = data_scaler.getCoverageScaled("bin");
        System.out.println(bin_averages);
        List<Double> spline_averages = data_scaler.getCoverageScaled("spline");
        //List<Double> averages = splitAndCalculateAverages(physical_coverage, num_datapoints);
        System.out.println(spline_averages);
    }
}
