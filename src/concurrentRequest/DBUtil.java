package concurrentRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sgfm.api.test.TimesTenConnTest;

public class DBUtil {
	private static Logger log = Logger.getLogger(DBUtil.class);
	private static String ttUrl;
	private static String ttName;
	private static String ttpwd;
	private static String ttdriver;
	private static String oraUrl;
	private static String oraName;
	private static String orapwd;
	private static String oradriver;
	public static final String WORK_DIR = System.getProperty("user.dir");;
	
	static{
		Properties conf = new Properties();
		InputStream is = null; 
		try {
			is = new FileInputStream(WORK_DIR+"/db.properties");
		} catch (Exception e1) {
			log.info("未找到db.properties文件, 使用默认设置!");
		}
		if(is==null)
			is = TimesTenConnTest.class.getClassLoader().getResourceAsStream("db.properties");
		try {
			conf.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ttUrl = conf.getProperty("realTimeDataSource.core.url");
		ttName = conf.getProperty("realTimeDataSource.core.username");
		ttpwd = conf.getProperty("realTimeDataSource.core.password");
		ttdriver = conf.getProperty("realTimeDataSource.core.driverClassName");
		
		oraUrl = conf.getProperty("oracleDataSource.core.url");
		oraName = conf.getProperty("oracleDataSource.core.username");
		orapwd = conf.getProperty("oracleDataSource.core.password");
		oradriver = conf.getProperty("oracleDataSource.core.driverClassName");
		
		try {
			Class.forName(ttdriver);
			Class.forName(oradriver);
		} catch (ClassNotFoundException e) {
			log.error(e);
		}
	}
	public static Connection getTTConnection() throws SQLException{
		return DriverManager.getConnection(ttUrl, ttName, ttpwd);
	}
	
	public static Connection getOraConnection () throws SQLException {
		return DriverManager.getConnection(oraUrl, oraName, orapwd);
	}
	
	public static void close(Connection connection){
		if(connection!=null){
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	
	
}
