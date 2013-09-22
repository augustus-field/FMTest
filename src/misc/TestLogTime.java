package misc;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import util.TimeLogger;

public class TestLogTime {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		TimeLogger tl = new TimeLogger();
		LogFactory.getLog(TestLogTime.class);
		System.out.println("Create log takes time: "+tl.getTimeDiff());
		LogFactory.getLog(TimeLogger.class);
		System.out.println("Create log by name takes time: "+tl.getTimeDiff());
		TimeUnit.SECONDS.sleep(-1);
		System.out.println("Sleep time: "+tl.getTimeDiff());
	}

}
