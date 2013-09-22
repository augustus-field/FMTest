package util;

/**
 * 获取相邻两次调用{@link #getTimeDiff}的时间差. 
 * @author Chen Li
 */
public class TimeLogger {
	private long[] timeStamps = new long[2];
	private int index = 0;

	public TimeLogger(){
		timeStamps[index++%2]=System.currentTimeMillis();
	}

	/**
	 * @return 当前时间与上次调用本方法的时间差.
	 */
	public long getTimeDiff(){
		timeStamps[index++%2]=System.currentTimeMillis();
		return Math.abs(timeStamps[1]-timeStamps[0]);
	}
}