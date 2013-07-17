package com.sgfm.api.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This class is for testing direct (or client) connection to timesten 
 * @author Chen Li
 *
 */

public class TimesTenConnTest{
	private static String driver,url,user,password;
	private Connection conn = null;
	private static Properties conf = new Properties();
	static {
		
		String workingDir = System.getProperty("user.dir");
		InputStream is = null; 
		try {
			is = new FileInputStream(workingDir+"/db.properties");
		} catch (Exception e1) {
			// e1.printStackTrace();
		}
		
		if(is==null){
			System.out.println(workingDir+"/db.properties"+" not found, use default configuration. ");

            is = TimesTenConnTest.class.getClassLoader().getResourceAsStream("db.properties");
		}
		
		try {
			conf.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//@Before
	public void initOracle() throws Exception{
	// oracle db driver
		driver = conf.getProperty("oracleDataSource.core.driverClassName");
		url = conf.getProperty("oracleDataSource.core.url");
		user= conf.getProperty("oracleDataSource.core.username");
		password = conf.getProperty("oracleDataSource.core.password");
		Class.forName(driver);
		conn  = DriverManager.getConnection(url, user, password);
		
	}
	
	public void initTimesten() throws Exception {
		// tt db driver
		driver = conf.getProperty("realTimeDataSource.core.driverClassName");
		url = conf.getProperty("realTimeDataSource.core.url");
		user= conf.getProperty("realTimeDataSource.core.username");
		password = conf.getProperty("realTimeDataSource.core.password");
		Class.forName(driver);
		conn = DriverManager.getConnection(url, user, password);
	}
	
	public void testQuery() throws SQLException{
		String query = conf.getProperty("test.query");
		System.out.println("connected to "+url);
		java.sql.Statement st = conn.createStatement();
		java.sql.ResultSet rs = st.executeQuery(query);
		
//		java.sql.ResultSet rs = st
//				.executeQuery("select * from t_intention ");
		int count = 0;
		while (rs.next()) {
//			System.out.println(rs.getString("table_name"));
			count++;
		}
		System.out.println(count);
		
		conn.close();
	}
	
	public static void main(String[] args) throws Exception {
		TimesTenConnTest test = new TimesTenConnTest();
		long st = System.currentTimeMillis();
		try {
			test.initTimesten();
			test.testQuery();
			System.out.println("Timesten connection success!");
		} catch (Exception e) {
			System.out.println("Timesten connection failed!");	
			e.printStackTrace();
		}
		long tt = System.currentTimeMillis();
		System.out.println("Timesten test time consumed: "+(tt-st));
		
		try {
			test.initOracle();
			test.testQuery();
			System.out.println("Oracle connection success!");
		} catch (Exception e) {
			System.out.println("Oracle connection failed!");
			e.printStackTrace();
		}
		System.out.println("Oracle test time consumed: "+(System.currentTimeMillis()-tt));
		
	}
}