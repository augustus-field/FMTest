package h2Test;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
/**
 * 比较H2操作性能
 * @author chenli
 *
 */
public class H2OpTest {
	private PrintWriter pw ;
	private static final int MAX_COUNT = 60000;
	private static final int HOUR_IN_MILLIS = 3600*1000;
	private Connection conn;
	private Random rand=new Random();
	private long currTime = System.currentTimeMillis(); // 当前时间
	private int duration = 24*365; // 起始与当前的天数差
	@Before
	public void setUp() throws Exception {
		//File file = new File(H2OpTest.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"H2OpTest.log");
		File file = new File("H2OpTest.log");
		System.out.println("Writing to "+file.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(file,true);
		pw = new PrintWriter(new OutputStreamWriter(fos,"utf8"));
		
		Class.forName("org.h2.Driver");
	}
	
	
	private void openConn() throws SQLException {
		conn = DriverManager.getConnection("jdbc:h2:~/test;MODE=Oracle;MVCC=TRUE;CACHE_SIZE=65536","sa","");
		System.out.println("Connection established, "+conn);
	}

	@Ignore
	@Test
	public void testAll() throws SQLException{
		init();
		for (int i = 0; i < 0; i++) {
			init();
			// testQueryByIds();
			testQueryBySepIds();
			pw.append("\n");
			
			init();
			// testQueryByConds();
			testQueryBySepIdsWithClearBatch();
			pw.append("\n");
			
			init();
			// testQueryByConds();
			testQueryByIds();
			pw.append("\n");
			
//			if(i%4==3){
//				conn.close();
//				conn = DriverManager.getConnection("jdbc:h2:~/test;MODE=Oracle;MVCC=TRUE","sa","");
//				pw.append("\n");
//			}
		}
	}
	
	public static void main(String[] args) throws SQLException {
		H2OpTest tester = new H2OpTest();
		tester.openConn();
		
		
		
		tester.closeConn();
	}
	
	private void init() throws SQLException {
		openConn();
		cleanTableAndIndex();
		initSchema();
		initFakeData();
		initIndex();
		closeConn();
	}

	private void cleanTableAndIndex() throws SQLException {
		String sql = "drop table if exists T_TEST";
		Statement stmt  = conn.createStatement();
		stmt.execute(sql);
		sql = "drop index if exists T_TEST_INDEX";
		stmt.execute(sql);
	}
	
	/**
	 * 创建index 
	 */
	private void initIndex() throws SQLException {
		String sql ="create index T_TEST_INDEX on T_TEST (t_id)";
		Statement stmt  = conn.createStatement();
		System.out.println("Trying to create index!");
		if(stmt.execute(sql))
			System.out.println("Index created!");
	}

	/**
	 * 创建化数据库表格
	 */
	private void initSchema() throws SQLException {
		String sql= "create table  T_TEST  (t_id int, t_desc varchar(16), t_info varchar(32), t_time bigint)";
		Statement stmt = conn.createStatement();
		stmt.execute(sql);
		// 测试建表是否成功
		sql = "select count(*) from INFORMATION_SCHEMA.TABLES where table_name='T_TEST'";
		PreparedStatement pstmt  = conn.prepareStatement(sql);
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		if(rs.getInt(1)>0)
			System.out.println("Table created!");
	}

	/**
	 * 初始化数据库表格
	 */
	private void initFakeData() throws SQLException {
		String sql = "insert into T_TEST  values (?,?,?,?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for(int i=0;i<MAX_COUNT*10;i++){
			pstmt.setInt(1, i+1);
			pstmt.setString(2, "desc: "+i);
			pstmt.setString(3, "info: "+i);
			pstmt.setLong(4, currTime-rand.nextInt(duration)*HOUR_IN_MILLIS);
			pstmt.addBatch();
			if(i%1000==999){
				pstmt.executeBatch();
				pstmt.clearBatch();
			}
			pstmt.executeBatch();
			conn.commit();
		}
	}

	@Test
	@Ignore
	/**
	 * 使用in按id删除
	 */
	public void testQueryByIds() throws SQLException{
		openConn();
		StringBuffer strBuffer = new StringBuffer("delete from T_TEST where t_id in (");
		for(int i=0;i<20000;i++){
			strBuffer.append(i+1);
			//strBuffer.append(rand.nextInt(MAX_COUNT));
			if(i!=20000-1)
				strBuffer.append(",");
		}
		strBuffer.append(")");
		String sql= strBuffer.toString();
		conn.setAutoCommit(false);
		
		long start = System.currentTimeMillis();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		int count = pstmt.executeUpdate();
		conn.commit();
		long end= System.currentTimeMillis();
		
		pw.append("使用in按ID删除"+count+"个条数据耗时："+(end-start));
		System.out.println("使用in按ID删除"+count+"个条数据耗时："+(end-start));
		closeConn();
	}
	
	
	@Test
	@Ignore
	/**
	 * 逐条删除
	 */
	public void testQueryBySepIds() throws SQLException{
		openConn();
		long start = System.currentTimeMillis();
		int totalCount = 20000;
		conn.setAutoCommit(false);
		PreparedStatement pstmt = null;
		for(int i=0;i<totalCount;i++){
			String sql="delete from T_TEST where t_id = "+(i+1);
			//strBuffer.append(rand.nextInt(MAX_COUNT));
			pstmt  = conn.prepareStatement(sql);
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		conn.commit();
		long end= System.currentTimeMillis();
		
		pw.append("按ID逐条删除"+totalCount+"个条数据耗时："+(end-start));
		System.out.println("按ID逐条删除"+totalCount+"个条数据耗时："+(end-start));
		closeConn();
	}
	
	@Test
	@Ignore
	/**
	 * 逐条删除并定时清除数据
	 */
	public void testQueryBySepIdsWithClearBatch() throws SQLException{
		openConn();
		int totalCount = 20000;
		conn.setAutoCommit(false);
		PreparedStatement pstmt = null;
		
		long start = System.currentTimeMillis();
		for(int i=0;i<totalCount;i++){
			String sql="delete from T_TEST where t_id = "+(i+1);
			//strBuffer.append(rand.nextInt(MAX_COUNT));
			pstmt  = conn.prepareStatement(sql);
			pstmt.addBatch();
			if(i%100==99){
				pstmt.executeBatch();
				pstmt.clearBatch();
			}
		}
		pstmt.executeBatch();
		conn.commit();
		long end= System.currentTimeMillis();
		
		pw.append("按ID逐条删除(采用clearBatch)"+totalCount+"个条数据耗时："+(end-start));
		System.out.println("按ID逐条删除(采用clearBatch)"+totalCount+"个条数据耗时："+(end-start));
		closeConn();
	}
	
	
	
	@Test
	@Ignore
	/**
	 * 按条件删除
	 */
	public void testQueryByConds() throws SQLException{
		openConn();
		String sql = "delete from T_TEST where t_time> "+(currTime-duration*HOUR_IN_MILLIS/6*2);
		conn.setAutoCommit(false);
		
		long start = System.currentTimeMillis();
		PreparedStatement pstmt = conn.prepareStatement(sql);
		int count = pstmt.executeUpdate();
		conn.commit();
		long end= System.currentTimeMillis();
		
		pw.append("按条件删除"+count+"个条数据耗时："+(end-start));
		System.out.println("按条件删除"+count+"个条数据耗时："+(end-start));
		closeConn();
	}
	
	private void closeConn() throws SQLException{
		if(pw!=null){
			pw.flush();
			pw.close();
		}
		if(conn!=null)
			conn.close();
	}

}
