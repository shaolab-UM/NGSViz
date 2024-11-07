/**
 * @author Benchen Ye
 * @create 2024-11--21:36
 */
public class NGSplot2Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No command line arguments received.");
        } else {
            System.out.println("No command line arguments received:");
            for (int i = 0; i < args.length; i++) {
                System.out.println("Parameter " + (i + 1) + ": " + args[i]);
            }
        }
    }
}
