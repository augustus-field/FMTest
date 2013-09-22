package misc.producer;

import java.util.concurrent.TimeUnit;
import misc.producer.ProducerConsumerTest;

public class Producer {
	
	public static Producer getInstance(){
		if(producer==null){
			producer = new Producer();
		}
		return producer;  
	}
	private Producer (){}
	private static Producer producer = null;

	public void produce() throws InterruptedException{
		TimeUnit.SECONDS.sleep(1);
		System.out.println("sending "+ProducerConsumerTest.count.incrementAndGet());
	}
}
