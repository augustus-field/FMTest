package concurrentRequest;


import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ConRequest {
	private static Properties p = new Properties();
	private static Logger log = Logger.getLogger(ConRequest.class.getCanonicalName());

	public static final String WORK_DIR = System.getProperty("user.dir");;
	static {
		InputStream is = null; 
		try {
			is = new FileInputStream(WORK_DIR+"/db.properties");
		} catch (Exception e1) {
			log.info("未找到db.properties文件, 使用默认设置!");
		}
		
		if(is==null)
			is = ConRequest.class.getClassLoader().getResourceAsStream("db.properties");
		try {
			p.load(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

//	public static final String BASE_URL =  p.getProperty("remote_base_url");
//	public static final String STR_LOGIN = p.getProperty("remote_login");
	public static final String BASE_URL =  p.getProperty("local_base_url");
	public static final String STR_LOGIN = p.getProperty("local_login");
//	public static final String BASE_URL =  p.getProperty("stable_test_url");
//	public static final String STR_LOGIN = p.getProperty("stable_test_login");

	public static final String STR_GET_MARKET_BASE = "marketInfo.sv";
	public static final String STR_PUSH_ORDER = "orderManage.sv?act= pushOrder";
	public static final String STR_USER_NAME = StringUtils.substringsBetween(STR_LOGIN, "'un':'", "',")[0] ;
	
	public static final String URI_CHARSET = "UTF8";
	public static final String IGNORE_MINUTE_DIFF = p.getProperty("ignore_minutes_diff","0").trim();
	
	
	public static void main(String[] args) {
		
		log.info("比较全量与增量信息开始!");
		
		// TODO use different log files for each thread
		
		Thread marketRequest = new Thread(new RunnableMarketsRequest());
		marketRequest.start();

		Thread orderRequest = new Thread(new RunnableOrderRequest());
		orderRequest.start();
		
	}
	
	
	
	
}
