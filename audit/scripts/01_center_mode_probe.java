import com.NGSViz.configSet.InputParameterAttributes;
import com.NGSViz.coverageCalculator.DataScaler;

import java.lang.reflect.Method;
import java.util.Arrays;

class CenterModeProbe {
    private static long countValue(double[] values, double target) {
        return Arrays.stream(values).filter(value -> value == target).count();
    }

    public static void main(String[] args) throws Exception {
        InputParameterAttributes.num_datapoints = 100;
        InputParameterAttributes.interval_type = "broad_interval";
        InputParameterAttributes.flank_points = 50;
        InputParameterAttributes.middle_points = 1;
        InputParameterAttributes.CenterMode = true;

        int flankSize = 200;
        int[] coverage = new int[1000];
        Arrays.fill(coverage, 0, 200, 1);
        Arrays.fill(coverage, 200, 800, 7);
        Arrays.fill(coverage, 800, 1000, 2);

        double[] publicResult = DataScaler.getCoverageScaled(coverage, "mean", flankSize);

        Method centerMethod = DataScaler.class.getDeclaredMethod(
                "centerScaleByGeneCenter",
                int[].class,
                int.class,
                int.class,
                String.class
        );
        centerMethod.setAccessible(true);
        double[] centerResult = (double[]) centerMethod.invoke(
                null,
                coverage,
                flankSize,
                101,
                "mean"
        );

        System.out.printf("public_center_bin=%.1f%n", publicResult[50]);
        System.out.printf("public_gene_bins=%d%n", countValue(publicResult, 7.0));
        System.out.printf("private_center_gene_bins=%d%n", countValue(centerResult, 7.0));
        System.out.printf("public_matches_private_center=%s%n", Arrays.equals(publicResult, centerResult));

        if (!Arrays.equals(publicResult, centerResult)) {
            throw new AssertionError("Center-mode result is overwritten by broad-interval scaling");
        }
    }
}
