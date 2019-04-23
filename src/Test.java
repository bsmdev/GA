import java.util.ArrayList;
import java.util.List;

/**
 * Created by Andreas on 04.02.2019.
 */
public class Test {


    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        System.out.println(list.contains(1));
        System.out.println(list.contains(4));
    }
}
