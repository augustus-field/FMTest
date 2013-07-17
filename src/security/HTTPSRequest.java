package security;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.contrib.ssl.EasySSLProtocolSocketFactory;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


public class HTTPSRequest {

	private static final String BASE_URL = "https://localhost:8443";
	private static final String STR_LOGIN="/SgfmApi/login.sv?param={'un':'fbccbb','pwd':'123456'}";
	/**
	 * 查询 0 未结算， 1 已结算
	 */
	private static final int SETTLED = 1;
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
		for(int i=0;i<1;i++){
			sendRequest(STR_ORDER_ADD);
			System.out.println(String.format("Total time consumed: +%fs", 
					(System.currentTimeMillis()-prevTime*1.0)/1000) );
			//
			// TimeUnit.SECONDS.sleep(2);
		}
		
		
		sendRequest(STR_GET_LEAGUE);
//		sendRequest(STR_GET_MARKET);
		
//		sendRequest(STR_ORDER_LIST);
//		sendRequest(STR_ORDER_LIST_SETTLED);
//		sendRequest(STR_INVALID_OD_LIST);
//		sendRequest(STR_VALID_OD_LIST);
		System.out.println(String.format("Total time consumed: +%fs", 
				(System.currentTimeMillis()-prevTime*1.0)/1000) );
	}
	
	
	private PrintWriter pw = null;
 
	private HttpClient httpclient = null;

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
	public void setUp() throws HttpException, IOException  {
		
		if(cleanPreviousLog){
			File f = new File(outputPath);
			if(f.exists()&&f.isFile())
				f.delete();
			// Do not remove previous logs in test cases of a single test.
			cleanPreviousLog=false;
		}
		
		// May incurr MIM attack: http://en.wikipedia.org/wiki/Man-in-the-middle_attack
		@SuppressWarnings("deprecation")
		Protocol easyhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8443);
		URI uri = new URI("https://172.17.109.92", true);
		// use relative url only
		GetMethod httpget = new GetMethod(uri.getPathQuery());
		HostConfiguration hc = new HostConfiguration();
		hc.setHost(uri.getHost(), uri.getPort(), easyhttps);
		httpclient = new HttpClient();
		httpclient.executeMethod(hc, httpget);

		// sendRequest(STR_LOGIN);
	}
	

	
	/**
	 * 向服务器发送doGet请求，打印请求地址与相应的返回结果
	 * @param urlStr 请求地址
	 * @throws HttpException
	 * @throws IOException
	 */
	private void sendRequest(String urlStr) {
		
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




