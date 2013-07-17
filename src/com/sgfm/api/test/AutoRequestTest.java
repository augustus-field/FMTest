package com.sgfm.api.test;


import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;


public class AutoRequestTest {

//	private static final String BASE_URL = "http://172.17.108.64:8080";
//	private static final String STR_LOGIN="/SgfmApi/login.sv?param={'un':'fbfbfb000m','pwd':'111111'}";
	private boolean reqGzip = true;
	private int counter = 1 ;
	
//	private static final String BASE_URL = "http://172.17.110.22:8080";
//	private static final String STR_LOGIN="/SgfmApi/login.sv?param={'un':'fbbbcc001m','pwd':'111111'}";

    private static final boolean PRINT_CONTENT = false;
	private static final String BASE_URL = "http://172.17.110.61:8088";
	private static final String STR_LOGIN="/SgfmApi/login.sv?param={'un':'fbpapg','pwd':'111111'}";
	
	/**
	 * 查询 0 未结算， 1 已结算
	 */
	private static final int SETTLED = 1;
	
	public static void main(String[] args) throws InterruptedException {
		AutoRequestTest autoTest = new AutoRequestTest();
		autoTest.setUp();
		autoTest.testGet();
	}
	
	@Test
	public void testGet() throws InterruptedException {
		long prevTime = System.currentTimeMillis();
		// Query valid order list
//		sendRequest(STR_VALID_OD_LIST+"&param={order_id:[123456,789101], order_sta:1, st_date:'" +
//				+(System.currentTimeMillis()-3600*24*1000*5)+
//				"',end_date:'" +
//				+System.currentTimeMillis()+
//				"', settled:1, direction:1,type:1, pg_no:1,pg_size:50}");
//		sendRequest(STR_VALID_OD_LIST+"&param={st_date:'" +
//				+(System.currentTimeMillis()-3600*24*1000*6)+
//				"',end_date:'" +
//				+System.currentTimeMillis()+
//				"', settled:1}");
		//sendRequest(STR_VALID_OD_LIST+"@param={st_date:'"+getTimeNDaysBefore(1)+ "', settled:0}");
		
		//sendRequest(STR_MKT_INFO);
		//sendRequest(STR_MKT_INFO+"&param={ver_num:33}");
		//sendRequest(STR_MKT_SELECTION);
		// Query add order
//		sendRequest(STR_ORDER_UNDO);
		for(int i=0;i<2;i++){
			sendRequest(STR_GET_MARKET);
//			sendRequest(STR_ORDER_ADD);
			System.out.println(String.format("Total time consumed: +%fs", 
					(System.currentTimeMillis()-prevTime*1.0)/1000) );
			//
			// TimeUnit.SECONDS.sleep(2);
			reqGzip=false;
		}
		
		
//		sendRequest(STR_GET_LEAGUE);
//		sendRequest(STR_GET_MARKET);
		
//		sendRequest(STR_ORDER_LIST);
//		sendRequest(STR_ORDER_LIST_SETTLED);
//		sendRequest(STR_INVALID_OD_LIST);
//		sendRequest(STR_VALID_OD_LIST);
		System.out.println(String.format("Total time consumed: +%fs", 
				(System.currentTimeMillis()-prevTime*1.0)/1000) );
	}
	
	
	private PrintWriter pw = null;
 
	private DefaultHttpClient httpclient = null;

