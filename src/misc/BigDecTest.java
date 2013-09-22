package misc;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class BigDecTest {


	public static void main(String[] args) {
//		BigDecimal bd = new BigDecimal("1023214");
//		System.out.println(bd.toString());
//		String.format("%d", bd.intValue());
//
//		// float/double comparison
//		Float f = 1.232f;
//		double d = 1.232;
//		System.out.println(f);
//		System.out.println(d);
//		System.out.println("float/double 1.232 equals: "+(f.equals(d)));
//		
//		// Null dereference in fore
//		List<Integer> lis = null; 
//		for (Integer integer : lis) {
//			System.out.println(integer);
//		}
		
//		Integer.parseInt("0.99");
//		float a = 0.88f;
//		double b =a;
//		System.out.println(b);
//		
//		int c = (int) Math.floor(0.52f);
//		System.out.println(c);
		
//		System.out.println((Integer.MAX_VALUE+Integer.MAX_VALUE));
		String s = "1.19";
		String f = "1.19";
		boolean test = Float.valueOf(s.toString()) == (int) 0;
		boolean testStr = (s.equals(f));
		System.out.println(test);
		System.out.println("testStr="+testStr);
		System.out.println(Float.valueOf(s));
		
		System.out.println(Float.MAX_VALUE);
	}

}
