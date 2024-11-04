package coverageCalculator;

import java.util.ArrayList;

/**
 * @author Benchen Ye
 * @create 2024-11--08:58
 * @function trim the buffer items in the both sides of list with length of bufsize
 */
public class TrimBuffer {
    public static ArrayList<Integer> trimBuffer(ArrayList<Integer> originalList, int bufsize) {
        int length = originalList.size();
        ArrayList<Integer> resultList = new ArrayList<>(originalList.subList(bufsize, length - bufsize));
        return resultList;
    }
}
