package security;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;

/**
 * This class connects to HTTPS server by disabling certificate validation.
 * Benchmark: small data
 * 	Secure login took: 420
 	Secure requests took: 131155
	Insecure requests took: 115289
 * 
 * @author cassc
 *
 */
public class SSL {
	/**
	 * Cookie store string
	 */
	private static String cs = null;
	private PrintWriter pw = null;
//	private static String baseURL = "https://172.17.108.64:8443";
//	private static final String STR_LOGIN="/SgfmApi/login.sv?param={'un':'fbfbfb','pwd':'111111'}";
	private static String baseURL = "http://172.17.110.22:8080";
	private static final String STR_LOGIN= 	 "/SgfmApi/login.sv?&param={\"un\":\"fbbbcc\",\"pwd\":\"111111\"}";

	private static final String SETTLED= "0"; // new Random().nextInt(2)+"";
	private static final String INSECURE_PORT ="8084";
	private static final int TEST_COUNT=10;
	private static final int SEL_ID=6272;
//	private static final int SEL_ID=3960;
	
	public static void main(String[] args) throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
		// Disable SSL validation 
		disableCertificateValidation();
		long timeBeforeLogin = System.currentTimeMillis(); 
		sendRequest(STR_LOGIN);
//		cs  = "JSESSIONID = czc74ypo7u8o1iqu6faez7d5i ; ";
		long timeAfterLogin = System.currentTimeMillis(); 
		for(int i=0;i<TEST_COUNT;i++){
			sendRequest(STR_GET_MARKET);
//		sendRequest(STR_ORDER_ADD);
//		sendRequest(STR_ORDER_LIST);
//		sendRequest(STR_INVALID_OD_LIST);
//		sendRequest(STR_GET_MARKET);
//		sendRequest(STR_LOGOUT);
		}
		long timeAfterSecureRequests = System.currentTimeMillis(); 
		// sendRequest(STR_LOGOUT);
		
//		baseURL=convertToInsecureURL(baseURL);
//		for(int i=0;i<TEST_COUNT;i++){
////			sendRequest(STR_LOGIN);
//		sendRequest(STR_ORDER_ADD);
//		sendRequest(STR_ORDER_LIST);
//		sendRequest(STR_INVALID_OD_LIST);
//		sendRequest(STR_GET_LEAGUE);
////		sendRequest(STR_LOGOUT);
//		}
		//  sendRequest(STR_ORDER_UNDO);
		
