package coverageCalculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-09--20:25
 */

public class CoverageMatrixObj {
    private Map<String, ArrayList<Integer>> matrix;

    public CoverageMatrixObj() {
        matrix = new LinkedHashMap<>();
    }

    public void addRow(String rowName, ArrayList<Integer> rowData) {
        matrix.put(rowName, rowData);
    }

    public ArrayList<Integer> getRow(String rowName) {
        return matrix.get(rowName);
    }

    public void printMatrix() {
        for (Map.Entry<String, ArrayList<Integer>> entry : matrix.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (int value : entry.getValue()) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

}