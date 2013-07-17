package deadlockTest;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;


public class SubLockTest {
	private static AtomicInteger count = new AtomicInteger(0);

	private Log logger = LogFactory.getLog(this.getClass());
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws InterruptedException {
		StockDataCustomer user = new StockDataCustomer();
		for(int i=0;;i++){
			logger.info("Processing message "+i + " for in thread: "+i%1000);
			final long id = i%1000;
			user.putStock2Cache(id, "Message: "+i);
			Random rand = new Random();
			TimeUnit.MILLISECONDS.sleep(rand.nextInt(1000)+1);
		}
	}

	public class StockDataCustomer {
		private Log logger = LogFactory.getLog(this.getClass());
		private Dummy dummy = new Dummy();

		/** 消费线程有效时长 */
		private static final long VALID_TIME = 2 * 60 * 60 * 1000L;// 2hour

		/**
		 * 缓存预处理的盘口数据，key：赛事ID，value：StockDataBean
		 */
		private Map<Long, StockDataBean> stockMap = new ConcurrentHashMap<Long, StockDataBean>();

		/**
		 * 缓存盘口数据
		 * 
		 * @param macthId
		 * @param stockXml
		 */
		public void putStock2Cache(Long macthId, String stockXml) {
			logger.debug("macthId="+macthId+":\n"+stockXml);
			if (null == stockMap.get(macthId)) {
				StockDataBean bean = new StockDataBean(macthId, stockXml, System.currentTimeMillis() + VALID_TIME);
				stockMap.put(macthId, bean);
				bean.handle();
			} else {
				StockDataBean bean = stockMap.get(macthId);
				bean.stockXml = stockXml;
				bean.receiveCount++;
				synchronized (bean.lockObj) {
					bean.lockObj.notifyAll();
				}
			}
		}

		class StockDataBean {
			public long macthId;
			public String stockXml;
			public long expireTime;// 过期时间戳，用于回收消费线程
			public Object lockObj = new Object();
			public long receiveCount = 0;
			public long oldReceiveCount = 0;

			public StockDataBean() {
			}

			public StockDataBean(long macthId, String stockXml, long expireTime) {
				super();
				this.macthId = macthId;
				this.stockXml = stockXml;
				this.expireTime = expireTime;
			}

			public void handle() {
				new Thread(new Runnable() {
					public void run() {
						long tempCount = 0;
						while (expireTime >= System.currentTimeMillis()) {
							// 1、控制赛事收盘后，线程还去处理业务 2、控制历史报文再次处理
							tempCount = receiveCount;
							if (tempCount > oldReceiveCount) {
								dummy.longCall();
								oldReceiveCount = tempCount;
							}
							try {
								synchronized (lockObj) {
									lockObj.wait(60 * 1000);// 1MIN
								}
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						stockMap.remove(macthId);// 清理
					}
				},"Handler-"+count.incrementAndGet()).start();
			}
		}
	}
}
