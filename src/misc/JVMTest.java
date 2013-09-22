package misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.codec.binary.StringUtils;

public class JVMTest {

	public static void main(String[] args) throws IOException {
		Properties jvmConf = System.getProperties();
		for(Map.Entry<Object, Object> o: jvmConf.entrySet()){
			System.out.println(String.format("%s:\t %s", o.getKey(), o.getValue()));
		}
		
		Process process = Runtime.getRuntime().exec("ls");
		StringBuilder sb = new StringBuilder();
		InputStream is = process.getInputStream();
		int i=-1;
		for(;i!=-1;i=is.read()){
			sb.append((char) i);
		}
		is.close();
		System.out.println(sb);
		
		
	}

}
