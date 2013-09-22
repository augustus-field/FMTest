package misc.producer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ProducerConsumerTest {
	public static AtomicInteger count = new AtomicInteger(0);
	public static void main(String[] args) {
		final Producer producer = Producer.getInstance();
		final Consumer consumer = Consumer.getInstance();
		Thread producerThread = new Thread(){
			public void run() {
				while (true){
					try {
						producer.produce();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		Thread consumerThread = new Thread(){
			public void run() {
				while (true){
					try {
						consumer.consume();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		producerThread.start();
		consumerThread.start();
	}
	
	

}
