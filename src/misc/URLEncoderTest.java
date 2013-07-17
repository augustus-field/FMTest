package misc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class URLEncoderTest {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		String path = "http://localhost:8084/SgfmApi/marketInfo.sv?param={%22language%22:%22EN%22,%22lg_ids%22:[],%22event_ids%22:[],%22ver_num%22:228}&d=1372820160716";
		System.out.println(URLDecoder.decode(path, "UTF-8"));
	}

}
