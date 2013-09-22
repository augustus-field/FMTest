package misc;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.HandlerBase;

import com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl;

public class SaxParseTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String xml = "";
		long st = System.currentTimeMillis();
		SAXParserFactory spf = new SAXParserFactoryImpl();
		try {
			spf.newSAXParser().parse(new ByteArrayInputStream(xml.getBytes()), new HandlerBase());
			long cur = System.currentTimeMillis()-st;
			if(cur>100){
				System.out.println("Parsing this xml takes time "+cur+": \n" + xml);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	}


