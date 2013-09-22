package misc.producer;

import java.util.concurrent.TimeUnit;
import misc.producer.ProducerConsumerTest;

public class Consumer{
	private Consumer(){}
	private static Consumer consumer = null;
	public static Consumer getInstance(){
		if(consumer==null){
			consumer= new Consumer();
		}
		return consumer;
	}
	
	public void consume() throws InterruptedException{
		TimeUnit.SECONDS.sleep(5);
		System.out.println("consuming "+ProducerConsumerTest.count.decrementAndGet());
	}
}
