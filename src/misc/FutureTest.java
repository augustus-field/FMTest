package misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import util.TimeLogger;

public class FutureTest {

	public static void main(String[] args) {
		startCleanServiceThread();
		TimeLogger tl = new TimeLogger();
		ExecutorService service = Executors.newCachedThreadPool();
		for(int i=0;i<10;i++){
			System.out.println("counter: "+i+" wait-list size:"+list.size());
			Future ft = service.submit(new SlowCallable());
			Object result = null;
			try {
				result = ft.get(1, TimeUnit.SECONDS);
				System.out.println("Get result "+result);
			} catch (TimeoutException e){
				System.out.println("Calling slow method timeout, sending for late processing!");
				// Wait and process the result in another thread.
				joinService(ft);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		System.out.println("Main task completes in "+tl.getTimeDiff());
	}
	
	/**
	 * A slow callable  
	 */
	static class SlowCallable implements Callable<String> {
		private static final int MAX = (int) 1E8;
		public String call() throws Exception {
			String result="init-val";
//			System.out.println("Slow callable starts..");
			for(int i=0;i<MAX;i++){
				// Playing numbers
				result = i+"";
			}
//			System.out.println("Slow callable ends.");
			return result;
		}
	}
	
	private static ExecutorService multiService = Executors.newFixedThreadPool(10);
	private static List<Future> list = new ArrayList<Future>();
	
	/**
	 * Add future of slow tasks into a thread pool for processing later. 
	 * @param ft
	 */
	static void joinService(final Future<?> ft){
		list.add(multiService.submit(new Callable<Object>() {
			public Object call() throws Exception {
				return ft.get();
			}
		}));
	}
	
	/**
	 * Wait for return of results from all slow tasks
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	static void cleanService() throws InterruptedException, ExecutionException{
		for(int i=list.size()-1;i>=0;i--){
			Object obj = list.get(i).get();
			System.out.println("Cleaned result: "+obj);
			list.remove(i);
		}
	}
	
	/**
	 * Start a thread for cleaning results returned by slow tasks
	 */
	static void startCleanServiceThread(){
		TimerTask task = new TimerTask() {
			public void run() {
				if(list.size()<1){
					System.out.println("Nothing to clean, I'll quit.");
				}
				try {
					cleanService();
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		};
		Timer t = new Timer();
		t.schedule(task, 3000, 10000);
	}
	
}