		long timeAfterInsecureRequests = System.currentTimeMillis(); 
		
		
		System.out.println(String.format("Secure login time(ms): %d\n " +
				"Secure requests time(ms): %d\n" +
				"Insecure requests time(ms): %d\n",
				timeAfterLogin-timeBeforeLogin, 
				timeAfterSecureRequests-timeAfterLogin,
				timeAfterInsecureRequests-timeAfterSecureRequests));
		
//		useImportedKey();
	}
	

	public static void useImportedKey() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException, KeyManagementException{

		// Accept all certificates from this localhost
//		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(
//			    new javax.net.ssl.HostnameVerifier(){
//			        public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
//			            if (hostname.equals("localhost")) {
//			            	Certificate[] cfs = null;
//			            	try {
//								cfs = sslSession.getPeerCertificates();
//							} catch (SSLPeerUnverifiedException e) {
//								e.printStackTrace();
//							}
//			            	for (Certificate cf : cfs) {
//								System.out.println(cf.getType());
//								System.out.println(cf.getPublicKey());
//							}
//			            	return true;
//			            }
//			            return false;
//			        }
//			    });
		
		/* Load the keyStore that includes self-signed cert as a "trusted" entry. */
		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		InputStream trustStore = new FileInputStream("/fantasy/log/lcerts");
		keyStore.load(trustStore, "111111".toCharArray());
		TrustManagerFactory tmf =   TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		tmf.init(keyStore);
		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, tmf.getTrustManagers(), null);
		SSLSocketFactory sslFactory = ctx.getSocketFactory();

		//SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		URL url = new URL("https://localhost:8443");
		HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
		conn.setSSLSocketFactory(sslFactory);
		conn.setRequestMethod("POST");
		
		InputStream inputstream = conn.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

		String string = null;
		while ((string = bufferedreader.readLine()) != null) {
		    System.out.println("Received " + string);
		}
		
		//trustStore.close();

	}

	/**
	 * http://stackoverflow.com/questions/875467/java-client-certificates-over-https-ssl
	 * Disable SSL certificate validation
	 */
	public static void disableCertificateValidation() {
		  // Create a trust manager that does not validate certificate chains
		  TrustManager[] trustAllCerts = new TrustManager[] { 
		    new X509TrustManager() {
		      public X509Certificate[] getAcceptedIssuers() { 
		        return new X509Certificate[0]; 
		      }
		      public void checkClientTrusted(X509Certificate[] certs, String authType) {}
		      public void checkServerTrusted(X509Certificate[] certs, String authType) {}
		  }};

		  // Ignore differences between given hostname and certificate hostname
		  HostnameVerifier hv = new HostnameVerifier() {
		    public boolean verify(String hostname, SSLSession session) { return true; }
		  };

		  // Install the all-trusting trust manager
		  try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		    HttpsURLConnection.setDefaultHostnameVerifier(hv);
		  } catch (Exception e) {}
	}
	
	public static void sendRequest(String urlParaStr) throws IOException{
		if(baseURL.startsWith("https"))
			sendSecureRawRequest(baseURL+urlParaStr);
		else
			sendInsecureRawRequest(baseURL+urlParaStr);
	}
	
	/**
	 * Send a normal http request and update cookie
	 * @param urlStr
	 * @throws IOException
	 */
	public static void sendInsecureRawRequest(String urlStr) throws IOException{
		URL url = new URL(urlStr);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();

		updateCookieForConnection(connection);

		Scanner scanner = new Scanner(connection.getInputStream());
		while (scanner.hasNext()) {
		    System.out.println(scanner.next());
		}
		
		connection.disconnect();
	}
	
	/**
	 * Send a secure SSL request/get and update cookie. 
	 * @param urlStr
	 * @throws IOException
	 */
	public static void sendSecureRawRequest(String urlStr) throws IOException{
		URL url = new URL(urlStr);
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		
		updateCookieForConnection(connection);
		
		Scanner scanner = new Scanner(connection.getInputStream());
		while (scanner.hasNext()) {
		    System.out.println(scanner.next());
		}
		
		Map<String, List<String>> headerMap = connection.getHeaderFields();
		for (String key : headerMap.keySet()) {
			List<String> vals = headerMap.get(key);
			System.out.println("Key--vals "+key);
			for (String val: vals) {
				System.out.println(val);
			}
			System.out.println("---");
		}
		
		connection.disconnect();
	}

	/**
	 * Update cookie for a connection
	 * @param connection
	 */
	private static void updateCookieForConnection(URLConnection connection) {
		// Send cookie to server if available
		if (cs != null)
			connection.setRequestProperty("Cookie", cs);
		// Update local cookie string if response has non-null cookie string
		String newCookie = connection.getHeaderField("Set-Cookie");
//		System.out.println(newCookie);
		if(newCookie!=null){
			String[] cookies = newCookie.split(";");
			String newCS="";
			for (String str : cookies) {
				if(!str.toLowerCase().startsWith("path"))
					newCS+=str+";";
			}
			cs =StringUtils.removeEnd(newCS, ";");
			System.out.println("Cookie updated to "+cs);
		}
	}
	
	
	private static String convertToInsecureURL(String urlStr) {
		String url = StringUtils.replace(urlStr, "https", "http");
		url=StringUtils.replace(url, "8443", INSECURE_PORT);
		return url;
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
		"}&login.sv";
	private static final String STR_INVALID_OD_LIST = "/SgfmApi/orderManage.sv?act=getInvalidOrderList&param={st_date:" +
		TIME_ONE_DAY_EARLY+"," +
		"settled:"+SETTLED+
		"}";
	private static final String STR_ORDER_LIST = "/SgfmApi/orderManage.sv?act=getOrderList&param={st_date:" +
		TIME_ONE_WEEK_EARLY+
		"}&login.sv";
	private static final String STR_ORDER_LIST_SETTLED = "/SgfmApi/orderManage.sv?act=getOrderList&param={st_date:" +
			TIME_ONE_WEEK_EARLY+","+
			"settled:'1'"+
			"}";
	private static final String STR_MKT_INFO = "/SgfmApi/marketInfo.sv?";
	private static final String STR_MKT_SELECTION = "/SgfmApi/marketInfo.sv?act=getSelections&param={sel_ids:[731540,731541,731545]}";
	private static final String STR_ORDER_ADD = "/SgfmApi/orderManage.sv?act=addOrder&param={%22orders%22:[{%22type%22:3,%22direction%22:1,%22price%22:0.98,%22stake%22:100,%22sel_id%22:"+SEL_ID+",%22expire%22:0,%22ivtm%22:0}]}"; 
//	"{'orders':[{'type':3,'direction':2,'price':1.98,'stake':100, 'sel_id':1222,'expire':3}]}";
	private static final String STR_ORDER_UNDO = "/Sgf;mApi/orderManage.sv?act=undoOrder";
	
	private static final String STR_ORDER_MOD = "/SgfmApi/orderManage.sv?act=updOrder&param={order_id:1000000007245,price:0.86,stake:100,expire:0,time_out:5}"; 
		// "/Sgf;mApi/orderManage.sv?act=updOrder&param={order_id:123456, price:0.86, stake:12000,expire:20}";
//	private static final String STR_ORDER_ADD = "/SgfmApi/orderManage.sv?act=addOrder&param=" +
//			"{'orders':[{'type':3,'direction':1,'price':0.98,'stake':100, 'sel_id':739920,'expire':3}," +
//			"{'type':1,'direction':1,'price':0.96,'stake':10000, 'sel_id':739924,'time_out':59}," +
//			"{'type':3,'direction':1,'price':0.97,'stake':8000, 'sel_id':739932,'expire':3}]}";
	private static final String STR_GET_LEAGUE = "/SgfmApi/marketInfo.sv?act=getLeague&login.sv";
	private static final String STR_GET_MARKET = "/SgfmApi/marketInfo.sv";
	private static final String STR_GET_MARKET_COND = "/SgfmApi/marketInfo.sv?param={language:'EN','lg_ids':[],'event_ids':[],'ver_num':52}&d=1366363770897";
	private static final String STR_LOGOUT = "/SgfmApi/logout.sv";
	
	private static final String outputPath = "output.log" ;
}
