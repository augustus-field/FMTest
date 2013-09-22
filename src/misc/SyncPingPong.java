package misc;

import java.util.concurrent.TimeUnit;

public class SyncPingPong {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static synchronized void main(String[] args) throws InterruptedException {
		
		Thread t = new Thread(){
			@Override
			public void run() {
				for(int i=0;i<100;i++){
					pong();
				}
			}
		};
		t.start();
		TimeUnit.SECONDS.sleep(1000);
		System.out.println("Ping");
		
//		bumpUp();
	}

	static synchronized void pong (){
//		System.out.println("pong holds SyncPingPong class: "+Thread.holdsLock(SyncPingPong.class));
		System.out.println("Pong");
	}
	
	
	static void bumpUp(){
		synchronized (lock) {
			System.out.println("BumpUp");
			bumpDown();
		}
	}
	static void bumpDown(){
		synchronized (lock) {
			System.out.println("BumpDown");
		}
	}
	
	private  static Object lock = new Object();
}
