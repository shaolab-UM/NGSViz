final class ContigBoundaryClampProbe {
    private static void printBroadCase(
            int contigLength,
            int start,
            int end,
            int flank,
            int buffer
    ) {
        int queryStart = Math.max(1, start - flank - buffer);
        int queryEnd = Math.min(contigLength, end + flank + buffer);

        int fixedTrimStart = queryStart + buffer;
        int fixedTrimEnd = queryEnd - buffer;
        int desiredStart = Math.max(1, start - flank);
        int desiredEnd = Math.min(contigLength, end + flank);

        int actualLeftBuffer = desiredStart - queryStart;
        int actualRightBuffer = queryEnd - desiredEnd;

        System.out.printf(
                "query=%d-%d fixedTrim=%d-%d desired=%d-%d actualBuffer=%d,%d%n",
                queryStart,
                queryEnd,
                fixedTrimStart,
                fixedTrimEnd,
                desiredStart,
                desiredEnd,
                actualLeftBuffer,
                actualRightBuffer
        );
    }

    private static void printBinCase(int regionLength, int pointCount) {
        int chunkSize = regionLength / pointCount;
        int zeroWidthBins = chunkSize == 0 ? pointCount - 1 : 0;
        System.out.printf(
                "regionLength=%d pointCount=%d chunkSize=%d zeroWidthBins=%d%n",
                regionLength,
                pointCount,
                chunkSize,
                zeroWidthBins
        );
    }

    public static void main(String[] args) {
        printBroadCase(1000, 950, 995, 50, 10);
        printBroadCase(1000, 1, 50, 50, 10);
        printBinCase(0, 21);
        printBinCase(10, 21);
    }
}
