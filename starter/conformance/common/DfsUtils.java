package conformance.common;

/**
 * Created by Sreejith Unnikrishnan on 5/11/16.
 */
public class DfsUtils {
    private static boolean isDebugEnabled = true;

    public static void safePrintln(String s){
        if (isDebugEnabled){
            synchronized (System.out){
                System.out.println("DEBUG: " + s);
            }
        }
    }
}
