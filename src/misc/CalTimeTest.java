package misc;

import java.util.Calendar;

public class CalTimeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Calendar calendar  = Calendar.getInstance();
		calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 1);
		System.out.println(calendar.getTime());
	}

}
