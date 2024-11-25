package sqldbOperate.exonMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benchen Ye
 * @create 2024-11--16:29
 */
public class OneEnstAllExonCoorClass {
    public String enst_name;
    public int start_enst;
    public int end_enst;
    public int width_enst;
    Map<String, Object> enst_map = new HashMap<>();
    List<Integer> start_list = new ArrayList<Integer>();
    List<Integer> end_list = new ArrayList<Integer>();
    List<Integer> width_list = new ArrayList<Integer>();
    //public
    public OneEnstAllExonCoorClass(String enst_name,
                                   int start_enst,
                                   int end_enst,
                                   int width_enst,
                                   List<Integer> start_list,
                                   List<Integer> end_list,
                                   List<Integer> width_list)
    {
        this.enst_name = enst_name;
        this.start_enst = start_enst;
        this.end_enst = end_enst;
        this.width_enst = width_enst;
        this.start_list = start_list;
        this.end_list = end_list;
        this.width_list = width_list;
        this.enst_map.put("start_enst", start_enst);
        this.enst_map.put("end_enst", end_enst);
        this.enst_map.put("width_enst", width_enst);
        this.enst_map.put("start_list", start_list);
        this.enst_map.put("end_list", end_list);
        this.enst_map.put("width_list", width_list);
    }
    public void printRes(){
        System.out.println("---------");
        System.out.println("Print the enst_name is: " + enst_name);
        System.out.println("enst_name is :" + enst_name);
        System.out.println("start_enst is :" + start_enst);
        System.out.println("end_enst is :" + end_enst);
        System.out.println("width_enst is :" + width_enst);
        System.out.println("start_list is :" + start_list);
        System.out.println("end_list is :" + end_list);
        System.out.println("width_list is :" + width_list);
        System.out.println("enst_map is :" + enst_map);
        System.out.println("---------");
    }
    public int getExonWholeWidth(){
        return width_enst;
    }
    public int getExonStart(){
        return start_enst;
    }
    public int getExonEnd(){
        return end_enst;
    }
    public Map<String, Object> getExonMatrixMap(){
        return enst_map;
    }
    public List<Integer> getStartList(){
        return start_list;
    }
    public List<Integer> getEndList(){
        return end_list;
    }
}
