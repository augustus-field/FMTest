package deadlockTest;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Dummy {
	private AtomicInteger count = new AtomicInteger(0);
	private Log logger = LogFactory.getLog(this.getClass());
	public void longCall(){
		try {
			logger.info("long call starts"+count.incrementAndGet());
			Random rand = new Random();
			TimeUnit.SECONDS.sleep(rand.nextInt(10)+1);
			logger.info("long call ends"+count.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
