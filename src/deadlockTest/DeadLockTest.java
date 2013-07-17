package deadlockTest;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;



public class DeadLockTest {
	private final static ThreadMXBean tmb = ManagementFactory.getThreadMXBean();

	public static long[] getDeadLockThreadIds(){
		return tmb.findDeadlockedThreads();
	}
	
	/**
	 * Searching for deadlocked threads.
	 * @param sec Time to wait before start searching for deadlocks
	 * @throws InterruptedException
	 */
	public static void printDeadLockThreadInfo(long sec) throws InterruptedException{
		if(sec>0){
			Thread.currentThread().join(sec*1000);
		}
		long[] ids = getDeadLockThreadIds();
		if(ids!=null){
			StringBuilder lockDesc = new StringBuilder();
			for (long id : ids) {
//				for (Thread t : Thread.getAllStackTraces().keySet()) {
//					if(t.getId()==id){
//						// System.out.println("Deadlocked thread: " + t.getName());
//						// logger.error("发现死锁线程: "+t.getName());
//						throw new DeadlockException("发现死锁线程"+t.getName());
//					}
//				}
				ThreadInfo ti = tmb.getThreadInfo(id, 10);
				LockInfo li = ti.getLockInfo();
				lockDesc.append(String.format("Thread %s deadlocked, lock held: %s, stacktrace: \n", 
						ti.getThreadName(), (li == null) ? "na, " : li.getClassName()));
				StackTraceElement[] sts = ti.getStackTrace();
				for (int i=0; sts!=null && i<sts.length; i++) {
					StackTraceElement stackTraceElement = sts[i];
					lockDesc.append(String.format(" --%s, %s, at line %s\n", 
							stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber()));
				}
			}
			throw new RuntimeException(lockDesc.toString());
		}
	}
	
	public static void printDeadLockThreadInfo() throws InterruptedException{
		printDeadLockThreadInfo(0);
	}
	
	/**
	 * 启动后台死锁检测.
	 * @param delay 启动等待时间(秒数)
	 * @param period 检测周期(秒数)
	 */
	public static void startDeadlockDetector(long delay, long period){
		Thread t = new Thread(new DeadLockDetectorRunnable(delay, period), "DeadlockDetector");
		t.start();
	}
	
	private static class DeadLockDetectorRunnable implements Runnable {
		private long delay, period;
		public DeadLockDetectorRunnable(long delay, long period){
			this.delay=delay;
			this.period=period;
		}
		
		@Override
		public void run() {
			
			try {
				if (delay > 0) {
					Thread.currentThread().join(delay*1000L);
				}

				while (true) {
					printDeadLockThreadInfo();
					Thread.sleep(period*1000L);
				}
		
			} catch (InterruptedException consumed) {
			}

		}
	}
}
