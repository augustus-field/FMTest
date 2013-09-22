package misc;

public class ShiftOperatorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean a = true;
		boolean b = false;
		System.out.println("a^b: "+(a^b));
		boolean c=false;
		System.out.println("a^b^c: "+(a^b^c));
		
		long d = 1; 
		System.out.println("d<<31:"+Long.toBinaryString((d<<31)));
		System.out.println("d>>31:"+Long.toBinaryString((d>>31)));
		System.out.println("d>>31:"+Long.toBinaryString((-d>>30)));
		
		Class clz = java.lang.ClassLoader.class;
	}

}