	/**
	 * Set to true to clear log everytime before running this test.
	 */
	private boolean cleanPreviousLog = false;
	
	
	// Use Apache HttpClient & HttpCore
	// to store and use cookies among multiple requests, 
	// thus keep session alive
	CookieStore cookieStore = new BasicCookieStore();
	HttpContext httpContext = new BasicHttpContext();
	
	
	@Before
	public void setUp()  {
		
		if(cleanPreviousLog){
			File f = new File(outputPath);
			if(f.exists()&&f.isFile())
				f.delete();
			// Do not remove previous logs in test cases of a single test.
			cleanPreviousLog=false;
		}
			
		// Get HTTP client instance
		httpclient  = new DefaultHttpClient();
		// Login address
		sendRequest(STR_LOGIN);
	}
	

	
	/**
	 * 向服务器发送doGet请求，打印请求地址与相应的返回结果
	 * @param urlStr 请求地址
	 * @throws HttpException
	 * @throws IOException
	 */
	private void sendRequest(String urlStr) {
		try {
			String url = new String(urlStr);
			url = BASE_URL + url;

			writeLog("REQUEST"+StringUtils.repeat(">", 12)+ " \n"+url, outputPath);

			httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			HttpUriRequest request = new HttpGet(URIUtil.encodeQuery(url,URI_CHARSET));        

			// Request for gzip version
			if(reqGzip)
				request.setHeader("Accept-Encoding", "gzip");
			// Execute HTTP GET
			HttpResponse response = httpclient.execute(request, httpContext);
			
			
			// Print out response
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// Buffering unknown size: discouraged. Using InputStream instead.
				// String responseStr = httpget.getResponseBodyAsString();
				Header[] headers = response.getAllHeaders();
				for (Header header : headers) {
					System.out.println(String.format("%s:%s",header.getName(), header.getValue()));
				}

				long length = 0; // file size in bytes
				InputStream is = response.getEntity().getContent();
				boolean isGzippedData = false;
				
				
				File file = null; 
				OutputStream os = null; 
				Header contentEncoding = response.getEntity().getContentEncoding();
				String gzipName = "";
				if(reqGzip){
					// Request gzip version and returns gzip data
					if(contentEncoding!=null && contentEncoding.getValue().equalsIgnoreCase("gzip")){
						isGzippedData=true;
						gzipName="gzip";
						file = new File("test"+(counter++)+".gz");
//						os = new GZIPOutputStream(new FileOutputStream(file));	
						os = new FileOutputStream(file);	
					}
				}
				
				if(!isGzippedData){
					// Not getting gzipped data
					file = new File("test"+(counter++)+".json");
					os = new FileOutputStream(file);	
				}
				
				// Write to local output
				int input = -1;
				while((input=is.read())!=-1){
					os.write(input);
				}
				os.flush();
				os.close();
				length=file.length();

                writeLog("RESPONSE TYPE: "+ gzipName + " LENGTH: "+length + " bytes. File written to "+file.getAbsolutePath(), outputPath);

                // Print content only for non-gzipped data
                if(!isGzippedData &&  PRINT_CONTENT){
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(is, writer, URI_CHARSET);
                    writeLog("\n "+writer.toString(),outputPath);
                }

			}else
				writeLog("ERROR"+StringUtils.repeat("X", 12)+ " \n"+response.getStatusLine().getStatusCode(), outputPath);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} 
	}
	
	@After
	public void cleanUp(){
		if(httpclient!=null)
			httpclient.getConnectionManager().shutdown();
		if(pw!=null)
			pw.close();
	}
	
	private long getTimeNDaysBefore(int n){
		return System.currentTimeMillis()-n*3600*1000*24;
	}
	
	
	public void writeLog(String str, String path) {
		System.out.println(str);
		try {
			if(pw==null){
				pw = new PrintWriter(new FileOutputStream(path, true));
				pw.append("\n\n"+StringUtils.repeat("*", 32)+"\n");
			}
			pw.append(str);
			pw.flush();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			pw.append("\n");
		}
	}
	/**
	 * 6.9 days earlier than now
	 */
	private static final long TIME_ONE_WEEK_EARLY = System.currentTimeMillis()-3600*24*100*69;
	/**
	 * 23 hours earlier
	 */
	private static final long TIME_ONE_DAY_EARLY = System.currentTimeMillis()-360*100*23;

	private static final String URI_CHARSET = "UTF8";
	

	
	private static final String STR_VALID_OD_LIST = "/SgfmApi/orderManage.sv?act=getValidOrderList&param={st_date:" +
		TIME_ONE_WEEK_EARLY+"," +
		"settled:"+SETTLED+
		"}";
	private static final String STR_INVALID_OD_LIST = "/SgfmApi/orderManage.sv?act=getInvalidOrderList&param={st_date:" +
		TIME_ONE_DAY_EARLY+"," +
		"settled:"+SETTLED+
		"}";
	private static final String STR_ORDER_LIST = "/SgfmApi/orderManage.sv?act=getOrderList&param={st_date:" +
		TIME_ONE_WEEK_EARLY+
		"}";
	private static final String STR_ORDER_LIST_SETTLED = "/SgfmApi/orderManage.sv?act=getOrderList&param={st_date:" +
			TIME_ONE_WEEK_EARLY+","+
			"settled:'1'"+
			"}";
	private static final String STR_MKT_INFO = "/SgfmApi/marketInfo.sv?";
	private static final String STR_MKT_SELECTION = "/SgfmApi/marketInfo.sv?act=getSelections&param={sel_ids:[731540,731541,731545]}";
	private static final String STR_ORDER_ADD = "/SgfmApi/orderManage.sv?act=addOrder&param=" +
			"{'orders':[{'type':3,'direction':2,'price':1.32,'stake':100, 'sel_id':1347}]}";
//	"{'orders':[{'type':3,'direction':2,'price':1.98,'stake':100, 'sel_id':1222,'expire':3}]}";
	private static final String STR_ORDER_UNDO = "/SgfmApi/orderManage.sv?act=undoOrder";
//	private static final String STR_ORDER_ADD = "/SgfmApi/orderManage.sv?act=addOrder&param=" +
//			"{'orders':[{'type':3,'direction':1,'price':0.98,'stake':100, 'sel_id':739920,'expire':3}," +
//			"{'type':1,'direction':1,'price':0.96,'stake':10000, 'sel_id':739924,'time_out':59}," +
//			"{'type':3,'direction':1,'price':0.97,'stake':8000, 'sel_id':739932,'expire':3}]}";
	private static final String STR_GET_LEAGUE = "/SgfmApi/marketInfo.sv?act=getLeague";
	private static final String STR_GET_MARKET = "/SgfmApi/marketInfo.sv";
	private static final String STR_GET_MARKET_COND = "/SgfmApi/marketInfo.sv?param={language:'EN','lg_ids':[],'event_ids':[],'ver_num':52}&d=1366363770897";
	
	private static final String outputPath = "output.log" ;
}



