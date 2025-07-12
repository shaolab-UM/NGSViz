package com.NGSViz.coverageCalculator;

import java.util.Arrays;

/**
 * @author Benchen Ye
 * @create 2024-11--08:58
 * @function trim the buffer items in the both sides of list with length of bufsize
 */
public class TrimBuffer {
    public static int[] trimBuffer2(int[] originalList, int bufsize) {
        int length = originalList.length;
        int[] resultList = Arrays.copyOfRange(originalList, bufsize, length - bufsize);
        return resultList;
    }
}

