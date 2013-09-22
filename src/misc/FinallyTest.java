package misc;

import java.util.concurrent.TimeUnit;

public class FinallyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("INside try");
			TimeUnit.SECONDS.sleep(3);
			return;
		} catch (Exception e) {
			System.out.println("inside catch");
		} finally{
			System.out.println("inside finally");
		}
	}

}
