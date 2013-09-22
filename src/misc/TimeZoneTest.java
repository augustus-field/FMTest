package misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import deadlockTest.DeadLockTest;

public class TimeZoneTest {
	public static void main(String[] args) throws InterruptedException, BrokenBarrierException {
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//		System.out.println(sdf.format(new Date()));
//		
//		int offset = TimeZone.getDefault().getRawOffset();
//		System.out.println(TimeZone.getDefault().getDisplayName()+" offset : "+offset/(3600*1000));
		
		final Counter counter = new Counter(0);
		Runnable runnable = new Runnable() {
			public void run() {
				counter.increment();
			}
		};
		CyclicBarrier startBarrier = new CyclicBarrier(10);
		CyclicBarrier endBarrier = new CyclicBarrier(10);		
		for(int i=0;i<9;i++){
			new BarrierContainer(runnable, startBarrier, endBarrier).start();
		}
		
//		DeadLockTest.startDeadlockDetector(0, 5);
		
		System.out.println("counts: "+counter.getValue());
		
		startBarrier.await();
		System.out.println("counts: "+counter.getValue());
		endBarrier.await();
		System.out.println("counts: "+counter.getValue());
	}
	
	static class Counter {
		private int count;
		public Counter(int count){
			this.count=count;
		}
		public void increment(){
			count++;
		}
		public int getValue(){
			return count;
		}
	}
	
	static class BarrierContainer extends Thread{
		private CyclicBarrier startBarrier, endBarrier;
		public BarrierContainer(Runnable runnable, CyclicBarrier startBarrier, CyclicBarrier endBarrier){
			super(runnable);
			this.setName("barrierContainer thread: ");
			this.startBarrier=startBarrier;
			this.endBarrier=endBarrier;
		}
		@Override
		public void run(){
			try {
				startBarrier.await();
				super.run();
				endBarrier.await();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}	
			
		}
	}
}
