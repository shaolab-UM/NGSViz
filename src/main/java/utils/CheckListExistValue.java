package utils;

import java.util.List;

/**
 * @author Benchen Ye
 * @create 2024-11--18:41
 */
public class CheckListExistValue {
    public static boolean checkListExistValue(List<Integer> list_name) {
        for (int i = 0; i < list_name.size(); i++) {
            int num = list_name.get(i);
            if (num != 0) {
                System.out.println("---------");
                System.out.println("---------");
                System.out.println("check list exist greater 0 value is : " + num);
                System.out.println("the id is : " + i);
                System.out.println("---------");
                System.out.println("---------");
                return true;
            }
        }
        return false;
    }
}
