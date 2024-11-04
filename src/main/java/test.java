import java.util.ArrayList;

/**
 * @author Benchen Ye
 * @create 2024-11--11:35
 */
public class test {
    public static void main(String[] args) {
        ArrayList<Integer> integerList = new ArrayList<>();
        integerList.add(10);
        integerList.add(20);
        integerList.add(30);

        long librarySize = 500000;

        ArrayList<Double> resultList = new ArrayList<>();

        for (Integer num : integerList) {
            double result = (double) num / librarySize * 1e6;
            resultList.add(result);
        }

        for (Double num : resultList) {
            System.out.print(num + " ");
        }
    }
}
