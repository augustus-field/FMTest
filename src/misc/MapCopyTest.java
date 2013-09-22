package misc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MapCopyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Map<String, String>> rtoMap = new HashMap<String, Map<String,String>>();
		rtoMap.put("bc", new HashMap<String, String>());
		rtoMap.get("bc").put("ratio", "1.29");
		rtoMap.get("bc").put("marketType", "2");
		System.out.println(rtoMap);
		Map<String, Map<String, String>> clRtoMap = getRatioMap(rtoMap);
		System.out.println(clRtoMap);
		System.out.println(rtoMap.equals(clRtoMap));
		System.out.println(rtoMap==clRtoMap);
	}
	

	public static Map<String, Map<String, String>> getRatioMap(Map<String, Map<String, String>> ratioMap) {
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		Set<String> set = ratioMap.keySet(); 
		Iterator<String> it =set.iterator();
		Map<String, Map<String, String>> outMap = new HashMap<String, Map<String,String>>();
		while (it.hasNext()) {
			String thirdKey = it.next();
			Map<String, String> innerMap = ratioMap.get(thirdKey);
			Set<String> innerSet = innerMap.keySet();
			Iterator<String> innerIt = innerSet.iterator();
			Map<String, String> newInnerMap = new HashMap<String, String>();
			while (innerIt.hasNext()) {
				String innerKey = innerIt.next();
				String value = innerMap.get(innerKey);
				newInnerMap.put(innerKey, value);
			}
			outMap.put(thirdKey, newInnerMap);
		}
		map.putAll(outMap);
		return map;
	}

}
