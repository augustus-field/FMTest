package misc;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class BigDecTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BigDecimal bd = new BigDecimal("1023214");
		System.out.println(bd.toString());
		String.format("%d", bd.intValue());

		// float/double comparison
		Float f = 1.232f;
		double d = 1.232;
		System.out.println(f);
		System.out.println(d);
		System.out.println("float/double 1.232 equals: "+(f.equals(d)));
		
		// Null dereference in fore
		List<Integer> lis = null; 
		for (Integer integer : lis) {
			System.out.println(integer);
		}
	}

}
