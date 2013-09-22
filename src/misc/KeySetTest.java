package misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class KeySetTest {

	public static void main(String[] args) {
		Map<String,String> mymap = new HashMap<String, String>();
		mymap.put("a", "fun");
		mymap.put("b", "funny");
		
		Set<String> keyset = mymap.keySet();
		System.out.println(keyset);
	}

}
