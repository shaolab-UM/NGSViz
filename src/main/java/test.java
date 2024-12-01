import configSet.InputParameterAttributes;
import configSet.ProcessInputParameters;
import coverageCalculator.LargeIntervalMain;
import coverageCalculator.MainCalculator;
import htsjdk.samtools.Chunk;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;

/**
 * @author Benchen Ye
 * @create 2024-11--11:35
 */
public class test extends InputParameterAttributes {
    public static void main(String[] args) {
// 示例数据
        double[][] coverageScaledMatrix = {
                {1.0, 2.0, 3.0},
                {4.0, 5.0, 6.0}
        };
        double[][] bkgCoverageScaledMatrix = {
                {0.5, 1.5, 2.5},
                {3.5, 4.5, 5.5}
        };
        double resultPseudoRpm = 0.1;
        double bkgPseudoRpm = 0.2;

        // 计算resultMatrix
        double[][] resultMatrix = calculateLog2Matrix(coverageScaledMatrix, bkgCoverageScaledMatrix, resultPseudoRpm, bkgPseudoRpm);

        // 输出结果
        for (double[] row : resultMatrix) {
            for (double value : row) {
                System.out.printf("%.4f ", value);
            }
            System.out.println();
        }
    }

    public static double[][] calculateLog2Matrix(double[][] coverageScaledMatrix, double[][] bkgCoverageScaledMatrix, double resultPseudoRpm, double bkgPseudoRpm) {
        int rows = coverageScaledMatrix.length;
        int cols = coverageScaledMatrix[0].length;
        double[][] resultMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                double numerator = coverageScaledMatrix[i][j] + resultPseudoRpm;
                double denominator = bkgCoverageScaledMatrix[i][j] + bkgPseudoRpm;
                resultMatrix[i][j] = Math.log(numerator / denominator) / Math.log(2);
            }
        }
        return resultMatrix;
    }

}
