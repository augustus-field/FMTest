package misc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import util.TimeLogger;

public class ThreadPoolInitTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		TimeLogger tl = new TimeLogger();
		ExecutorService service = Executors.newFixedThreadPool(1000);
		service.shutdown();
		System.out.println("init service time: "+tl.getTimeDiff());
	}

}
