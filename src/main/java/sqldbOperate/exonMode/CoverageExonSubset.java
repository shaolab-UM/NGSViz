package sqldbOperate.exonMode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--21:01
 */
public class CoverageExonSubset {
    public static ArrayList<Integer> getCoverageExonSubset(ArrayList<Integer> physical_coverage, String tid_name,
                                                           int query_range_start, int query_range_end) {
        System.out.println("---------");
        System.out.println("Processing exon coverage, its id is : " + tid_name);
        int range_len = query_range_end - query_range_start + 1;
        List<Integer> exon_coverage_list = new ArrayList<>();
        List<Integer> exon_start_list = ExonModelData.getExonStartList(tid_name);
        List<Integer> exon_end_list = ExonModelData.getExonEndList(tid_name);
        int exon_list_len = exon_start_list.size();

        for(int i=0;i<exon_list_len;i++){
            int exon_start = exon_start_list.get(i) - query_range_start + 1;
            int exon_end = exon_end_list.get(i) - query_range_start + 1;
            // adjust the value of first start and last end
            if(i==0){
                exon_start = 0;
            }
            if(i==exon_list_len-1){
                exon_end = range_len;
            }
            System.out.println("---------");
            System.out.println("---------");
            System.out.println("start is : " + exon_start);
            System.out.println("end is : " + exon_end);

            //
            System.out.println("---------");
            System.out.println("---------");
            List<Integer> sub_List = physical_coverage.subList(exon_start, exon_end);
            System.out.println("sub coverage is : " + sub_List);
            exon_coverage_list.addAll(sub_List);
        }
        ArrayList<Integer> exon_coverage = new ArrayList<>(exon_start_list);
        System.out.println("---------");
        return exon_coverage;
    }
}
