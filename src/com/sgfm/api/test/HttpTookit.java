package com.sgfm.api.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

/**
 * HTTP工具箱
 * 
 */
public final class HttpTookit {
	private static Log log = LogFactory.getLog(HttpTookit.class);
	private static InputStream is;
	private static int counter= 0;
	CookieStore cookieStore = new BasicCookieStore();
	/**
	 * 执行一个HTTP GET请求，返回请求响应的HTML
	 * 不启用压缩
	 * @param url
	 *            请求的URL地址
	 * @param queryString
	 *            请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 * @throws FileNotFoundException 
	 */
	public static String doGet(String url, String queryString) throws FileNotFoundException {
		File file = new File("test"+(counter++));
		FileOutputStream os = new FileOutputStream(file);
		
		String response = null;
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(url);

		try {
			if (StringUtils.isNotBlank(queryString))
				method.setQueryString(URIUtil.encodeQuery(queryString));
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				response = method.getResponseBodyAsString();
				
				
				// get return header and stream
				is=method.getResponseBodyAsStream();
				// Write to local output
				int input = -1;
				while((input=is.read())!=-1){
					os.write(input);
				}
				os.close();
				log.info("File size(bytes): "+file.length());
			}
		} catch (URIException e) {
			log.error("执行HTTP Get请求时，编码查询字符串“" + queryString + "”发生异常！", e);
		} catch (IOException e) {
			log.error("执行HTTP Get请求" + url + "时，发生异常！", e);
		} finally {
			method.releaseConnection();
		}
		return response;
	}

	/**
	 * 执行一个HTTP POST请求，返回请求响应的HTML
	 * 启用压缩
	 * @param url
	 *            请求的URL地址
	 * @param params
	 *            请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 * @throws FileNotFoundException 
	 */
	/**
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException 
	 */
	public static String doPost(String url, Map<String, String> params) throws IOException {
		// Write response to local file output for testing
		File file = new File("test"+(counter++)+".gz");
		GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(file));
		
		String response = null;
		HttpClient client = new HttpClient();
		HttpMethod method = new PostMethod(url);
		// Request gzipped resource
		method.setRequestHeader("Accept-Encoding", "gzip");
		
		// 设置Http Post数据
		if (params != null) {
			HttpMethodParams p = new HttpMethodParams();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				p.setParameter(entry.getKey(), entry.getValue());
			}
			method.setParams(p);
		}
		try {
			client.executeMethod(method);
			if (method.getStatusCode() == HttpStatus.SC_OK) {
				response = method.getResponseBodyAsString();
				System.out.println(method.getResponseHeader("Content-Encoding"));
				// get return header and stream
				GZIPInputStream gin = new GZIPInputStream(method.getResponseBodyAsStream());
				int i = -1;
				while((i=gin.read())!=-1){
					os.write(i);
				}
				os.close();
				log.info("File size(bytes): "+file.length());
			}
		} catch (IOException e) {
			log.error("执行HTTP Post请求" + url + "时，发生异常！", e);
		} finally {
			method.releaseConnection();
		}
		return response;
	}

	/**
	 * Simple method (using URLConnection) for getting response from a url.
	 * @param urlStr
	 * @return
	 * @throws IOException
	 */
	public static String doConnection(String urlStr) throws IOException{
		File file = new File("test"+(counter++));
		FileOutputStream os = new FileOutputStream(file);
		
		URL url = new URL(urlStr);
		URLConnection con = url.openConnection();
		
		InputStream in = con.getInputStream();
		String encoding = con.getContentEncoding();
		
		// Test if gzip is enabled.
		if ("gzip".equals(encoding)) {
		    in = new GZIPInputStream(in);
		    System.out.println("gzip enabled!");
		}
		
		System.out.println(con.getContentEncoding());
		System.out.println(con.getContentType());
		Map<String, List<String>>  map = con.getHeaderFields();
		Set<String> headers=  map.keySet();
		for (String head: headers) {
			List<String> headValues = map.get(head);
			System.out.print(head);
			System.out.println(": "+headValues.toString());
		}
		
		// Write to local output
		int input = -1;
		while((input=in.read())!=-1){
			os.write(input);
		}
		os.close();
		log.info("File size(bytes): "+file.length());
		
		encoding = encoding == null ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		return body;
	}
	
	public static void main(String[] args) throws IOException {
//		doGet("http://localhost:8088/SgfmApi/marketInfo.sv",null);
//		doPost("http://localhost:8088/SgfmApi/marketInfo.sv",new HashMap<String, String>());
//		doGet("http://stackoverflow.com/questions/1450151/can-i-compress-http-requests-using-gzip",null);
//		doPost("http://stackoverflow.com/questions/1450151/can-i-compress-http-requests-using-gzip",new HashMap<String, String>());
	}
}