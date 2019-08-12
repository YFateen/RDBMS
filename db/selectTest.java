package db;

/**
 * Created by Yusuf on 3/1/2017.
 */
public class selectTest {
    public static void main(String[] args) {
        Float value = new Float(340282346638528860000000000000000000000.000);
        Float value1 = new Float(349283019238012300000000.000);
        String strRepr = strRepr = Float.toString(value);
        System.out.println(value);
        System.out.println(strRepr);
        System.out.println(340282346638528860000000000000000000000.000);
        System.out.println(String.format("%2$.3f", value, value1));
    }
}